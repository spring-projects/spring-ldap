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

import javax.naming.NameClassPair;

/**
 * Callback interface used by {@link LdapTemplate} search, list and listBindings
 * methods. Implementations of this interface perform the actual work of
 * extracting results from a single <code>NameClassPair</code> (a
 * <code>NameClassPair</code>, <code>Binding</code> or
 * <code>SearchResult</code> depending on the search operation) returned by an
 * LDAP seach operation, such as search(), list(), and listBindings().
 * 
 * @author Mattias Arthursson
 */
public interface NameClassPairCallbackHandler {
    /**
     * Handle one entry. This method will be called once for each entry returned
     * by a search or list.
     * 
     * @param nameClassPair
     *            the NameClassPair returned from the
     *            <code>NamingEnumeration</code>.
     */
    public void handleNameClassPair(NameClassPair nameClassPair);
}
