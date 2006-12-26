package org.springframework.ldap.support.transaction;

/**
 * Transaction object for ContextSourceTransactionManager.
 * 
 * @author Mattias Arthursson
 */
public class ContextSourceTransactionObject {
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