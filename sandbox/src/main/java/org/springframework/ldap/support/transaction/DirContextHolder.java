package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

import org.springframework.transaction.support.ResourceHolderSupport;

public class DirContextHolder extends ResourceHolderSupport {
    private DirContext ctx;

    private CompensatingTransactionDataManager transactionDataManager;
    
    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
    }

    public DirContextHolder(DirContext ctx) {
        this.ctx = ctx;
    }

    public DirContext getCtx() {
        return ctx;
    }
    
    public void clear() {
        super.clear();
        
    }
}
