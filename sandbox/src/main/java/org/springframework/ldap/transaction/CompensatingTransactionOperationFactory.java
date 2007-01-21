package org.springframework.ldap.transaction;

public interface CompensatingTransactionOperationFactory {
    public CompensatingTransactionOperationRecorder createRecordingOperation(
            String method);
}
