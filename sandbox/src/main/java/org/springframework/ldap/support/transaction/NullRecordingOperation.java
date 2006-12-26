package org.springframework.ldap.support.transaction;

public class NullRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        return new NullRollbackOperation();
    }

}
