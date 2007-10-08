/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * A Transaction Manager to manage LDAP and JDBC operations within the same
 * transaction. Note that even though the same logical transaction is used, this
 * is <b>not</b> a JTA XA transaction; no two-phase commit will be performed,
 * and thus commit and rollback may yield unexpected results.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class ContextSourceAndDataSourceTransactionManager extends
        DataSourceTransactionManager {

    private static final long serialVersionUID = 6832868697460384648L;

    private ContextSourceTransactionManagerDelegate ldapManagerDelegate = new ContextSourceTransactionManagerDelegate();

    public ContextSourceAndDataSourceTransactionManager() {
        super();
        // Override the default behaviour.
        setNestedTransactionAllowed(false);
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#isExistingTransaction(java.lang.Object)
     */
    protected boolean isExistingTransaction(Object transaction) {
        ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) transaction;

        return super.isExistingTransaction(actualTransactionObject
                .getDataSourceTransactionObject());
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doGetTransaction()
     */
    protected Object doGetTransaction() throws TransactionException {
        Object dataSourceTransactionObject = super.doGetTransaction();
        Object contextSourceTransactionObject = ldapManagerDelegate
                .doGetTransaction();

        return new ContextSourceAndDataSourceTransactionObject(
                contextSourceTransactionObject, dataSourceTransactionObject);
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin(java.lang.Object,
     *      org.springframework.transaction.TransactionDefinition)
     */
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) transaction;

        super.doBegin(actualTransactionObject.getDataSourceTransactionObject(),
                definition);
        ldapManagerDelegate.doBegin(actualTransactionObject
                .getLdapTransactionObject(), definition);
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) transaction;

        super.doCleanupAfterCompletion(actualTransactionObject
                .getDataSourceTransactionObject());
        ldapManagerDelegate.doCleanupAfterCompletion(actualTransactionObject
                .getLdapTransactionObject());
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {

        ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) status
                .getTransaction();

        try {
            super.doCommit(new DefaultTransactionStatus(actualTransactionObject
                    .getDataSourceTransactionObject(), status
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
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceAndDataSourceTransactionObject actualTransactionObject = (ContextSourceAndDataSourceTransactionObject) status
                .getTransaction();

        super.doRollback(new DefaultTransactionStatus(actualTransactionObject
                .getDataSourceTransactionObject(), status.isNewTransaction(),
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

    private class ContextSourceAndDataSourceTransactionObject {
        private Object ldapTransactionObject;

        private Object dataSourceTransactionObject;

        public ContextSourceAndDataSourceTransactionObject(
                Object ldapTransactionObject, Object dataSourceTransactionObject) {
            this.ldapTransactionObject = ldapTransactionObject;
            this.dataSourceTransactionObject = dataSourceTransactionObject;
        }

        public Object getDataSourceTransactionObject() {
            return dataSourceTransactionObject;
        }

        public Object getLdapTransactionObject() {
            return ldapTransactionObject;
        }
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doSuspend(java.lang.Object)
     */
    protected Object doSuspend(Object transaction) throws TransactionException {
        throw new TransactionSuspensionNotSupportedException(
                "Transaction manager [" + getClass().getName()
                        + "] does not support transaction suspension");
    }

    /*
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doResume(java.lang.Object,
     *      java.lang.Object)
     */
    protected void doResume(Object transaction, Object suspendedResources)
            throws TransactionException {
        throw new TransactionSuspensionNotSupportedException(
                "Transaction manager [" + getClass().getName()
                        + "] does not support transaction suspension");
    }
}