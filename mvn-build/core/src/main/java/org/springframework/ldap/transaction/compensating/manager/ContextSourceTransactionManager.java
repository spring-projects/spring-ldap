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

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.UnbindOperationExecutor;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;
import org.springframework.transaction.compensating.support.DefaultCompensatingTransactionOperationManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * TransactionManager for managing LDAP transactions. Since transactions are not
 * supported in the LDAP protocol, this class and its collaborators aim to
 * provide <em>compensating</em> transactions instead. Should a transaction
 * need to be rolled back, this TransactionManager will try to restore the
 * original state using information recorded prior to each operation. The
 * operation where the original state is restored is called a compensating
 * operation.
 * <p>
 * <b>NOTE:</b> The transactions provided by this TransactionManager are all
 * <i>client side</i> and are by no means 'real' transactions, in the sense
 * that we know them in the ordinary database world, e.g.:
 * <ul>
 * <li>Should the transaction failure be caused by a network failure, there is
 * no way whatsoever that this TransactionManager can restore the database
 * state. In this case, all possibilities for rollback will be utterly lost.</li>
 * <li>Transaction isolation is not provided, i.e. entries participating in a
 * transaction for one client may very well participate in another transaction
 * for another client at the same time. Should one of these transactions be
 * rolled back, the outcome of this is undetermined, and may in the worst case
 * result in total failure.</li>
 * </ul>
 * </p>
 * <p>
 * While the points above should be noted and considered, the compensating
 * transaction approach will be perfectly sufficient for all but the most
 * unfortunate of circumstances. Considering that there currently is a total
 * absence of server-side transaction support in the LDAP world, being able to
 * mark operations as transactional in the same way as for relational database
 * operations is surely a step forward.
 * </p>
 * <p>
 * An LDAP transaction is tied to a {@link ContextSource}, to be supplied to
 * the {@link #setContextSource(ContextSource)} method. While the actual
 * ContextSource used by the target LdapTemplate instance needs to be of the
 * type {@link TransactionAwareContextSourceProxy}, the ContextSource supplied
 * to this class should be the actual target ContextSource.
 * </p>
 * <p>
 * Using this TransactionManager along with
 * {@link TransactionAwareContextSourceProxy}, all modifying operations (bind,
 * unbind, rebind, rename, modifyAttributes) in a transaction will be
 * intercepted. Each modification has its corresponding
 * {@link CompensatingTransactionOperationRecorder}, which collects the
 * information necessary to perform a rollback and produces a
 * {@link CompensatingTransactionOperationExecutor} which is then used to
 * execute the actual operation and is later called for performing the commit or
 * rollback.
 * </p>
 * <p>
 * For several of the operations, performing a rollback is pretty
 * straightforward. For example, in order to roll back a rename operation, it
 * will only be required to rename the entry back to its original position. For
 * other operations, however, it's a bit more complicated. An unbind operation
 * is not possible to roll back by simply binding the entry back with the
 * attributes retrieved from the original entry. It might not be possible to get
 * all the information from the original entry. Consequently, the
 * {@link UnbindOperationExecutor} will move the original entry to a temporary
 * location in its performOperation() method. The commit() method will know that
 * everything went well, so it will be OK to unbind the entry. The rollback
 * operation will be to rename the entry back to its original location. The same
 * behaviour is used for rebind() operations. The operation of calculating a
 * temporary location for an entry is delegated to a
 * {@link TempEntryRenamingStrategy} (default
 * {@link DefaultTempEntryRenamingStrategy}), specified in
 * {@link #setRenamingStrategy(TempEntryRenamingStrategy)}.
 * </p>
 * <p>
 * The actual work of this Transaction Manager is delegated to a
 * {@link ContextSourceTransactionManagerDelegate}. This is because the exact
 * same logic needs to be used if we want to wrap a JDBC and LDAP transaction in
 * the same logical transaction.
 * </p>
 * 
 * @author Mattias Arthursson
 * 
 * @see ContextSourceAndDataSourceTransactionManager
 * @see ContextSourceTransactionManagerDelegate
 * @see DefaultCompensatingTransactionOperationManager
 * @see TempEntryRenamingStrategy
 * @see TransactionAwareContextSourceProxy
 * @since 1.2
 */
public class ContextSourceTransactionManager extends
        AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 7138208218687237856L;

    private ContextSourceTransactionManagerDelegate delegate = new ContextSourceTransactionManagerDelegate();

    /*
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object,
     *      org.springframework.transaction.TransactionDefinition)
     */
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        delegate.doBegin(transaction, definition);
    }

    /*
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        delegate.doCleanupAfterCompletion(transaction);
    }

    /*
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {
        delegate.doCommit(status);
    }

    /*
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
     */
    protected Object doGetTransaction() throws TransactionException {
        return delegate.doGetTransaction();
    }

    /*
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        delegate.doRollback(status);
    }

    /**
     * Get the ContextSource.
     * 
     * @return the contextSource.
     * @see ContextSourceTransactionManagerDelegate#getContextSource()
     */
    public ContextSource getContextSource() {
        return delegate.getContextSource();
    }

    /**
     * Set the ContextSource.
     * 
     * @param contextSource
     *            the ContextSource.
     * @see ContextSourceTransactionManagerDelegate#setContextSource(ContextSource)
     */
    public void setContextSource(ContextSource contextSource) {
        delegate.setContextSource(contextSource);
    }

    /**
     * Set the {@link TempEntryRenamingStrategy}.
     * 
     * @param renamingStrategy
     *            the Renaming Strategy.
     * @see ContextSourceTransactionManagerDelegate#setRenamingStrategy(TempEntryRenamingStrategy)
     */
    public void setRenamingStrategy(TempEntryRenamingStrategy renamingStrategy) {
        delegate.setRenamingStrategy(renamingStrategy);
    }
}
