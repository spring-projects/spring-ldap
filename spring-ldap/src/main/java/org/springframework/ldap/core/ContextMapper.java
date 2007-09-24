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

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;

/**
 * An interface used by LdapTemplate to map LDAP Contexts to beans. When a
 * DirObjectFactory is set on the ContextSource, the objects returned from
 * <code>search</code> and <code>listBindings</code> operations are
 * automatically transformed to DirContext objects (when using the
 * {@link DefaultDirObjectFactory} - which is typically the case, unless
 * something else has been explicitly specified - you get a
 * {@link DirContextAdapter} object). This object will then be passed to the
 * ContextMapper implementation for transformation to the desired bean.
 * <p>
 * ContextMapper implementations are typically stateless and thus reusable; they
 * are ideal for implementing mapping logic in one place.
 * <p>
 * Alternatively, consider using an {@link AttributesMapper} in stead.
 * 
 * @see LdapTemplate#search(Name, String, ContextMapper)
 * @see LdapTemplate#listBindings(Name, ContextMapper)
 * @see LdapTemplate#lookup(Name, ContextMapper)
 * @see AttributesMapper
 * @see DefaultDirObjectFactory
 * @see DirContextAdapter
 * @see AbstractContextMapper
 * 
 * @author Mattias Arthursson
 */
public interface ContextMapper {
    /**
     * Map a single LDAP Context to an object. The supplied Object
     * <code>ctx</code> is the object from a single {@link SearchResult},
     * {@link Binding}, or a lookup operation.
     * 
     * @param ctx
     *            the context to map to an object. Typically this will be a
     *            {@link DirContextAdapter} instance, unless a project specific
     *            <code>DirObjectFactory</code> has been specified on the
     *            <code>ContextSource</code>.
     * @return an object built from the data in the context.
     */
    public Object mapFromContext(Object ctx);
}
