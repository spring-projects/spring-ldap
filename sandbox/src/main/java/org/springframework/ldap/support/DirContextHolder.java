package org.springframework.ldap.support;

import javax.naming.directory.DirContext;

import org.springframework.transaction.support.ResourceHolderSupport;

public class DirContextHolder extends ResourceHolderSupport {
    private DirContext ctx;

    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
    }

    public DirContextHolder(DirContext ctx) {
        this.ctx = ctx;
    }

    public DirContext getCtx() {
        return ctx;
    }
}
