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

/**
 * Transaction object used by
 * {@link AbstractCompensatingTransactionManagerDelegate}. Keeps a reference to
 * the {@link CompensatingTransactionHolderSupport} associated with the current
 * transaction.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class CompensatingTransactionObject {
    private CompensatingTransactionHolderSupport holder;

    /**
     * Constructor.
     * 
     * @param holder
     *            the {@link CompensatingTransactionHolderSupport} associated
     *            with the current transaction.
     */
    public CompensatingTransactionObject(
            CompensatingTransactionHolderSupport holder) {
        this.holder = holder;
    }

    /**
     * Get the DirContextHolder.
     * 
     * @return the DirContextHolder.
     */
    public CompensatingTransactionHolderSupport getHolder() {
        return holder;
    }

    /**
     * Set the {@link CompensatingTransactionHolderSupport} associated with the
     * current transaction.
     * 
     * @param holder
     *            the {@link CompensatingTransactionHolderSupport} associated
     *            with the current transaction.
     */
    public void setHolder(CompensatingTransactionHolderSupport holder) {
        this.holder = holder;
    }
}