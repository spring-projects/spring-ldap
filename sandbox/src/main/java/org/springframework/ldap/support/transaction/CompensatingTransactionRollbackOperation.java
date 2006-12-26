package org.springframework.ldap.support.transaction;

public interface CompensatingTransactionRollbackOperation {
    public void rollback();
}
