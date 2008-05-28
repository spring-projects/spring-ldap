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
package org.springframework.ldap.transaction.compensating.manager;

import javax.naming.directory.DirContext;

import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;

/**
 * Keeps track of the transaction DirContext. The same DirContext instance will
 * be reused throughout a transaction. Also keeps a
 * {@link CompensatingTransactionOperationManager}, responsible for performing
 * operations and keeping track of all changes and storing information necessary
 * for commit or rollback.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class DirContextHolder extends CompensatingTransactionHolderSupport {
    private DirContext ctx;

    /**
     * Constructor.
     * 
     * @param manager
     *            The {@link CompensatingTransactionOperationManager}.
     * @param ctx
     *            The DirContext associated with the current transaction.
     */
    public DirContextHolder(CompensatingTransactionOperationManager manager,
            DirContext ctx) {
        super(manager);
        this.ctx = ctx;
    }

    /**
     * Set the DirContext associated with the current transaction.
     * 
     * @param ctx
     *            The DirContext associated with the current transaction.
     */
    public void setCtx(DirContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Return the DirContext associated with the current transaction.
     */
    public DirContext getCtx() {
        return ctx;
    }

    /*
     * @see org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport#getTransactedResource()
     */
    protected Object getTransactedResource() {
        return ctx;
    }
}
