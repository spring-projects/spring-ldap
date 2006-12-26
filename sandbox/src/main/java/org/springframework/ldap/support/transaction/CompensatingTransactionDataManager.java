package org.springframework.ldap.support.transaction;

public interface CompensatingTransactionDataManager {
    public void operationPerformed(String operation, Object[] params);
    public void rollback();
}
