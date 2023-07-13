/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.transaction.compensating.manager;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * A Transaction Manager to manage LDAP and JDBC operations within the same transaction.
 * Note that even though the same logical transaction is used, this is <b>not</b> a JTA XA
 * transaction; no two-phase commit will be performed, and thus commit and rollback may
 * yield unexpected results.
 *
 * Note that nested transactions are not supported.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 * @deprecated The idea of wrapping two transaction managers without actual XA support is
 * probably not such a good idea after all. AbstractPlatformTransactionManager is not
 * designed for this usage.
 */
@Deprecated
public class ContextSourceAndDataSourceTransactionManager extends DataSourceTransactionManager {

	private static final long serialVersionUID = 6832868697460384648L;

	private ContextSourceTransactionManagerDelegate ldapManagerDelegate = new ContextSourceTransactionManagerDelegate();

	public ContextSourceAndDataSourceTransactionManager() {
		super();
		// Override the default behaviour.
		setNestedTransactionAllowed(false);
	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#
	 * isExistingTransaction(java.lang.Object)
	 */
	@Override
	protected boolean isExistingTransaction(Object transaction) {
		// We don't support nested transactions here
		return false;
	}

	/*
	 * @see
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager#doGetTransaction()
	 */
	@Override
	protected Object doGetTransaction() {
		Object dataSourceTransactionObject = super.doGetTransaction();
		Object contextSourceTransactionObject = this.ldapManagerDelegate.doGetTransaction();

		return new ContextSourceAndDataSourceTransactionObject(contextSourceTransactionObject,
				dataSourceTransactionObject);
	}

	/*
	 * @see
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin(java.lang.
	 * Object, org.springframework.transaction.TransactionDefinition)
	 */
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) transaction;

		super.doBegin(actualTransactionObject.getDataSourceTransactionObject(), definition);
		try {
			this.ldapManagerDelegate.doBegin(actualTransactionObject.getLdapTransactionObject(), definition);
		}
		catch (TransactionException ex) {
			// Failed to start LDAP transaction - make sure we clean up properly
			super.doCleanupAfterCompletion(actualTransactionObject.getDataSourceTransactionObject());
			throw ex;
		}
	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#
	 * doCleanupAfterCompletion(java.lang.Object)
	 */
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) transaction;

		super.doCleanupAfterCompletion(actualTransactionObject.getDataSourceTransactionObject());
		this.ldapManagerDelegate.doCleanupAfterCompletion(actualTransactionObject.getLdapTransactionObject());
	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doCommit(org.
	 * springframework.transaction.support.DefaultTransactionStatus)
	 */
	@Override
	protected void doCommit(DefaultTransactionStatus status) {

		ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) status
				.getTransaction();

		try {
			super.doCommit(new DefaultTransactionStatus(actualTransactionObject.getDataSourceTransactionObject(),
					status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
					status.getSuspendedResources()));
		}
		catch (TransactionException ex) {
			if (isRollbackOnCommitFailure()) {
				logger.debug("Failed to commit db resource, rethrowing", ex);
				// If we are to rollback on commit failure, just rethrow the
				// exception - this will cause a rollback to be performed on
				// both resources.
				throw ex;
			}
			else {
				logger.warn("Failed to commit and resource is rollbackOnCommit not set -"
						+ " proceeding to commit ldap resource.");
			}
		}
		this.ldapManagerDelegate.doCommit(new DefaultTransactionStatus(
				actualTransactionObject.getLdapTransactionObject(), status.isNewTransaction(),
				status.isNewSynchronization(), status.isReadOnly(), status.isDebug(), status.getSuspendedResources()));
	}

	/*
	 * @see
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager#doRollback(org.
	 * springframework.transaction.support.DefaultTransactionStatus)
	 */
	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) status
				.getTransaction();

		super.doRollback(new DefaultTransactionStatus(actualTransactionObject.getDataSourceTransactionObject(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources()));
		this.ldapManagerDelegate.doRollback(new DefaultTransactionStatus(
				actualTransactionObject.getLdapTransactionObject(), status.isNewTransaction(),
				status.isNewSynchronization(), status.isReadOnly(), status.isDebug(), status.getSuspendedResources()));
	}

	public ContextSource getContextSource() {
		return this.ldapManagerDelegate.getContextSource();
	}

	public void setContextSource(ContextSource contextSource) {
		this.ldapManagerDelegate.setContextSource(contextSource);
	}

	public void setRenamingStrategy(TempEntryRenamingStrategy renamingStrategy) {
		this.ldapManagerDelegate.setRenamingStrategy(renamingStrategy);
	}

	/*
	 * @see
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager#doSuspend(java.
	 * lang.Object)
	 */
	protected Object doSuspend(Object transaction) {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/*
	 * @see
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager#doResume(java.lang
	 * .Object, java.lang.Object)
	 */
	protected void doResume(Object transaction, Object suspendedResources) {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.ldapManagerDelegate.checkRenamingStrategy();
	}

	private static final class ContextSourceAndDataSourceTransactionObject {

		private Object ldapTransactionObject;

		private Object dataSourceTransactionObject;

		ContextSourceAndDataSourceTransactionObject(Object ldapTransactionObject, Object dataSourceTransactionObject) {
			this.ldapTransactionObject = ldapTransactionObject;
			this.dataSourceTransactionObject = dataSourceTransactionObject;
		}

		Object getDataSourceTransactionObject() {
			return this.dataSourceTransactionObject;
		}

		Object getLdapTransactionObject() {
			return this.ldapTransactionObject;
		}

	}

}
