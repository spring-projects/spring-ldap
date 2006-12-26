package org.springframework.ldap.support.transaction;

public interface CompensatingTransactionRecordingOperation {
    public CompensatingTransactionRollbackOperation performOperation(Object[] args);
}
