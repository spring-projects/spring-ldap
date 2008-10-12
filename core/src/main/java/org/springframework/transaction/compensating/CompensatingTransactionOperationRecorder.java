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
 * An implementation of this interface is responsible for recording data and
 * supplying a {@link CompensatingTransactionOperationExecutor} to be invoked
 * for execution and compensating transaction management of the operation.
 * Recording of an operation should not fail (throwing an Exception), but
 * instead log the result.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public interface CompensatingTransactionOperationRecorder {
    /**
     * Record information about the operation performed and return a
     * corresponding {@link CompensatingTransactionOperationExecutor} to be used
     * if the operation would need to be rolled back.
     * 
     * @param args
     *            The arguments that have been sent to the operation.
     * @return A {@link CompensatingTransactionOperationExecutor} to be used if
     *         the recorded operation should need to be rolled back.
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args);
}
