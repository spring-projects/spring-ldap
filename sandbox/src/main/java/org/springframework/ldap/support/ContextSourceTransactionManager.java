package org.springframework.ldap.support;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.ContextSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ContextSourceTransactionManager extends
        AbstractPlatformTransactionManager {

    private ContextSource contextSource;

    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public ContextSource getContextSource() {
        return contextSource;
    }

    protected Object doGetTransaction() throws TransactionException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(this.contextSource);
        ContextSourceTransactionObject txObject = new ContextSourceTransactionObject(
                contextHolder);
        return txObject;
    }

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

    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {
        // Nothing much to do here.
    }

    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        // Perform compensating transaction cleanup using information stored in
        // ContextHolder.
    }

    protected void doCleanupAfterCompletion(Object transaction) {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;
        TransactionSynchronizationManager.unbindResource(contextSource);
        DirContext ctx = txObject.getContextHolder().getCtx();

        try {
            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

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
