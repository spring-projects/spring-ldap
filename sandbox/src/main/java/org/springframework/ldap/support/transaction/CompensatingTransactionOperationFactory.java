package org.springframework.ldap.support.transaction;

public interface CompensatingTransactionOperationFactory {
    public CompensatingTransactionOperationRecorder createRecordingOperation(
            String method);
}
