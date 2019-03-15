/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core.simple;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;

/**
 * LDAP operations interface usable on Java 5 and above, exposing a set of
 * common LDAP operations.
 * 
 * @author Mattias Hellborg Arthursson
 * @deprecated Core classes are parameterized as of 2.0.
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
	 * ParameterizedContextMapper.
	 * 
	 * @param base Base DN relative to the base of the ContextSource - where to
	 * start the search.
	 * @param filter Search filter.
	 * @param mapper the Mapper to supply all results to.
	 * @return a List of type T containing objects for all entries found, as
	 * mapped by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> List<T> search(String base, String filter,
			ParameterizedContextMapper<T> mapper);

	/**
	 * Search for a List of type T using the supplied filter and link
	 * ParameterizedContextMapper.
	 * 
	 * @param base Base DN relative to the base of the ContextSource - where to
	 * start the search.
	 * @param filter Search filter.
	 * @param mapper the Mapper to supply all results to.
	 * @return a List of type T containing objects for all entries found, as
	 * mapped by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	<T> List<T> search(Name base, String filter,
			ParameterizedContextMapper<T> mapper);

	/**
	 * Search for a List of type T using the supplied filter, SearchControls,
	 * DirContextProcessor and ParameterizedContextMapper.
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
	 * mapped by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> List<T> search(String base, String filter, SearchControls controls,
			ParameterizedContextMapper<T> mapper, DirContextProcessor processor);

	/**
	 * Search for a List of type T using the supplied filter, SearchControls,
	 * DirContextProcessor and ParameterizedContextMapper.
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
	 * mapped by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	<T> List<T> search(Name base, String filter, SearchControls controls,
			ParameterizedContextMapper<T> mapper, DirContextProcessor processor);

	/**
	 * Perform a lookup of the specified DN and map the result using the mapper.
	 * 
	 * @param dn the Distinguished Name to look up.
	 * @param mapper the mapper to use.
	 * @return the mapped object, as received by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, ParameterizedContextMapper<T> mapper);

	/**
	 * Perform a lookup of the specified DN and map the result using the mapper.
	 * 
	 * @param dn the Distinguished Name to look up.
	 * @param mapper the mapper to use.
	 * @return the mapped object, as received by the ParameterizedContextMapper.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	<T> T lookup(Name dn, ParameterizedContextMapper<T> mapper);

	/**
	 * Perform a search for a unique entry matching the specified search
	 * criteria and return the found object. If no entry is found or if there
	 * are more than one matching entry, an
	 * {@link IncorrectResultSizeDataAccessException} is thrown.
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter.
	 * @param mapper the mapper to use for the search.
	 * @return the single object returned by the mapper that matches the search
	 * criteria.
	 * @throws IncorrectResultSizeDataAccessException if the result is not one
	 * unique entry
	 * @since 1.3
	 */
	<T> T searchForObject(String base, String filter,
			ParameterizedContextMapper<T> mapper);

	/**
	 * Perform a search for a unique entry matching the specified search
	 * criteria and return the found object. If no entry is found or if there
	 * are more than one matching entry, an
	 * {@link IncorrectResultSizeDataAccessException} is thrown.
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter.
	 * @param mapper the mapper to use for the search.
	 * @return the single object returned by the mapper that matches the search
	 * criteria.
	 * @throws IncorrectResultSizeDataAccessException if the result is not one
	 * unique entry
	 * @since 1.3
	 */
	<T> T searchForObject(Name base, String filter,
			ParameterizedContextMapper<T> mapper);

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
	 * Look up the specified DN, and automatically cast it to a
	 * {@link DirContextOperations} instance.
	 * 
	 * @param dn The Distinguished Name of the entry to look up.
	 * @return A {@link DirContextOperations} instance constructed from the
	 * found entry.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	DirContextOperations lookupContext(Name dn);

	/**
	 * Create an entry in the LDAP tree. The attributes used to create the entry
	 * are either retrieved from the <code>obj</code> parameter or the
	 * <code>attributes</code> parameter (or both). One of these parameters may
	 * be null but not both.
	 * 
	 * @param dn The distinguished name to bind the object and attributes to.
	 * @param obj The object to bind, may be null. Typically a DirContext
	 * implementation.
	 * @param attributes The attributes to bind, may be null.
	 * @throws NamingException if any error occurs.
	 */
	void bind(String dn, Object obj, Attributes attributes);

	/**
	 * Create an entry in the LDAP tree. The attributes used to create the entry
	 * are either retrieved from the <code>obj</code> parameter or the
	 * <code>attributes</code> parameter (or both). One of these parameters may
	 * be null but not both.
	 * 
	 * @param dn The distinguished name to bind the object and attributes to.
	 * @param obj The object to bind, may be null. Typically a DirContext
	 * implementation.
	 * @param attributes The attributes to bind, may be null.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	void bind(Name dn, Object obj, Attributes attributes);

	/**
	 * Bind the data in the supplied context in the tree. All specified
	 * Attributes in will be bound to the DN set on the instance.
	 * 
	 * @param ctx the context to bind
	 * @throws IllegalStateException if no DN is set or if the instance is in
	 * update mode.
	 * @since 1.3
	 */
	void bind(DirContextOperations ctx);

	/**
	 * Remove an entry from the LDAP tree. The entry must not have any children.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(String dn);

	/**
	 * Remove an entry from the LDAP tree. The entry must not have any children.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @throws NamingException if any error occurs.
	 * @since 1.3
	 */
	void unbind(Name dn);

	/**
	 * Modify the Attributes of the entry corresponding to the supplied
	 * {@link DirContextOperations} instance. The instance should have been
	 * received from the {@link #lookupContext(String)} operation, and then
	 * modified to match the current state of the matching domain object, e.g.:
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

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * AndFilter filter = new AndFilter();
	 * filter.and(&quot;objectclass&quot;, &quot;person&quot;).and(&quot;uid&quot;, userId);
	 * boolean authenticated = ldapTemplate.authenticate(LdapUtils.emptyLdapName(),
	 * 		filter.toString(), password);
	 * </pre>
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @since 1.3
	 */
	boolean authenticate(String base, String filter, String password);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * AndFilter filter = new AndFilter();
	 * filter.and(&quot;objectclass&quot;, &quot;person&quot;).and(&quot;uid&quot;, userId);
	 * boolean authenticated = ldapTemplate.authenticate(LdapUtils.emptyLdapName(),
	 * 		filter.toString(), password);
	 * </pre>
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @since 1.3
	 */
	boolean authenticate(Name base, String filter, String password);
}
