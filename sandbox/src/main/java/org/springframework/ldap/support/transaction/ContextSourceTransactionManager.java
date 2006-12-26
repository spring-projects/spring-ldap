package org.springframework.ldap.support.transaction;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.ContextSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TransactionManager for managing LDAP transactions. Since transactions are not
 * supported in the LDAP protocol, this class and its collaborators aims to
 * provide compensating transactions instead. TODO: improve javadoc.
 * 
 * @author Mattias Arthursson
 */
public class ContextSourceTransactionManager extends
        AbstractPlatformTransactionManager {

    private ContextSource contextSource;

    /**
     * Set the ContextSource to work on. The supplied ContextSource must be of the type abstract
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
        // Perform compensating transaction cleanup using information stored in
        // ContextHolder.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;
        TransactionSynchronizationManager.unbindResource(contextSource);
        DirContext ctx = txObject.getContextHolder().getCtx();

        try {
            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }

        txObject.getContextHolder().clear();
    }

    /**
     * Transaction object for ContextSourceTransactionManager.
     * 
     * @author Mattias Arthursson
     */
    static class ContextSourceTransactionObject {
        private DirContextHolder contextHolder;

        public ContextSourceTransactionObject(DirContextHolder contextHolder) {
            this.contextHolder = contextHolder;
        }

        public DirContextHolder getContextHolder() {
            return contextHolder;
        }

        public void setContextHolder(DirContextHolder contextHolder) {
            this.contextHolder = contextHolder;
        }
    }

}
