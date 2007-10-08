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
 * Responsible for executing a single recorded operation as well as committing
 * or rolling it back, depending on the transaction outcome. Instances of this
 * interface are constructed by {@link CompensatingTransactionOperationRecorder}
 * objects, supplying them with the information necessary for the respective
 * operations.
 * <p>
 * The actual operations performed by the respective methods of this class might
 * not be what would originally be expected. E.g. one would expect that the
 * {@link #performOperation()} method of a
 * CompensatingTransactionOperationExecutor implementation would actually delete
 * the entry, leaving it for the {@link #rollback()} method to recreate it using
 * data from the original entry. However, this will not always be possible. In
 * an LDAP system, for instance, it might not be possible to retrieve all the
 * stored data from the original entry. In that case, the
 * {@link #performOperation()} method will instead move the entry to a temporary
 * location and leave it for the {@link #commit()} method to actually remove the
 * entry.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public interface CompensatingTransactionOperationExecutor {
    /**
     * Rollback the operation, restoring state of the target as it was before
     * the operation was performed using the information supplied on creation of
     * this instance.
     */
    public void rollback();

    /**
     * Commit the operation. In many cases, this will not require any work at
     * all to be performed. However, in some cases there will be interesting
     * stuff to do. See class description for elaboration on this.
     */
    public void commit();

    /**
     * Perform the operation. This will most often require performing the
     * recorded operation, but in some cases the actual operation performed by
     * this method might be something else. See class description for
     * elaboration on this.
     */
    public void performOperation();
}
