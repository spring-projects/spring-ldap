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

import javax.naming.directory.DirContext;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Keeps track of the transaction DirContext. The same DirContext instance will
 * be reused throughout a transaction. Also keeps a
 * {@link CompensatingTransactionOperationManager}, responsible for performing
 * operations and keeping track of all changes and storing information necessary
 * for commit or rollback.
 * 
 * @author Mattias Arthursson
 * 
 */
public class DirContextHolder extends ResourceHolderSupport {
    private DirContext ctx;

    private CompensatingTransactionOperationManager transactionDataManager;

    private CompensatingTransactionOperationFactory operationFactory;

    private TempEntryRenamingStrategy renamingStrategy;

    /**
     * Constructor.
     * 
     * @param ctx
     *            The DirContext associated with the current transaction.
     */
    public DirContextHolder(DirContext ctx,
            TempEntryRenamingStrategy renamingStrategy) {
        this.ctx = ctx;
        this.renamingStrategy = renamingStrategy;
        this.transactionDataManager = new DefaultCompensatingTransactionOperationManager(
                createOperationFactory());
    }

    /**
     * Set the DirContext associated with the current transaction.
     * 
     * @param ctx
     *            The DirContext associated with the current transaction.
     */
    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
        this.transactionDataManager = new DefaultCompensatingTransactionOperationManager(
                createOperationFactory());
    }

    /**
     * Factory method to create a
     * {@link CompensatingTransactionOperationFactory} using the settings and
     * current state of this object.
     * 
     * @return a new {@link LdapCompensatingTransactionOperationFactory}
     *         referncing the current transaction context.
     */
    private CompensatingTransactionOperationFactory createOperationFactory() {
        return new LdapCompensatingTransactionOperationFactory(ctx,
                renamingStrategy);
    }

    /**
     * Return the DirContext associated with the current transaction.
     */
    public DirContext getCtx() {
        return ctx;
    }

    public void clear() {
        super.clear();
        transactionDataManager = null;
        operationFactory = null;
    }

    /**
     * Get the CompensatingTransactionOperationManager to handle the data for
     * the current transaction.
     * 
     * @return the CompensatingTransactionOperationManager.
     */
    public CompensatingTransactionOperationManager getTransactionDataManager() {
        return transactionDataManager;
    }

    /**
     * Set the CompensatingTransactionOperationManager. For testing purposes
     * only.
     * 
     * @param transactionDataManager
     *            the CompensatingTransactionOperationManager to use.
     */
    void setTransactionDataManager(
            CompensatingTransactionOperationManager transactionDataManager) {
        this.transactionDataManager = transactionDataManager;
    }

    public CompensatingTransactionOperationFactory getOperationFactory() {
        return operationFactory;
    }

    public TempEntryRenamingStrategy getRenamingStrategy() {
        return renamingStrategy;
    }
}
