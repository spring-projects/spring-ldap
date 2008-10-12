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

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * An interface used by LdapTemplate for mapping LDAP Attributes to beans.
 * Implementions of this interface perform the actual work of extracting
 * results, but need not worry about exception handling. <code>NamingExceptions</code> will
 * be caught and handled correctly by the {@link LdapTemplate} class.
 * <p>
 * Typically used in search methods of {@link LdapTemplate}.
 * <code>AttributeMapper</code> objects are normally stateless and thus
 * reusable; they are ideal for implementing attribute-mapping logic in one
 * place.
 * <p>
 * Alternatively, consider using a {@link ContextMapper} in stead.
 * 
 * @see LdapTemplate#search(Name, String, AttributesMapper)
 * @see LdapTemplate#lookup(Name, AttributesMapper)
 * @see ContextMapper
 * 
 * @author Mattias Arthursson
 */
public interface AttributesMapper {
    /**
     * Map Attributes to an object. The supplied attributes are the attributes
     * from a single SearchResult.
     * 
     * @param attributes
     *            attributes from a SearchResult.
     * @return an object built from the attributes.
     * @throws NamingException
     *             if any error occurs mapping the attributes
     */
    public Object mapFromAttributes(Attributes attributes)
            throws NamingException;
}
