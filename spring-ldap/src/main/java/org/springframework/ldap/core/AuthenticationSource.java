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

/**
 * An <code>AuthenticationSource</code> is responsible for providing the
 * principal (user DN) and credentials to be used when creating a new context.
 * 
 * @author Mattias Arthursson
 * 
 */
public interface AuthenticationSource {
    /**
     * Get the principal to use when creating an authenticated context.
     * 
     * @return the principal (userDn).
     */
    public String getPrincipal();

    /**
     * Get the credentials to use when creating an authenticated context.
     * 
     * @return the credentials (password).
     */
    public String getCredentials();
}
