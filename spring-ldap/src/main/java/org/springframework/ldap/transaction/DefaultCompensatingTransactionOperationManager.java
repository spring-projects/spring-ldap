/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of {@link CompensatingTransactionOperationManager}.
 * Manages a stack of {@link CompensatingTransactionOperationExecutor} objects
 * and manages rollback of these in the reverse order.
 * 
 * @author Mattias Arthursson
 */
public class DefaultCompensatingTransactionOperationManager implements
        CompensatingTransactionOperationManager {

    private static Log log = LogFactory
            .getLog(DefaultCompensatingTransactionOperationManager.class);

    private Stack rollbackOperations = new Stack();

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
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationManager#operationPerformed(java.lang.String,
     *      java.lang.Object[])
     */
    public void performOperation(String operation, Object[] args) {
        CompensatingTransactionOperationRecorder recorder = operationFactory
                .createRecordingOperation(operation);
        CompensatingTransactionOperationExecutor executor = recorder
                .recordOperation(args);

        executor.performOperation();

        // Don't push the executor until the actual operation passed.
        rollbackOperations.push(executor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationManager#rollback()
     */
    public void rollback() {
        log.debug("Performing rollback");
        while (!rollbackOperations.isEmpty()) {
            CompensatingTransactionOperationExecutor rollbackOperation = (CompensatingTransactionOperationExecutor) rollbackOperations
                    .pop();
            rollbackOperation.rollback();
        }
    }

    /**
     * Get the rollback operations. Package protected for testing purposes.
     * 
     * @return the rollback operations.
     */
    protected Stack getRollbackOperations() {
        return rollbackOperations;
    }

    /**
     * Set the rollback operations. Package protected - for testing purposes
     * only.
     * 
     * @param rollbackOperations
     *            the rollback operations.
     */
    void setRollbackOperations(Stack rollbackOperations) {
        this.rollbackOperations = rollbackOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationManager#commit()
     */
    public void commit() {
        log.debug("Performing rollback");
        // TODO: Should this really be done in reverse order?
        while (!rollbackOperations.isEmpty()) {
            CompensatingTransactionOperationExecutor rollbackOperation = (CompensatingTransactionOperationExecutor) rollbackOperations
                    .pop();
            rollbackOperation.commit();
        }
    }

}
