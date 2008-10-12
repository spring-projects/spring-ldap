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

import org.springframework.transaction.compensating.support.DefaultCompensatingTransactionOperationManager;

/**
 * Factory interface for creating
 * {@link CompensatingTransactionOperationRecorder} objects based on operation
 * method names.
 * 
 * @author Mattias Arthursson
 * @see DefaultCompensatingTransactionOperationManager
 * @since 1.2
 */
public interface CompensatingTransactionOperationFactory {
    /**
     * Create an appropriate {@link CompensatingTransactionOperationRecorder}
     * instance corresponding to the supplied method name.
     * 
     * @param resource
     *            The target transaction resource.
     * @param method
     *            the method name to create a
     *            {@link CompensatingTransactionOperationRecorder} for.
     * 
     * @return a new {@link CompensatingTransactionOperationRecorder} instance.
     */
    public CompensatingTransactionOperationRecorder createRecordingOperation(
            Object resource, String method);
}
