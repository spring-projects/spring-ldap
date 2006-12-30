package org.springframework.ldap.support.transaction;

/**
 * An implementation of this interface is responsible for recording data and
 * supplying a {@link CompensatingTransactionRollbackOperation} to be invoked
 * should the operation need to be rolled back. Recording of an operation should
 * not fail (throwing an Exception), but rather log the result.
 * 
 * @author Mattias Arthursson
 */
public interface CompensatingTransactionRecordingOperation {
    /**
     * Record information about the operation performed and return a
     * corresponding {@link CompensatingTransactionRollbackOperation} to be used
     * if the operation would need to be rolled back.
     * 
     * @param args
     *            The arguments that have been sent to the operation.
     * @return A {@link CompensatingTransactionRollbackOperation} to be used if
     *         the recorded operation should need to be rolled back.
     */
    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args);
}
