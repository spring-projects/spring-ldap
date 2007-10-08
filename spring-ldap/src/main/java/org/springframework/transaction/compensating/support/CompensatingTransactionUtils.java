/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.compensating.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Common methods for use with compensating transactions.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class CompensatingTransactionUtils {

    /**
     * Not to be instantiated.
     */
    private CompensatingTransactionUtils() {
    }

    /**
     * Perform the specified operation, storing the state prior to the operation
     * in order to enable commit/rollback later. If no transaction is currently
     * active, proceed with the original call on the target.
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
        CompensatingTransactionHolderSupport transactionResourceHolder = (CompensatingTransactionHolderSupport) TransactionSynchronizationManager
                .getResource(synchronizationKey);
        if (transactionResourceHolder != null) {

            CompensatingTransactionOperationManager transactionOperationManager = transactionResourceHolder
                    .getTransactionOperationManager();
            transactionOperationManager.performOperation(
                    transactionResourceHolder.getTransactedResource(), method
                            .getName(), args);
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
