/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.ContextNotEmptyException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import java.util.List;

/**
 * Interface that specifies a basic set of LDAP operations. Implemented by
 * LdapTemplate, but it might be a useful option to use this interface in order
 * to enhance testability.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public interface LdapOperations {
	/**
	 * Perform a search using a particular {@link SearchExecutor} and context
	 * processor. Use this method only if especially needed - for the most cases
	 * there is an overloaded convenience method which calls this one with
	 * suitable argments. This method handles all the plumbing; getting a
	 * readonly context; looping through the <code>NamingEnumeration</code> and
	 * closing the context and enumeration. The actual search is delegated to
	 * the SearchExecutor and each found <code>NameClassPair</code> is passed to
	 * the <code>CallbackHandler</code>. Any encountered
	 * <code>NamingException</code> will be translated using
	 * {@link LdapUtils#convertLdapException(javax.naming.NamingException)}.
	 * 
	 * @param se The <code>SearchExecutor</code> to use for performing the
	 * actual search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to which
	 * each found entry will be passed.
	 * @param processor <code>DirContextProcessor</code> for custom pre- and
	 * post-processing.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted as no entries being found.
	 */
	void search(SearchExecutor se, NameClassPairCallbackHandler handler, DirContextProcessor processor)
			throws NamingException;

	/**
	 * Perform a search using a particular {@link SearchExecutor}. Use this
	 * method only if especially needed - for the most cases there is an
	 * overloaded convenience method which calls this one with suitable
	 * argments. This method handles all the plumbing; getting a readonly
	 * context; looping through the <code>NamingEnumeration</code> and closing
	 * the context and enumeration. The actual search is delegated to the
	 * <code>SearchExecutor</code> and each found <code>NameClassPair</code> is
	 * passed to the <code>CallbackHandler</code>. Any encountered
	 * <code>NamingException</code> will be translated using the
	 * {@link LdapUtils#convertLdapException(javax.naming.NamingException)}.
	 * 
	 * @param se The <code>SearchExecutor</code> to use for performing the
	 * actual search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to which
	 * each found entry will be passed.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted as no entries being found.
	 * @see #search(Name, String, AttributesMapper)
	 * @see #search(Name, String, ContextMapper)
	 */
	void search(SearchExecutor se, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Perform an operation (or series of operations) on a read-only context.
	 * This method handles the plumbing - getting a <code>DirContext</code>,
	 * translating any Exceptions and closing the context afterwards. This
	 * method is not intended for searches; use
	 * {@link #search(SearchExecutor, NameClassPairCallbackHandler)} or any of
	 * the overloaded search methods for this.
	 * 
	 * @param ce The <code>ContextExecutor</code> to which the actual operation
	 * on the <code>DirContext</code> will be delegated.
	 * @return the result from the ContextExecutor's operation.
	 * @throws NamingException if the operation resulted in a
	 * <code>NamingException</code>.
	 * 
	 * @see #search(SearchExecutor, NameClassPairCallbackHandler)
	 * @see #search(Name, String, AttributesMapper)
	 * @see #search(Name, String, ContextMapper)
	 */
	<T> T executeReadOnly(ContextExecutor<T> ce) throws NamingException;

	/**
	 * Perform an operation (or series of operations) on a read-write context.
	 * This method handles the plumbing - getting a <code>DirContext</code>,
	 * translating any exceptions and closing the context afterwards. This
	 * method is intended only for very particular cases, where there is no
	 * suitable method in this interface to use.
	 * 
	 * @param ce The <code>ContextExecutor</code> to which the actual operation
	 * on the <code>DirContext</code> will be delegated.
	 * @return the result from the ContextExecutor's operation.
	 * @throws NamingException if the operation resulted in a
	 * <code>NamingException</code>.
	 * @see #bind(Name, Object, Attributes)
	 * @see #unbind(Name)
	 * @see #rebind(Name, Object, Attributes)
	 * @see #rename(Name, Name)
	 * @see #modifyAttributes(Name, ModificationItem[])
	 */
	<T> T executeReadWrite(ContextExecutor<T> ce) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. The <code>SearchScope</code>
	 * specified in the supplied <code>SearchControls</code> will be used in the
	 * search. Note that if you are using a <code>ContextMapper</code>, the
	 * returningObjFlag needs to be set to true in the
	 * <code>SearchControls</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResult</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(Name base, String filter, SearchControls controls, NameClassPairCallbackHandler handler)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. See
	 * {@link #search(Name, String, SearchControls, NameClassPairCallbackHandler)}
	 * for details.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResult</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(String base, String filter, SearchControls controls, NameClassPairCallbackHandler handler)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. The <code>SearchScope</code>
	 * specified in the supplied <code>SearchControls</code> will be used in the
	 * search. Note that if you are using a <code>ContextMapper</code>, the
	 * returningObjFlag needs to be set to true in the
	 * <code>SearchControls</code>. The given <code>DirContextProcessor</code>
	 * will be called before and after the search.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResult</code> to.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(Name base, String filter, SearchControls controls, NameClassPairCallbackHandler handler,
			DirContextProcessor processor) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>. The <code>SearchScope</code> specified in
	 * the supplied <code>SearchControls</code> will be used in the search. The
	 * given <code>DirContextProcessor</code> will be called before and after
	 * the search.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	<T> List<T> search(String base, String filter, SearchControls controls, AttributesMapper<T> mapper,
			DirContextProcessor processor) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>. The <code>SearchScope</code> specified in
	 * the supplied <code>SearchControls</code> will be used in the search. The
	 * given <code>DirContextProcessor</code> will be called before and after
	 * the search.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	<T> List<T> search(Name base, String filter, SearchControls controls, AttributesMapper<T> mapper,
			DirContextProcessor processor) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Object returned
	 * in each <code>SearchResult</code> is supplied to the specified
	 * <code>ContextMapper</code>. The <code>SearchScope</code> specified in the
	 * supplied <code>SearchControls</code> will be used in the search. The
	 * given <code>DirContextProcessor</code> will be called before and after
	 * the search.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search. If
	 * the returnObjFlag is not set in the <code>SearchControls</code>, this
	 * method will set it automatically, as this is required for the
	 * <code>ContextMapper</code> to work.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, SearchControls controls, ContextMapper<T> mapper, DirContextProcessor processor)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Object returned
	 * in each <code>SearchResult</code> is supplied to the specified
	 * <code>ContextMapper</code>. The <code>SearchScope</code> specified in the
	 * supplied <code>SearchControls</code> will be used in the search. The
	 * given <code>DirContextProcessor</code> will be called before and after
	 * the search.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search. If
	 * the returnObjFlag is not set in the <code>SearchControls</code>, this
	 * method will set it automatically, as this is required for the
	 * <code>ContextMapper</code> to work.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, SearchControls controls, ContextMapper<T> mapper, DirContextProcessor processor)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. See
	 * {@link #search(Name, String, SearchControls, NameClassPairCallbackHandler, DirContextProcessor)}
	 * for details.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResults</code> to.
	 * @param processor The <code>DirContextProcessor</code> to use before and
	 * after the search.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(String base, String filter, SearchControls controls, NameClassPairCallbackHandler handler,
			DirContextProcessor processor) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. Use the specified values for
	 * search scope and return objects flag.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param returningObjFlag Whether the bound object should be returned in
	 * search results. Must be set to <code>true</code> if a
	 * <code>ContextMapper</code> is used.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResults</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(Name base, String filter, int searchScope, boolean returningObjFlag,
			NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. Use the specified values for
	 * search scope and return objects flag.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param returningObjFlag Whether the bound object should be returned in
	 * search results. Must be set to <code>true</code> if a
	 * <code>ContextMapper</code> is used.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResults</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(String base, String filter, int searchScope, boolean returningObjFlag,
			NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. The default Search scope (
	 * <code>SearchControls.SUBTREE_SCOPE</code>) will be used and the
	 * returnObjects flag will be set to <code>false</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResults</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(Name base, String filter, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>NameClassPairCallbackHandler</code>. The default Search scope (
	 * <code>SearchControls.SUBTREE_SCOPE</code>) will be used and the
	 * returnObjects flag will be set to <code>false</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * the <code>SearchResults</code> to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void search(String base, String filter, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Only return any
	 * attributes mathing the specified attribute names. The Attributes in each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param attrs The attributes to return, <code>null</code> means returning
	 * all attributes.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	<T> List<T> search(Name base, String filter, int searchScope, String[] attrs, AttributesMapper<T> mapper)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. Only return any
	 * attributes mathing the specified attribute names. The Attributes in each
	 * <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param attrs The attributes to return, <code>null</code> means returning
	 * all attributes.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, int searchScope, String[] attrs, AttributesMapper<T> mapper)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, int searchScope, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, int searchScope, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>. The default search scope will be used.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes in
	 * each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>. The default search scope will be used.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>AttributesMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>. Only return the
	 * supplied attributes.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param attrs The attributes to return, <code>null</code> means all
	 * attributes.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, int searchScope, String[] attrs, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>. Only return the
	 * supplied attributes.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param attrs The attributes to return, <code>null</code> means all
	 * attributes.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, int searchScope, String[] attrs, ContextMapper<T> mapper)
			throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, int searchScope, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param searchScope The search scope to set in <code>SearchControls</code>
	 * .
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, int searchScope, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>. The default search
	 * scope (<code>SearchControls.SUBTREE_SCOPE</code>) will be used.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>. The default search
	 * scope (<code>SearchControls.SUBTREE_SCOPE</code>) will be used.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The
	 * <code>Object</code> returned in each <code>SearchResult</code> is
	 * supplied to the specified <code>ContextMapper</code>. The default search
	 * scope (<code>SearchControls.SUBTREE_SCOPE</code>) will be used.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, SearchControls controls, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Object returned
	 * in each <code>SearchResult</code> is supplied to the specified
	 * <code>ContextMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search. If
	 * the returnObjFlag is not set in the <code>SearchControls</code>, this
	 * method will set it automatically, as this is required for the
	 * <code>ContextMapper</code> to work.
	 * @param mapper The <code>ContextMapper</code> to use for translating each
	 * entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, SearchControls controls, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes
	 * returned in each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(String base, String filter, SearchControls controls, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Search for all objects matching the supplied filter. The Attributes
	 * returned in each <code>SearchResult</code> is supplied to the specified
	 * <code>AttributesMapper</code>.
	 * 
	 * @param base The base DN where the search should begin.
	 * @param filter The filter to use in the search.
	 * @param controls The <code>SearchControls</code> to use in the search.
	 * @param mapper The <code>AttributesMapper</code> to use for translating
	 * each entry.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> search(Name base, String filter, SearchControls controls, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Each resulting <code>NameClassPair</code> is supplied
	 * to the specified <code>NameClassPairCallbackHandler</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * each {@link NameClassPair} to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void list(String base, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Each resulting <code>NameClassPair</code> is supplied
	 * to the specified <code>NameClassPairCallbackHandler</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * each {@link NameClassPair} to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void list(Name base, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Pass all the found <code>NameClassPair</code> objects
	 * to the supplied <code>NameClassPairMapper</code> and return all the
	 * returned values as a <code>List</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>NameClassPairMapper</code> to supply each
	 * {@link NameClassPair} to.
	 * @return a <code>List</code> containing the Objects returned from the
	 * Mapper.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> list(String base, NameClassPairMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Pass all the found <code>NameClassPair</code> objects
	 * to the supplied <code>NameClassPairMapper</code> and return all the
	 * returned values as a <code>List</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>NameClassPairMapper</code> to supply each
	 * {@link NameClassPair} to.
	 * @return a <code>List</code> containing the Objects returned from the
	 * Mapper.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> list(Name base, NameClassPairMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @return a List containing the names of all the contexts bound to
	 * <code>base</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    List<String> list(String base) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @return a List containing the names of all the contexts bound to
	 * <code>base</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	List<String> list(Name base) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Each resulting <code>Binding</code> is supplied to the
	 * specified <code>NameClassPairCallbackHandler</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * each {@link Binding} to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void listBindings(final String base, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Each resulting <code>Binding</code> is supplied to the
	 * specified <code>NameClassPairCallbackHandler</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param handler The <code>NameClassPairCallbackHandler</code> to supply
	 * each {@link Binding} to.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	void listBindings(final Name base, NameClassPairCallbackHandler handler) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Pass all the found <code>Binding</code> objects to the
	 * supplied <code>NameClassPairMapper</code> and return all the returned
	 * values as a <code>List</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>NameClassPairMapper</code> to supply each
	 * {@link Binding} to.
	 * @return a <code>List</code> containing the Objects returned from the
	 * Mapper.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> listBindings(String base, NameClassPairMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. Pass all the found <code>Binding</code> objects to the
	 * supplied <code>NameClassPairMapper</code> and return all the returned
	 * values as a <code>List</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>NameClassPairMapper</code> to supply each
	 * {@link Binding} to.
	 * @return a <code>List</code> containing the Objects returned from the
	 * Mapper.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> listBindings(Name base, NameClassPairMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of children of the given
	 * <code>base</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @return a <code>List</code> containing the names of all the contexts
	 * bound to <code>base</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	List<String> listBindings(final String base) throws NamingException;

	/**
	 * Perform a non-recursive listing of children of the given
	 * <code>base</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @return a <code>List</code> containing the names of all the contexts
	 * bound to <code>base</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
	List<String> listBindings(final Name base) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. The Object returned in each {@link Binding} is
	 * supplied to the specified <code>ContextMapper</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> listBindings(String base, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Perform a non-recursive listing of the children of the given
	 * <code>base</code>. The Object returned in each {@link Binding} is
	 * supplied to the specified <code>ContextMapper</code>.
	 * 
	 * @param base The base DN where the list should be performed.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return a <code>List</code> containing all entries received from the
	 * <code>ContextMapper</code>.
	 * @throws NamingException if any error occurs. Note that a
	 * <code>NameNotFoundException</code> will be ignored. Instead this is
	 * interpreted that no entries were found.
	 */
    <T> List<T> listBindings(Name base, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Lookup the supplied DN and return the found object. This will typically
	 * be a {@link DirContextAdapter}, unless the <code>DirObjectFactory</code>
	 * has been modified in the <code>ContextSource</code>.
	 * 
	 * @param dn The distinguished name of the object to find.
	 * @return the found object, typically a {@link DirContextAdapter} instance.
	 * @throws NamingException if any error occurs.
	 * @see #lookupContext(Name)
	 * @see AbstractContextSource#setDirObjectFactory(Class)
	 */
	Object lookup(Name dn) throws NamingException;

	/**
	 * Lookup the supplied DN and return the found object. This will typically
	 * be a {@link DirContextAdapter}, unless the <code>DirObjectFactory</code>
	 * has been modified in the <code>ContextSource</code>.
	 * 
	 * @param dn The distinguished name of the object to find.
	 * @return the found object, typically a {@link DirContextAdapter} instance.
	 * @throws NamingException if any error occurs.
	 * @see #lookupContext(String)
	 * @see AbstractContextSource#setDirObjectFactory(Class)
	 */
	Object lookup(String dn) throws NamingException;

	/**
	 * Convenience method to get the attributes of a specified DN and
	 * automatically pass them to an <code>AttributesMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param mapper The <code>AttributesMapper</code> to use for mapping the
	 * found object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(Name dn, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to get the attributes of a specified DN and
	 * automatically pass them to an <code>AttributesMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param mapper The <code>AttributesMapper</code> to use for mapping the
	 * found object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to lookup a specified DN and automatically pass the
	 * found object to a <code>ContextMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(Name dn, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to lookup a specified DN and automatically pass the
	 * found object to a <code>ContextMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to get the specified attributes of a specified DN and
	 * automatically pass them to an <code>AttributesMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param attributes The names of the attributes to pass to the mapper.
	 * @param mapper The <code>AttributesMapper</code> to use for mapping the
	 * found object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(Name dn, String[] attributes, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to get the specified attributes of a specified DN and
	 * automatically pass them to an <code>AttributesMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param attributes The names of the attributes to pass to the mapper.
	 * @param mapper The <code>AttributesMapper</code> to use for mapping the
	 * found object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, String[] attributes, AttributesMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to get the specified attributes of a specified DN and
	 * automatically pass them to a <code>ContextMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param attributes The names of the attributes to pass to the mapper.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(Name dn, String[] attributes, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Convenience method to get the specified attributes of a specified DN and
	 * automatically pass them to a <code>ContextMapper</code>.
	 * 
	 * @param dn The distinguished name to find.
	 * @param attributes The names of the attributes to pass to the mapper.
	 * @param mapper The <code>ContextMapper</code> to use for mapping the found
	 * object.
	 * @return the object returned from the mapper.
	 * @throws NamingException if any error occurs.
	 */
	<T> T lookup(String dn, String[] attributes, ContextMapper<T> mapper) throws NamingException;

	/**
	 * Modify an entry in the LDAP tree using the supplied
	 * <code>ModificationItems</code>.
	 * 
	 * @param dn The distinguished name of the node to modify.
	 * @param mods The modifications to perform.
	 * @throws NamingException if any error occurs.
	 * @see #modifyAttributes(DirContextOperations)
	 */
	void modifyAttributes(Name dn, ModificationItem[] mods) throws NamingException;

	/**
	 * Modify an entry in the LDAP tree using the supplied
	 * <code>ModificationItems</code>.
	 * 
	 * @param dn The distinguished name of the node to modify.
	 * @param mods The modifications to perform.
	 * @throws NamingException if any error occurs.
	 * @see #modifyAttributes(DirContextOperations)
	 */
	void modifyAttributes(String dn, ModificationItem[] mods) throws NamingException;

	/**
	 * Create an entry in the LDAP tree. The attributes used to create the entry
	 * are either retrieved from the <code>obj</code> parameter or the
	 * <code>attributes</code> parameter (or both). One of these parameters may
	 * be <code>null</code> but not both.
	 * 
	 * @param dn The distinguished name to bind the object and attributes to.
	 * @param obj The object to bind, may be <code>null</code>. Typically a
	 * <code>DirContext</code> implementation.
	 * @param attributes The attributes to bind, may be <code>null</code>.
	 * @throws NamingException if any error occurs.
	 * @see DirContextAdapter
	 */
	void bind(Name dn, Object obj, Attributes attributes) throws NamingException;

	/**
	 * Create an entry in the LDAP tree. The attributes used to create the entry
	 * are either retrieved from the <code>obj</code> parameter or the
	 * <code>attributes</code> parameter (or both). One of these parameters may
	 * be <code>null</code> but not both.
	 * 
	 * @param dn The distinguished name to bind the object and attributes to.
	 * @param obj The object to bind, may be <code>null</code>. Typically a
	 * <code>DirContext</code> implementation.
	 * @param attributes The attributes to bind, may be <code>null</code>.
	 * @throws NamingException if any error occurs.
	 * @see DirContextAdapter
	 */
	void bind(String dn, Object obj, Attributes attributes) throws NamingException;

	/**
	 * Remove an entry from the LDAP tree. The entry must not have any children
	 * - if you suspect that the entry might have descendants, use
	 * {@link #unbind(Name, boolean)} in stead.
	 * 
	 * @param dn The distinguished name of the entry to remove.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(Name dn) throws NamingException;

	/**
	 * Remove an entry from the LDAP tree. The entry must not have any children
	 * - if you suspect that the entry might have descendants, use
	 * {@link #unbind(Name, boolean)} in stead.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(String dn) throws NamingException;

	/**
	 * Remove an entry from the LDAP tree, optionally removing all descendants
	 * in the process.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @param recursive Whether to unbind all subcontexts as well. If this
	 * parameter is <code>false</code> and the entry has children, the operation
	 * will fail.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(Name dn, boolean recursive) throws NamingException;

	/**
	 * Remove an entry from the LDAP tree, optionally removing all descendants
	 * in the process.
	 * 
	 * @param dn The distinguished name to unbind.
	 * @param recursive Whether to unbind all subcontexts as well. If this
	 * parameter is <code>false</code> and the entry has children, the operation
	 * will fail.
	 * @throws NamingException if any error occurs.
	 */
	void unbind(String dn, boolean recursive) throws NamingException;

	/**
	 * Remove an entry and replace it with a new one. The attributes used to
	 * create the entry are either retrieved from the <code>obj</code> parameter
	 * or the <code>attributes</code> parameter (or both). One of these
	 * parameters may be <code>null</code> but not both. This method assumes
	 * that the specified context already exists - if not it will fail.
	 * 
	 * @param dn The distinguished name to rebind.
	 * @param obj The object to bind to the DN, may be <code>null</code>.
	 * Typically a <code>DirContext</code> implementation.
	 * @param attributes The attributes to bind, may be <code>null</code>.
	 * @throws NamingException if any error occurs.
	 * @see DirContextAdapter
	 */
	void rebind(Name dn, Object obj, Attributes attributes) throws NamingException;

	/**
	 * Remove an entry and replace it with a new one. The attributes used to
	 * create the entry are either retrieved from the <code>obj</code> parameter
	 * or the <code>attributes</code> parameter (or both). One of these
	 * parameters may be <code>null</code> but not both. This method assumes
	 * that the specified context already exists - if not it will fail.
	 * 
	 * @param dn The distinguished name to rebind.
	 * @param obj The object to bind to the DN, may be <code>null</code>.
	 * Typically a <code>DirContext</code> implementation.
	 * @param attributes The attributes to bind, may be <code>null</code>.
	 * @throws NamingException if any error occurs.
	 * @see DirContextAdapter
	 */
	void rebind(String dn, Object obj, Attributes attributes) throws NamingException;

	/**
	 * Move an entry in the LDAP tree to a new location.
	 * 
	 * @param oldDn The distinguished name of the entry to move; may not be
	 * <code>null</code> or empty.
	 * @param newDn The distinguished name where the entry should be moved; may
	 * not be <code>null</code> or empty.
	 * @throws ContextNotEmptyException if newDn is already bound
	 * @throws NamingException if any other error occurs.
	 */
	void rename(final Name oldDn, final Name newDn) throws NamingException;

	/**
	 * Move an entry in the LDAP tree to a new location.
	 * 
	 * @param oldDn The distinguished name of the entry to move; may not be
	 * <code>null</code> or empty.
	 * @param newDn The distinguished name where the entry should be moved; may
	 * not be <code>null</code> or empty.
	 * @throws ContextNotEmptyException if newDn is already bound
	 * @throws NamingException if any other error occurs.
	 */
	void rename(final String oldDn, final String newDn) throws NamingException;

	/**
	 * Convenience method to lookup the supplied DN and automatically cast it to
	 * {@link DirContextOperations}.
	 * 
	 * @param dn The distinguished name of the object to find.
	 * @return The found object, cast to {@link DirContextOperations}.
	 * @throws ClassCastException if an alternative
	 * <code>DirObjectFactory</code> has been registered with the
	 * <code>ContextSource</code>, causing the actual class of the returned
	 * object to be something else than {@link DirContextOperations}.
	 * @throws NamingException if any other error occurs.
	 * @see #lookup(Name)
	 * @see #modifyAttributes(DirContextOperations)
	 * @since 1.2
	 */
	DirContextOperations lookupContext(Name dn) throws NamingException, ClassCastException;

	/**
	 * Convenience method to lookup the supplied DN and automatically cast it to
	 * {@link DirContextOperations}.
	 * 
	 * @param dn The distinguished name of the object to find.
	 * @return The found object, cast to {@link DirContextOperations}.
	 * @throws ClassCastException if an alternative
	 * <code>DirObjectFactory</code> has been registered with the
	 * <code>ContextSource</code>, causing the actual class of the returned
	 * object to be something else than {@link DirContextOperations}.
	 * @throws NamingException if any other error occurs.
	 * @see #lookup(String)
	 * @see #modifyAttributes(DirContextOperations)
	 * @since 1.2
	 */
	DirContextOperations lookupContext(String dn) throws NamingException, ClassCastException;

	/**
	 * Modify the attributes of the entry referenced by the supplied
	 * {@link DirContextOperations} instance. The DN to update will be the DN of
	 * the <code>DirContextOperations</code>instance, and the
	 * <code>ModificationItem</code> array is retrieved from the
	 * <code>DirContextOperations</code> instance using a call to
	 * {@link AttributeModificationsAware#getModificationItems()}. <b>NB:</b>
	 * The supplied instance needs to have been properly initialized; this means
	 * that if it hasn't been received from a <code>lookup</code> operation, its
	 * DN needs to be initialized and it must have been put in update mode (
	 * {@link DirContextAdapter#setUpdateMode(boolean)}).
	 * <p>
	 * Typical use of this method would be as follows:
	 * 
	 * <pre>
	 * public void update(Person person) {
	 * 	DirContextOperations ctx = ldapOperations.lookupContext(person.getDn());
	 * 
	 * 	ctx.setAttributeValue(&quot;description&quot;, person.getDescription());
	 * 	ctx.setAttributeValue(&quot;telephoneNumber&quot;, person.getPhone());
	 * 	// More modifications here
	 * 
	 * 	ldapOperations.modifyAttributes(ctx);
	 * }
	 * </pre>
	 * 
	 * @param ctx the DirContextOperations instance to use in the update.
	 * @throws IllegalStateException if the supplied instance is not in update
	 * mode or has not been properly initialized.
	 * @throws NamingException if any other error occurs.
	 * @since 1.2
	 * @see #lookupContext(Name)
	 * @see DirContextAdapter
	 */
	void modifyAttributes(DirContextOperations ctx) throws IllegalStateException, NamingException;

	/**
	 * Bind the data in the supplied context in the tree. All specified
	 * attributes <code>ctx</code>in will be bound to the DN set on <code>ctx</code>.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * DirContextOperations ctx = new DirContextAdapter(dn);
	 * ctx.setAttributeValue(&quot;cn&quot;, &quot;john doe&quot;);
	 * ctx.setAttributeValue(&quot;description&quot;, &quot;some description&quot;);
	 * //More initialization here.
	 * 
	 * ldapTemplate.bind(ctx);
	 * </pre>
	 * @param ctx the context to bind
	 * @throws IllegalStateException if no DN is set or if the instance is in
	 * update mode.
	 * @since 1.3
	 */
	void bind(DirContextOperations ctx);

	/**
	 * Remove an entry and replace it with a new one. The attributes used to
	 * create the entry are retrieved from the <code>ctx</code> parameter. This
	 * method assumes that the specified context already exists - if not it will
	 * fail. The entry will be bound to the DN set on <code>ctx</code>.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * DirContextOperations ctx = new DirContextAdapter(dn);
	 * ctx.setAttributeValue(&quot;cn&quot;, &quot;john doe&quot;);
	 * ctx.setAttributeValue(&quot;description&quot;, &quot;some description&quot;);
	 * //More initialization here.
	 * 
	 * ldapTemplate.rebind(ctx);
	 * </pre>
	 * @param ctx the context to rebind
	 * @throws IllegalStateException if no DN is set or if the instance is in
	 * update mode.
	 * @since 1.3
	 */
	void rebind(DirContextOperations ctx);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry.
	 * <p>
	 * Example:<br/>
	 * 
	 * <pre>
	 * AndFilter filter = new AndFilter();
	 * filter.and(&quot;objectclass&quot;, &quot;person&quot;).and(&quot;uid&quot;, userId);
	 * boolean authenticated = ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
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

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry.
	 * <p>
	 * Example:<br/>
	 * 
	 * <pre>
	 * AndFilter filter = new AndFilter();
	 * filter.and(&quot;objectclass&quot;, &quot;person&quot;).and(&quot;uid&quot;, userId);
	 * boolean authenticated = ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
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
	 * entry. The resulting DirContext instance is then used as input to the
	 * supplied {@link AuthenticatedLdapEntryContextCallback} to perform any
	 * additional LDAP operations against the authenticated DirContext.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param callback the callback to that will be called to perform operations
	 * on the DirContext authenticated with the found user.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @see #authenticate(Name, String, String)
	 * @since 1.3
	 */
	boolean authenticate(Name base, String filter, String password, AuthenticatedLdapEntryContextCallback callback);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry. The resulting DirContext instance is then used as input to the
	 * supplied {@link AuthenticatedLdapEntryContextCallback} to perform any
	 * additional LDAP operations against the authenticated DirContext.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param callback the callback to that will be called to perform operations
	 * on the DirContext authenticated with the found user.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @see #authenticate(String, String, String)
	 * @since 1.3
	 */
	boolean authenticate(String base, String filter, String password, AuthenticatedLdapEntryContextCallback callback);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry. The resulting DirContext instance is then used as input to the
	 * supplied {@link AuthenticatedLdapEntryContextCallback} to perform any
	 * additional LDAP operations against the authenticated DirContext. If an
	 * exception is caught, the same exception is passed on to the given
	 * {@link AuthenticationErrorCallback}. This enables the caller to provide a
	 * callback that, for example, collects the exception for later processing.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param callback the callback that will be called to perform operations
	 * on the DirContext authenticated with the found user.
	 * @param errorCallback the callback that will be called if an exception is caught.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @see #authenticate(Name, String, String, AuthenticatedLdapEntryContextCallback)
	 * @since 1.3.1
	 */
	boolean authenticate(Name base, String filter, String password,
			AuthenticatedLdapEntryContextCallback callback,
			AuthenticationErrorCallback errorCallback);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry. The resulting DirContext instance is then used as input to the
	 * supplied {@link AuthenticatedLdapEntryContextCallback} to perform any
	 * additional LDAP operations against the authenticated DirContext. If an
	 * exception is caught, the same exception is passed on to the given
	 * {@link AuthenticationErrorCallback}. This enables the caller to provide a
	 * callback that, for example, collects the exception for later processing.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param callback the callback that will be called to perform operations
	 * on the DirContext authenticated with the found user.
	 * @param errorCallback the callback that will be called if an exception is caught.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @see #authenticate(String, String, String, AuthenticatedLdapEntryContextCallback)
	 * @since 1.3.1
	 */
	boolean authenticate(String base, String filter, String password,
			AuthenticatedLdapEntryContextCallback callback,
			AuthenticationErrorCallback errorCallback);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry. If an exception is caught, the same exception is passed on to the given
	 * {@link AuthenticationErrorCallback}. This enables the caller to provide a
	 * callback that, for example, collects the exception for later processing.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param errorCallback the callback that will be called if an exception is caught.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @see #authenticate(Name, String, String, AuthenticatedLdapEntryContextCallback, AuthenticationErrorCallback)
	 * @since 1.3.1
	 */
	boolean authenticate(Name base, String filter, String password,
			AuthenticationErrorCallback errorCallback);

	/**
	 * Utility method to perform a simple LDAP 'bind' authentication. Search for
	 * the LDAP entry to authenticate using the supplied base DN and filter; use
	 * the DN of the found entry together with the password as input to
	 * {@link ContextSource#getContext(String, String)}, thus authenticating the
	 * entry. If an exception is caught, the same exception is passed on to the given
	 * {@link AuthenticationErrorCallback}. This enables the caller to provide a
	 * callback that, for example, collects the exception for later processing.
	 * 
	 * @param base the DN to use as the base of the search.
	 * @param filter the search filter - must result in a unique result.
	 * @param password the password to use for authentication.
	 * @param errorCallback the callback that will be called if an exception is caught.
	 * @return <code>true</code> if the authentication was successful,
	 * <code>false</code> otherwise.
	 * @throws IncorrectResultSizeDataAccessException if more than one users were found
	 * @see #authenticate(String, String, String, AuthenticatedLdapEntryContextCallback, AuthenticationErrorCallback)
	 * @since 1.3.1
	 */
	boolean authenticate(String base, String filter, String password,
			AuthenticationErrorCallback errorCallback);

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
	 * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
	 * @since 1.3
	 */
	<T> T searchForObject(Name base, String filter, ContextMapper<T> mapper);

    /**
     * Perform a search for a unique entry matching the specified search
     * criteria and return the found object. If no entry is found or if there
     * are more than one matching entry, an
     * {@link IncorrectResultSizeDataAccessException} is thrown.
     * @param base the DN to use as the base of the search.
     * @param filter the search filter.
     * @param searchControls the searchControls to use for the search.
     * @param mapper the mapper to use for the search.
     * @return the single object returned by the mapper that matches the search
     * criteria.
     * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
     * @since 2.0
     */
    <T> T searchForObject(Name base, String filter, SearchControls searchControls, ContextMapper<T> mapper);

    /**
     * Perform a search for a unique entry matching the specified search
     * criteria and return the found object. If no entry is found or if there
     * are more than one matching entry, an
     * {@link IncorrectResultSizeDataAccessException} is thrown.
     * @param base the DN to use as the base of the search.
     * @param filter the search filter.
     * @param searchControls the searchControls to use for the search.
     * @param mapper the mapper to use for the search.
     * @return the single object returned by the mapper that matches the search
     * criteria.
     * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
     * @since 2.0
     */
    <T> T searchForObject(String base, String filter, SearchControls searchControls, ContextMapper<T> mapper);

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
	 * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
	 * @since 1.3
	 */
	<T> T searchForObject(String base, String filter, ContextMapper<T> mapper);

    /**
     * Perform a search with parameters from the specified LdapQuery. All found objects will be supplied to the
     * <code>ContextMapper</code> for processing, and all returned objects will be collected in a list to be returned.
     *
     * @param query the LDAP query specification.
     * @param mapper the <code>ContextMapper</code> to supply all found entries to.
     * @return a <code>List</code> containing all entries received from the
     * <code>ContextMapper</code>.
     *
     * @throws NamingException if any error occurs.
     * @since 2.0
     */
    <T> List<T> search(LdapQuery query, ContextMapper<T> mapper);

    /**
     * Perform a search with parameters from the specified LdapQuery. The Attributes of the found entrieswill be supplied to the
     * <code>AttributesMapper</code> for processing, and all returned objects will be collected in a list to be returned.
     *
     * @param query the LDAP query specification.
     * @param mapper the <code>Attributes</code> to supply all found Attributes to.
     * @return a <code>List</code> containing all entries received from the
     * <code>Attributes</code>.
     *
     * @throws NamingException if any error occurs.
     * @since 2.0
     */
    <T> List<T> search(LdapQuery query, AttributesMapper<T> mapper);

    /**
     * Perform a search for a unique entry matching the specified LDAP
     * query and return the found entry as a DirContextOperation instance. If no entry is found or if there
     * are more than one matching entry, an
     * {@link IncorrectResultSizeDataAccessException} is thrown.
     * @param query the DN to use as the base of the search.
     * @return the single entry matching the query as a DirContextOperations instance.
     * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
     * @since 2.0
     */
    DirContextOperations searchForContext(LdapQuery query);

    /**
     * Perform a search for a unique entry matching the specified LDAP
     * query and return the found object. If no entry is found or if there
     * are more than one matching entry, an
     * {@link IncorrectResultSizeDataAccessException} is thrown.
     * @param query the LDAP query specification.
     * @return the single object returned by the mapper that matches the search
     * criteria.
     * @throws IncorrectResultSizeDataAccessException if the result is not one unique entry
     * @since 2.0
     */
    <T> T searchForObject(LdapQuery query, ContextMapper<T> mapper);
}
