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
package org.springframework.transaction.compensating;

/**
 * A CompensatingTransactionOperationManager implementation records and performs
 * operations that are to be performed within a compensating transaction. It
 * keeps track of compensating actions necessary for rolling back each
 * individual operation.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public interface CompensatingTransactionOperationManager {
    /**
     * Indicates that the supplied operation (method name) is to be performed.
     * This method is responsible for recording the current state (prior to the
     * operation), performing the operation, and storing the necessary
     * information to roll back or commit the performed operation.
     * 
     * @param resource
     *            the target resource to perform the operation on.
     * @param operation
     *            The method to be invoked.
     * @param args
     *            Arguments supplied to the method.
     */
    public void performOperation(Object resource, String operation,
            Object[] args);

    /**
     * Rollback all recorded operations by performing each of the recorded
     * rollback operations.
     */
    public void rollback();

    /**
     * Commit all recorded operations. In many cases this means doing nothing,
     * but in some cases some temporary data will need to be removed.
     */
    public void commit();
}
