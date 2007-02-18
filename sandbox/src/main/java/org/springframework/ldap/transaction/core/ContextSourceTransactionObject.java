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
package org.springframework.ldap.transaction.core;

/**
 * Transaction object for ContextSourceTransactionManager. Keeps a reference to
 * the {@link DirContextHolder} associated with the current transaction.
 * 
 * @author Mattias Arthursson
 */
public class ContextSourceTransactionObject {
    private DirContextHolder contextHolder;

    /**
     * Constructor.
     * 
     * @param contextHolder
     *            the DirContextHolder associated with the current transaction.
     */
    public ContextSourceTransactionObject(DirContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    /**
     * Get the DirContextHolder.
     * 
     * @return the DirContextHolder.
     */
    public DirContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * Set the DirContextHolder associated with the current transaction.s
     * 
     * @param contextHolder
     *            the DirContextHolder associated with the current transaction.
     */
    public void setContextHolder(DirContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }
}