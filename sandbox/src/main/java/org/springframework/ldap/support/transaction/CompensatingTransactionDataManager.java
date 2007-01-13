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

/**
 * A CompensatingTransactionDataManager implementation records operations that
 * are performed in a transaction and keeps track of compensating actions
 * necessary for rolling back each individual operation.
 * 
 * @author Mattias Arthursson
 * 
 */
public interface CompensatingTransactionDataManager {
    /**
     * Indicates that the supplied operation (method name) has been performed
     * and that the supplied {@link CompensatingTransactionRollbackOperation}
     * should be stored for possible rollback. This method is called after the
     * the actual invocation of the target method.
     * 
     * @param operation
     *            the method to be invoked.
     */
    public void operationPerformed(
            CompensatingTransactionRollbackOperation operation);

    /**
     * Rollback all recorded operations, by performing each of the recorded
     * rollback operations.
     */
    public void rollback();

    /**
     * Commit all recorded operations. In many cases this means doing nothing,
     * but in some cases some temporary data will need to be removed.
     */
    public void commit();
}
