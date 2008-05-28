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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Interface for delegating an actual operation to be performed on a
 * <code>DirContext</code>. For searches, use {@link SearchExecutor} in
 * stead. A typical usage of this interface could be e.g.:
 * 
 * <pre>
 * ContextExecutor executor = new ContextExecutor() {
 *     public Object executeWithContext(DirContext ctx) throws NamingException {
 *         return ctx.lookup(dn);
 *     }
 * };
 * </pre>
 * 
 * @see LdapTemplate#executeReadOnly(ContextExecutor)
 * @see LdapTemplate#executeReadWrite(ContextExecutor)
 * 
 * @author Mattias Arthursson
 */
public interface ContextExecutor {
    /**
     * Perform any operation on the context.
     * 
     * @param ctx
     *            the DirContext to perform the operation on.
     * @return any object resulting from the operation - might be null.
     * @throws NamingException
     *             if the operation resulted in one.
     */
    public Object executeWithContext(DirContext ctx) throws NamingException;
}
