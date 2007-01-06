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
 * {@link CompensatingTransactionDataManager}, responsible for keeping track of
 * all changes and storing compensating rollback operations, should the
 * transaction need to be rolled back.
 * 
 * @author Mattias Arthursson
 * 
 */
public class DirContextHolder extends ResourceHolderSupport {
    private DirContext ctx;

    private CompensatingTransactionDataManager transactionDataManager;

    private CompensatingTransactionOperationFactory operationFactory;

    /**
     * Constructor.
     * 
     * @param ctx
     *            The DirContext associated with the current transaction.
     */
    public DirContextHolder(DirContext ctx) {
        this.ctx = ctx;
        this.transactionDataManager = new DefaultCompensatingTransactionDataManager();
        this.operationFactory = new LdapCompensatingTransactionOperationFactory(
                ctx);
    }

    /**
     * Set the DirContext associated with the current transaction.
     * 
     * @param ctx
     *            the DirContext associated with the current transaction.
     */
    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
        this.transactionDataManager = new DefaultCompensatingTransactionDataManager();
        this.operationFactory = new LdapCompensatingTransactionOperationFactory(
                ctx);
    }

    /**
     * Get the DirContext associated with the current transaction.
     * 
     * @return
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
     * Get the CompensatingTransactionDataManager to handle the data for the
     * current transaction.
     * 
     * @return the CompensatingTransactionDataManager.
     */
    public CompensatingTransactionDataManager getTransactionDataManager() {
        return transactionDataManager;
    }

    /**
     * Set the CompensatingTransactionDataManager. For testing purposes only.
     * 
     * @param transactionDataManager
     *            the CompensatingTransactionDataManager to use.
     */
    void setTransactionDataManager(
            CompensatingTransactionDataManager transactionDataManager) {
        this.transactionDataManager = transactionDataManager;
    }

    public CompensatingTransactionOperationFactory getOperationFactory() {
        return operationFactory;
    }
}
