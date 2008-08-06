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
package org.springframework.ldap.core.simple;

import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;

/**
 * LDAP operations interface usable on Java 5 and above, exposing a set of
 * common LDAP operations.
 * 
 * @author Mattias Arthursson
 */
public interface SimpleLdapOperations {

	/**
	 * Get the wrapped LdapOperations instance.
	 * 
	 * @return the wrapped LdapOperations instance.
	 * @throws NamingException if any error occurs.
	 */
	LdapOperations getLdapOperations();

	/**
	 * Search for a List of type T using the supplied filter and link
	 * ParametrizedContextMapper.
	 * 
	 * @param base Base DN relative to the base of the ContextSource - where to
	 * start the search.
	 * @param filter Search filter.
	 * @param mapper the Mapper to supply all results to.
	 * @return a List of type T containing objects for all entries found, as
	 * mapped by the ParametrizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> List<T> search(String base, String filter, ParameterizedContextMapper<T> mapper);

	/**
	 * Search for a List of type T using the supplied filter, SearchControls,
	 * DirContextProcessor and ParametrizedContextMapper.
	 * 
	 * @param base Base DN relative to the base of the ContextSource - where to
	 * start the search.
	 * @param filter Search filter.
	 * @param controls the SearchControls. Make sure that the returningObjFlag
	 * is set to <code>true</code>.
	 * @param mapper the Mapper to supply all results to.
	 * @param processor the DirContextProcessor to be used for applying pre/post
	 * processing on the DirContext instance.
	 * @return a List of type T containing objects for all entries found, as
	 * mapped by the ParametrizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> List<T> search(String base, String filter, SearchControls controls, ParameterizedContextMapper<T> mapper,
			DirContextProcessor processor);

	/**
	 * Perform a lookup of the specified DN and map the result using the mapper.
	 * 
	 * @param dn the Distinguished Name to look up.
	 * @param mapper the mapper to use.
	 * @return the mapped object, as received by the ParametrizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, ParameterizedContextMapper<T> mapper);

	/**
	 * Look up the specified DN, and automatically cast it to a
	 * {@link DirContextOperations} instance.
	 * 
	 * @param dn The Distinguished Name of the entry to look up.
	 * @return A {@link DirContextOperations} instance constructed from the
	 * found entry.
	 * @throws NamingException if any error occurs.
	 */
	DirContextOperations lookupContext(String dn);

	/**
	 * Create an entry in the LDAP tree. The attributes used to create the entry
	 * are either retrieved from the <code>obj</code> parameter or the
	 * <code>attributes</code> parameter (or both). One of these parameters
	 * may be null but not both.
	 * 
	 * @param dn The distinguished name to bind the object and attributes to.
	 * @param obj The object to bind, may be null. Typically a DirContext
	 * implementation.
	 * @param attributes The attributes to bind, may be null.
	 * @throws NamingException if any error occurs.
	 */
	void bind(String dn, Object obj, Attributes attributes);

	/**
	 * Remove an entry from the LDAP tree. The entry must not have any children.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(String dn);

	/**
	 * Modify the Attributes of the entry corresponding to the supplied
	 * {@link DirContextOperations} instance. The instance should have been
	 * received from the {@link #lookupContext(String)} operation, and then modified to
	 * match the current state of the matching domain object, e.g.:
	 * 
	 * <pre>
	 * public void update(Person person) {
	 * 	DirContextOperations ctx = simpleLdapOperations.lookup(person.getDn());
	 * 
	 * 	ctx.setAttributeValue(&quot;description&quot;, person.getDescription());
	 * 	ctx.setAttributeValue(&quot;telephoneNumber&quot;, person.getPhone());
	 * 	// More modifications here
	 * 
	 * 	simpleLdapOperations.modifyAttributes(ctx);
	 * }
	 * </pre>
	 * 
	 * @param ctx the entry to update in the LDAP tree.
	 * @throws NamingException if any error occurs.
	 */
	void modifyAttributes(DirContextOperations ctx);
}
