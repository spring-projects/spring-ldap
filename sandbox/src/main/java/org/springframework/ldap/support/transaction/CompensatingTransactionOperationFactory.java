package org.springframework.ldap.support.transaction;

public interface CompensatingTransactionOperationFactory {
    public CompensatingTransactionRecordingOperation createRecordingOperation(
            String method);
}
