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

package org.springframework.ldap.core;

import org.springframework.ldap.NamingException;

/**
 * Thrown by a {@link ContextMapperCallbackHandler} when it cannot retrieve an
 * object from the given <code>Binding</code>.
 * 
 * @author Ulrik Sandberg
 * @since 1.2
 */
public class ObjectRetrievalException extends NamingException {

    /**
     * Create a new ObjectRetrievalException.
     * 
     * @param msg
     *            the detail message
     * 
     */
    public ObjectRetrievalException(String msg) {
        super(msg);
    }

    /**
     * Create a new ObjectRetrievalException.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the root cause (if any)
     */
    public ObjectRetrievalException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
