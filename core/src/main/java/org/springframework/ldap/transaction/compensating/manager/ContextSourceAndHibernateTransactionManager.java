/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.transaction.compensating.manager;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.support.DefaultTransactionStatus;
/**
 * A Transaction Manager to manage LDAP and Hibernate 3 operations within the same
 * transaction. Note that even though the same logical transaction is used, this
 * is <b>not</b> a JTA XA transaction; no two-phase commit will be performed,
 * and thus commit and rollback may yield unexpected results.<br />
 * This Transaction Manager is as good as it gets when you are using in LDAP in 
 * combination with a Hibernate 3 and unable to use XA transactions because LDAP
 * is not transactional by design to begin with.<br />
 * 
 * Furthermore, this manager <b>does not support nested transactions</b>
 * @author Hans Westerbeek
 * @since 1.2.2
 */
public class ContextSourceAndHibernateTransactionManager extends HibernateTransactionManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ContextSourceTransactionManagerDelegate ldapManagerDelegate = new ContextSourceTransactionManagerDelegate();

	 /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#isExistingTransaction(java.lang.Object)
     */
    protected boolean isExistingTransaction(Object transaction) {
        ContextSourceAndHibernateTransactionObject actualTransactionObject = (ContextSourceAndHibernateTransactionObject) transaction;

        return super.isExistingTransaction(actualTransactionObject
                .getHibernateTransactionObject());
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doGetTransaction()
     */
    protected Object doGetTransaction() throws TransactionException {
        Object dataSourceTransactionObject = super.doGetTransaction();
        Object contextSourceTransactionObject = ldapManagerDelegate
                .doGetTransaction();

        return new ContextSourceAndHibernateTransactionObject(
                contextSourceTransactionObject, dataSourceTransactionObject);
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doBegin(java.lang.Object,
     *      org.springframework.transaction.TransactionDefinition)
     */
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        ContextSourceAndHibernateTransactionObject actualTransactionObject = (ContextSourceAndHibernateTransactionObject) transaction;

        super.doBegin(actualTransactionObject.getHibernateTransactionObject(),
                definition);
        try {
            ldapManagerDelegate.doBegin(actualTransactionObject
                    .getLdapTransactionObject(), definition);
        } catch (TransactionException e) {
            // Failed to start LDAP transaction - make sure we clean up properly
            super.doCleanupAfterCompletion(actualTransactionObject.getHibernateTransactionObject());
            throw e;
        }
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        ContextSourceAndHibernateTransactionObject actualTransactionObject = (ContextSourceAndHibernateTransactionObject) transaction;

        super.doCleanupAfterCompletion(actualTransactionObject
                .getHibernateTransactionObject());
        ldapManagerDelegate.doCleanupAfterCompletion(actualTransactionObject
                .getLdapTransactionObject());
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {

        ContextSourceAndHibernateTransactionObject actualTransactionObject = (ContextSourceAndHibernateTransactionObject) status
                .getTransaction();

        try {
            super.doCommit(new DefaultTransactionStatus(actualTransactionObject
                    .getHibernateTransactionObject(), status
                    .isNewTransaction(), status.isNewSynchronization(), status
                    .isReadOnly(), status.isDebug(), status
                    .getSuspendedResources()));
        } catch (TransactionException ex) {
            if (isRollbackOnCommitFailure()) {
                logger.debug("Failed to commit db resource, rethrowing", ex);
                // If we are to rollback on commit failure, just rethrow the
                // exception - this will cause a rollback to be performed on
                // both resources.
                throw ex;
            } else {
                logger
                        .warn("Failed to commit and resource is rollbackOnCommit not set -"
                                + " proceeding to commit ldap resource.");
            }
        }
        ldapManagerDelegate.doCommit(new DefaultTransactionStatus(
                actualTransactionObject.getLdapTransactionObject(), status
                        .isNewTransaction(), status.isNewSynchronization(),
                status.isReadOnly(), status.isDebug(), status
                        .getSuspendedResources()));
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceAndHibernateTransactionObject actualTransactionObject = (ContextSourceAndHibernateTransactionObject) status
                .getTransaction();

        super.doRollback(new DefaultTransactionStatus(actualTransactionObject
                .getHibernateTransactionObject(), status.isNewTransaction(),
                status.isNewSynchronization(), status.isReadOnly(), status
                        .isDebug(), status.getSuspendedResources()));
        ldapManagerDelegate.doRollback(new DefaultTransactionStatus(
                actualTransactionObject.getLdapTransactionObject(), status
                        .isNewTransaction(), status.isNewSynchronization(),
                status.isReadOnly(), status.isDebug(), status
                        .getSuspendedResources()));
    }

    public ContextSource getContextSource() {
        return ldapManagerDelegate.getContextSource();
    }

    public void setContextSource(ContextSource contextSource) {
        ldapManagerDelegate.setContextSource(contextSource);
    }

    protected void setRenamingStrategy(
            TempEntryRenamingStrategy renamingStrategy) {
        ldapManagerDelegate.setRenamingStrategy(renamingStrategy);
    }

    private final static class ContextSourceAndHibernateTransactionObject {
        private Object ldapTransactionObject;

        private Object hibernateTransactionObject;

        public ContextSourceAndHibernateTransactionObject(
                Object ldapTransactionObject, Object hibernateTransactionObject) {
            this.ldapTransactionObject = ldapTransactionObject;
            this.hibernateTransactionObject = hibernateTransactionObject;
        }

        public Object getHibernateTransactionObject() {
            return hibernateTransactionObject;
        }

        public Object getLdapTransactionObject() {
            return ldapTransactionObject;
        }
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doSuspend(java.lang.Object)
     */
    protected Object doSuspend(Object transaction) throws TransactionException {
        throw new TransactionSuspensionNotSupportedException(
                "Transaction manager [" + getClass().getName()
                        + "] does not support transaction suspension");
    }

    /*
     * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doResume(java.lang.Object,
     *      java.lang.Object)
     */
    protected void doResume(Object transaction, Object suspendedResources)
            throws TransactionException {
        throw new TransactionSuspensionNotSupportedException(
                "Transaction manager [" + getClass().getName()
                        + "] does not support transaction suspension");
    }

    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        ldapManagerDelegate.checkRenamingStrategy();
    }
}
