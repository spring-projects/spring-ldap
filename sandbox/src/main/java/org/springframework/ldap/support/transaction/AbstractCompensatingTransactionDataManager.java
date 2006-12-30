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
package org.springframework.ldap.support.transaction;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract superclass for {@link CompensatingTransactionDataManager}
 * implementations. Keeps track of the basic plumbing and defines the
 * {@link #getRecordingOperation(String)} method for subclasses to implement.
 * 
 * @author Mattias Arthursson
 */
public abstract class AbstractCompensatingTransactionDataManager implements
        CompensatingTransactionDataManager {

    private static Log log = LogFactory
            .getLog(AbstractCompensatingTransactionDataManager.class);

    private Stack rollbackOperations = new Stack();

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionDataManager#operationPerformed(java.lang.String,
     *      java.lang.Object[])
     */
    public void operationPerformed(String operation, Object[] params) {
        CompensatingTransactionRecordingOperation recordingOperation = getRecordingOperation(operation);
        rollbackOperations.push(recordingOperation.performOperation(params));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionDataManager#rollback()
     */
    public void rollback() {
        log.debug("Performing rollback");
        while (!rollbackOperations.isEmpty()) {
            CompensatingTransactionRollbackOperation rollbackOperation = (CompensatingTransactionRollbackOperation) rollbackOperations
                    .pop();
            rollbackOperation.rollback();
        }
    }

    /**
     * Get a {@link CompensatingTransactionRecordingOperation} to record the
     * operation corresponding to the supplied operation.
     * 
     * @param operation
     *            the operation (method) to be invoked.
     * @return a CompensatingTransactionRecordingOperation for handling the
     *         supplied operation.
     */
    protected abstract CompensatingTransactionRecordingOperation getRecordingOperation(
            String operation);

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

}
