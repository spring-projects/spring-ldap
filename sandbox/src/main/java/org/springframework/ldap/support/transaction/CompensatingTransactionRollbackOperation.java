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
 * Responsible for rolling back a single operation, restoring the state of the
 * target as it was before the operation was performed. Instances of this
 * interface are constructed by
 * {@link CompensatingTransactionRecordingOperation} objects, supplying them
 * with the information necessary for rollback.
 * 
 * @author Mattias Arthursson
 */
public interface CompensatingTransactionRollbackOperation {
    /**
     * Rollback the operation, restoring state of the target as it was before
     * the operation was performed using the information supplied on creation of
     * this instance (supplied by a
     * {@link CompensatingTransactionRecordingOperation}).
     */
    public void rollback();
    
    public void commit();
    
    public void performOperation();
}
