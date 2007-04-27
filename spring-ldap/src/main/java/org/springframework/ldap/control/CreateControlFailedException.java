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

package org.springframework.ldap.control;

import org.springframework.ldap.NamingException;

/**
 * Thrown by an AbstractRequestControlDirContextProcessor when it cannot create
 * a request control.
 * 
 * @author Ulrik Sandberg
 * @since 1.2
 */
public class CreateControlFailedException extends NamingException {

    /**
     * Create a new CreateControlFailedException.
     * 
     * @param msg
     *            the detail message
     */
    public CreateControlFailedException(String msg) {
        super(msg);
    }

    /**
     * Create a new CreateControlFailedException.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the root cause (if any)
     */
    public CreateControlFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
