package org.springframework.ldap.support.transaction;

/**
 * An implementation of this interface is responsible for recording data and
 * supplying a {@link CompensatingTransactionOperationExecutor} to be invoked
 * for execution and compensating transaction management of the operation.
 * Recording of an operation should not fail (throwing an Exception), but rather
 * log the result.
 * 
 * @author Mattias Arthursson
 */
public interface CompensatingTransactionOperationRecorder {
    /**
     * Record information about the operation performed and return a
     * corresponding {@link CompensatingTransactionOperationExecutor} to be used
     * if the operation would need to be rolled back.
     * 
     * @param args
     *            The arguments that have been sent to the operation.
     * @return A {@link CompensatingTransactionOperationExecutor} to be used if
     *         the recorded operation should need to be rolled back.
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args);
}
