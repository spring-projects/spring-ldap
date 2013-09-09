/*
 * Copyright 2005-2013 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

import java.util.Stack;

/**
 * Default implementation of {@link CompensatingTransactionOperationManager}.
 * Manages a stack of {@link CompensatingTransactionOperationExecutor} objects
 * and performs rollback of these in the reverse order.
 * 
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class DefaultCompensatingTransactionOperationManager implements
        CompensatingTransactionOperationManager {

    private static Log log = LogFactory
            .getLog(DefaultCompensatingTransactionOperationManager.class);

    private Stack<CompensatingTransactionOperationExecutor> operationExecutors =
            new Stack<CompensatingTransactionOperationExecutor>();

    private CompensatingTransactionOperationFactory operationFactory;

    /**
     * Set the {@link CompensatingTransactionOperationFactory} to use.
     * 
     * @param operationFactory
     *            the {@link CompensatingTransactionOperationFactory}.
     */
    public DefaultCompensatingTransactionOperationManager(
            CompensatingTransactionOperationFactory operationFactory) {
        this.operationFactory = operationFactory;
    }

    /*
     * @see org.springframework.transaction.compensating.CompensatingTransactionOperationManager#performOperation(java.lang.Object,
     *      java.lang.String, java.lang.Object[])
     */
    public void performOperation(Object resource, String operation,
            Object[] args) {
        CompensatingTransactionOperationRecorder recorder = operationFactory
                .createRecordingOperation(resource, operation);
        CompensatingTransactionOperationExecutor executor = recorder
                .recordOperation(args);

        executor.performOperation();

        // Don't push the executor until the actual operation passed.
        operationExecutors.push(executor);
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationManager#rollback()
     */
    public void rollback() {
        log.debug("Performing rollback");
        while (!operationExecutors.isEmpty()) {
            CompensatingTransactionOperationExecutor rollbackOperation = operationExecutors.pop();
            try {
                rollbackOperation.rollback();
            } catch (Exception e) {
                throw new TransactionSystemException(
                        "Error occurred during rollback", e);
            }
        }
    }

    /**
     * Get the rollback operations. Used for testing purposes.
     * 
     * @return the rollback operations.
     */
    protected Stack<CompensatingTransactionOperationExecutor> getOperationExecutors() {
        return operationExecutors;
    }

    /**
     * Set the rollback operations. Package protected - for testing purposes
     * only.
     * 
     * @param operationExecutors
     *            the rollback operations.
     */
    void setOperationExecutors(Stack<CompensatingTransactionOperationExecutor> operationExecutors) {
        this.operationExecutors = operationExecutors;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationManager#commit()
     */
    public void commit() {
        log.debug("Performing commit");
        for (CompensatingTransactionOperationExecutor operationExecutor : operationExecutors) {
            try {
                operationExecutor.commit();
            } catch (Exception e) {
                throw new TransactionSystemException(
                        "Error occurred during commit", e);
            }
        }
    }
}
