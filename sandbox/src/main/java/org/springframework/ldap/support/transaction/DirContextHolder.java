package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

import org.springframework.transaction.support.ResourceHolderSupport;

public class DirContextHolder extends ResourceHolderSupport {
    private DirContext ctx;

    private CompensatingTransactionDataManager transactionDataManager;

    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
        this.transactionDataManager = new LdapCompensatingTransactionDataManager(
                ctx);
    }

    public DirContextHolder(DirContext ctx) {
        this.ctx = ctx;
        this.transactionDataManager = new LdapCompensatingTransactionDataManager(
                ctx);
    }

    public DirContext getCtx() {
        return ctx;
    }

    public void clear() {
        super.clear();
        transactionDataManager = null;
    }

    /**
     * Get the CompensatingTransactionDataManager to handle the data for the
     * current transaction.
     * 
     * @return the CompensatingTransactionDataManager.
     */
    public CompensatingTransactionDataManager getTransactionDataManager() {
        return transactionDataManager;
    }

    /**
     * Set the CompensatingTransactionDataManager. For testing purposes only.
     * 
     * @param transactionDataManager
     *            the CompensatingTransactionDataManager to use.
     */
    void setTransactionDataManager(
            CompensatingTransactionDataManager transactionDataManager) {
        this.transactionDataManager = transactionDataManager;
    }
}
