package org.springframework.transaction.compensating.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Common methods for use with compensating transactions.
 * 
 * @author Mattias Arthursson
 */
public class CompensatingTransactionUtils {

    /**
     * Not to be instantiated.
     */
    private CompensatingTransactionUtils() {
    }

    /**
     * Perform the specified operation, storing the state prior to the
     * operation, to enable commit/rollback later. If no transaction is
     * currently active, proceed with the original call on the target.
     * 
     * @param synchronizationKey
     *            the transaction synchronization key we are operating on
     *            (typically something similar to a DataSource).
     * @param target
     *            the actual target resource that should be used for invoking
     *            the operation on should no transaction be active.
     * @param method
     *            name of the method to be invoked.
     * @param args
     *            arguments with which the operation is invoked.
     */
    public static void performOperation(Object synchronizationKey,
            Object target, Method method, Object[] args) throws Throwable {
        CompensatingTransactionHolderSupport transactionContextHolder = (CompensatingTransactionHolderSupport) TransactionSynchronizationManager
                .getResource(synchronizationKey);
        if (transactionContextHolder != null) {

            CompensatingTransactionOperationManager transactionDataManager = transactionContextHolder
                    .getTransactionOperationManager();
            transactionDataManager.performOperation(method.getName(), args);
        } else {
            // Perform the target operation
            try {
                method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

}
