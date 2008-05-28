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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Interface for delegating an actual search operation. The typical
 * implementation of executeSearch would be something like:
 * 
 * <pre>
 * SearchExecutor executor = new SearchExecutor(){
 *   public NamingEnumeration executeSearch(DirContext ctx) throws NamingException{
 *     return ctx.search(dn, filter, searchControls);
 *   }
 * }
 * </pre>
 * 
 * @see org.springframework.ldap.core.LdapTemplate#search(SearchExecutor,
 *      NameClassPairCallbackHandler)
 * 
 * @author Mattias Arthursson
 */
public interface SearchExecutor {
    /**
     * Execute the actual search.
     * 
     * @param ctx
     *            the <code>DirContext</code> on which to work.
     * @return the <code>NamingEnumeration</code> resulting from the search
     *         operation.
     * @throws NamingException
     *             if the search results in one.
     */
    public NamingEnumeration executeSearch(DirContext ctx)
            throws NamingException;
}
