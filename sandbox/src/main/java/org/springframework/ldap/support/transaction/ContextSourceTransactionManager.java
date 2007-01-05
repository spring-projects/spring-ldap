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
package org.springframework.ldap.support.transaction;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TransactionManager for managing LDAP transactions. Since transactions are not
 * supported in the LDAP protocol this class and its collaborators aims to
 * provide compensating transactions instead.
 * <p>
 * A transaction is tied to a {@link ContextSource}, to be supplied to the
 * {@link #setContextSource(ContextSource)} method. While the actual
 * ContextSource used by the target LdapTemplate instance needs to be of the
 * type {@link TransactionAwareContextSourceProxy}, the ContextSource supplied
 * to this class should be the actual target ContextSource.
 * </p>
 * <p>
 * This class creates a {@link ContextSourceTransactionObject} as the
 * implementation specific Transaction object. The actual transaction data is
 * managed by a {@link DirContextHolder} and its collaborating
 * {@link LdapCompensatingTransactionDataManager}. Using a
 * {@link TransactionAwareContextSourceProxy} all modify operations (bind,
 * rebind, modifyAttributes, unbind) will result in corresponding rollback
 * operations to be recorded and these operations will be invoked should the
 * transaction be rolled back.
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

    /**
     * Set the ContextSource to work on. The supplied ContextSource must be of
     * the type abstract
     * 
     * @param contextSource
     *            the ContextSource to work on.
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
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
            DirContextHolder contextHolder = new DirContextHolder(newCtx);

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
        // Nothing much to do here.
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
}
