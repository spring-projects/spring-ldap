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
package org.springframework.transaction.compensating.support;

import org.springframework.ldap.transaction.compensating.LdapCompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
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
     * This method creates a new TransactionOperationManager instance. To be
     * called by subclass in case the target object has changed.
     */
    protected void refreshTransactionOperationManager() {
        setTransactionOperationManager(new DefaultCompensatingTransactionOperationManager(
                createOperationFactory()));
    }

    /**
     * Factory method to create a
     * {@link CompensatingTransactionOperationFactory} using the settings and
     * current state of this object.
     * 
     * @return a new {@link LdapCompensatingTransactionOperationFactory}
     *         referncing the current transaction context.
     */
    protected abstract CompensatingTransactionOperationFactory createOperationFactory();

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