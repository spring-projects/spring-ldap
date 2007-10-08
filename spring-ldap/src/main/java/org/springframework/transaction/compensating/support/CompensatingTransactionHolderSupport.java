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

import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Base class for compensating transaction resource holders.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public abstract class CompensatingTransactionHolderSupport extends
        ResourceHolderSupport {

    private CompensatingTransactionOperationManager transactionOperationManager;

    /**
     * Constructor.
     * 
     * @param manager
     *            The {@link CompensatingTransactionOperationManager} to use for
     *            creating Compensating operations.
     */
    public CompensatingTransactionHolderSupport(
            CompensatingTransactionOperationManager manager) {
        this.transactionOperationManager = manager;
    }

    /**
     * Get the actual transacted resource.
     * 
     * @return the transaction's target resource
     */
    protected abstract Object getTransactedResource();

    /*
     * @see org.springframework.transaction.support.ResourceHolderSupport#clear()
     */
    public void clear() {
        super.clear();
        transactionOperationManager = null;
    }

    /**
     * Get the CompensatingTransactionOperationManager to handle the data for
     * the current transaction.
     * 
     * @return the CompensatingTransactionOperationManager.
     */
    public CompensatingTransactionOperationManager getTransactionOperationManager() {
        return transactionOperationManager;
    }

    /**
     * Set the CompensatingTransactionOperationManager. For testing purposes
     * only.
     * 
     * @param transactionOperationManager
     *            the CompensatingTransactionOperationManager to use.
     */
    public void setTransactionOperationManager(
            CompensatingTransactionOperationManager transactionOperationManager) {
        this.transactionOperationManager = transactionOperationManager;
    }
}