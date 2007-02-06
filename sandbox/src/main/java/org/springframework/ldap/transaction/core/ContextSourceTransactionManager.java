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
package org.springframework.ldap.transaction.core;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.CompensatingTransactionOperationExecutor;
import org.springframework.ldap.transaction.CompensatingTransactionOperationRecorder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TransactionManager for managing LDAP transactions. Since transactions are not
 * supported in the LDAP protocol this class and its collaborators aims to
 * provide compensating transactions instead, i.e. should a transaction need to
 * be rolled back this TransactionManager will try to restore the original
 * original state using information recorded prior to each operation.
 * <p>
 * <b>NOTE:</b> The transactions provided by this TransactionManager are all
 * <i>client side</i> and are by no means 'real' transactions, in the sense
 * that we know them in the ordinary database world, e.g.:
 * <ul>
 * <li>Should the transaction failure be caused by a network failure there is
 * no way whatsoever that this TransactionManager can restore the database
 * state. In this case all possibilities for rollback will be utterly lost.</li>
 * <li>Transaction isolation is not provided, i.e. entries participating in a
 * transaction for one client may very well participate in another transaction
 * for another client at the same time. Should one of these transaction be
 * rolled back, the outcome of this is undetermined, and may in the worst case
 * result in total failure.</li>
 * </ul>
 * </p>
 * <p>
 * While the points above should be noted and considered, the compensating
 * transaction approach will be perfectly sufficient for all but the most
 * unfortunate of circumstances, particularly considering the total absence of
 * transaction support which is normally the case working against LDAP servers.
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
 * {@link TransactionAwareContextSourceProxy} all modifying operations (bind,
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
 * straightforward. E.g. in order to roll back a rename operation it will only
 * be required to rename the entry back to its original position. For other
 * operations however, it's a bit more complicated. E.g. an unbind operation is
 * not possible to roll back by simply binding the entry back with the
 * attributes retrieved from the original entry. This is because it might not be
 * possible to get all the information from the original entry. Consequently,
 * the {@link UnbindOperationExecutor} will move the original entry to a
 * temporary location in its performOperation() method. In the commit() method
 * we already know that everything went well, so we're free to unbind the entry,
 * but the rollback operation will be to rename the entry back to its original
 * location. The same behaviour is used for rebind() operations. The operation
 * of calculating a temporary location for an entry is delegated to a
 * {@link TempEntryRenamingStrategy} (default
 * {@link DefaultTempEntryRenamingStrategy}), specified in
 * {@link #setRenamingStrategy(TempEntryRenamingStrategy)}.
 * </p>
 * 
 * @author Mattias Arthursson
 */
public class ContextSourceTransactionManager extends
        AbstractPlatformTransactionManager {

    private static final long serialVersionUID = -4308820955185446535L;

    private static Log log = LogFactory
            .getLog(ContextSourceTransactionManager.class);

    private ContextSource contextSource;

    private TempEntryRenamingStrategy renamingStrategy = new DefaultTempEntryRenamingStrategy();

    /**
     * Set the ContextSource to work on. Even though the actual ContextSource
     * sent to the LdapTemplate instance should be a
     * {@link TransactionAwareContextSourceProxy}, the one sent to this method
     * should be the target of that proxy. If it is not, the target will be
     * extracted and used instead.
     * 
     * @param contextSource
     *            the ContextSource to work on.
     */
    public void setContextSource(ContextSource contextSource) {
        if (contextSource instanceof TransactionAwareContextSourceProxy) {
            TransactionAwareContextSourceProxy proxy = (TransactionAwareContextSourceProxy) contextSource;
            this.contextSource = proxy.getTarget();
        } else {
            this.contextSource = contextSource;
        }
    }

    public ContextSource getContextSource() {
        return contextSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
     */
    protected Object doGetTransaction() throws TransactionException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(this.contextSource);
        ContextSourceTransactionObject txObject = new ContextSourceTransactionObject(
                contextHolder);
        return txObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object,
     *      org.springframework.transaction.TransactionDefinition)
     */
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;

        if (txObject.getContextHolder() == null) {
            DirContext newCtx = getContextSource().getReadOnlyContext();
            DirContextHolder contextHolder = new DirContextHolder(newCtx,
                    renamingStrategy);

            txObject.setContextHolder(contextHolder);
            TransactionSynchronizationManager.bindResource(getContextSource(),
                    contextHolder);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) status
                .getTransaction();
        txObject.getContextHolder().getTransactionDataManager().commit();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) status
                .getTransaction();
        txObject.getContextHolder().getTransactionDataManager().rollback();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        log.debug("Cleaning stored ContextHolder");
        TransactionSynchronizationManager.unbindResource(contextSource);

        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;
        DirContext ctx = txObject.getContextHolder().getCtx();

        try {
            log.debug("Closing target context");
            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }

        txObject.getContextHolder().clear();
    }

    /**
     * Set the {@link TempEntryRenamingStrategy} to be used when renaming
     * temporary entries in unbind and rebind operations. Default value is a
     * {@link DefaultTempEntryRenamingStrategy}.
     * 
     * @param renamingStrategy
     *            the {@link TempEntryRenamingStrategy} to use.
     */
    public void setRenamingStrategy(TempEntryRenamingStrategy renamingStrategy) {
        this.renamingStrategy = renamingStrategy;
    }
}
