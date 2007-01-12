/*
 * Copyright 2002-2005 the original author or authors.
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

import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.ContextNotEmptyException;
import org.springframework.ldap.NamingException;

/**
 * Interface that specifies a basic set of LDAP operations. Implemented by
 * LdapTemplate, but it might be a useful option to use this interface in order
 * to enhance testability.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public interface LdapOperations {
    /**
     * Perform a search using a custom context processor. Use this method only
     * if especially needed - for the most cases there is an overloaded
     * convenience method which calls this one with suitable argments. This
     * method handles all the plumbing; getting a readonly context; looping
     * through the NamingEnumeration and closing the context and enumeration.
     * The actual search is delegated to the SearchExecutor and each found
     * SearchResult is passed to the CallbackHandler. Any encountered
     * NamingException will be translated using the NamingExceptionTranslator.
     * 
     * @param se
     *            The SearchExecutor to use for performing the actual search.
     * @param handler
     *            The NameClassPairCallbackHandler to which each found entry
     *            will be passed.
     * @param processor
     *            DirContextProcessor for custom pre- and post-processing.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted as no entries being
     *             found.
     */
    public void search(SearchExecutor se, NameClassPairCallbackHandler handler,
            DirContextProcessor processor) throws NamingException;

    /**
     * Perform a search. Use this method only if especially needed - for the
     * most cases there is an overloaded convenience method which calls this one
     * with suitable argments. This method handles all the plumbing; getting a
     * readonly context; looping through the NamingEnumeration and closing the
     * context and enumeration. The actual search is delegated to the
     * SearchExecutor and each found SearchResult is passed to the
     * CallbackHandler. Any encountered NamingException will be translated using
     * the NamingExceptionTranslator.
     * 
     * @param se
     *            The SearchExecutor to use for performing the actual search.
     * @param handler
     *            The NameClassPairCallbackHandler to which each found entry
     *            will be passed.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted as no entries being
     *             found.
     */
    public void search(SearchExecutor se, NameClassPairCallbackHandler handler)
            throws NamingException;

    /**
     * Perform an operation (or series of operations) on a read-only context.
     * This method handles the plumbing - getting a DirContext, translating any
     * Exceptions and closing the context afterwards. This method is not
     * intended for searches; use
     * {@link #search(SearchExecutor, NameClassPairCallbackHandler)} or any of
     * the overloaded search methods for this.
     * 
     * @param ce
     *            The ContextExecutor to which the actual operation on the
     *            DirContext will be delegated.
     * @return the result from the ContextExecutor's operation.
     * @throws NamingException
     *             if the operation resulted in a NamingException.
     */
    public Object executeReadOnly(ContextExecutor ce) throws NamingException;

    /**
     * Perform an operation (or series of operations) on a read-write context.
     * This method handles the plumbing - getting a DirContext, translating any
     * exceptions and closing the context afterwards.
     * 
     * @param ce
     *            The ContextExecutor to which the actual operation on the
     *            DirContext will be delegated.
     * @return the result from the ContextExecutor's operation.
     * @throws NamingException
     *             if the operation resulted in a NamingException.
     */
    public Object executeReadWrite(ContextExecutor ce) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The SearchScope
     * specified in the supplied SearchControls will be used in the search. Note
     * that if you are using a ContextMapper, the returningObjFlag needs to be
     * set to true in the SearchControls.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. See
     * {@link #search(Name, String, SearchControls, NameClassPairCallbackHandler)}
     * for details.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The SearchScope
     * specified in the supplied SearchControls will be used in the search. Note
     * that if you are using a ContextMapper, the returningObjFlag needs to be
     * set to true in the SearchControls. The given DirContextProcessor will be
     * called before and after the search.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * SearchScope specified in the supplied SearchControls will be used in the
     * search. The given DirContextProcessor will be called before and after the
     * search.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            AttributesMapper mapper, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * SearchScope specified in the supplied SearchControls will be used in the
     * search. The given DirContextProcessor will be called before and after the
     * search.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            AttributesMapper mapper, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * SearchScope specified in the supplied SearchControls will be used in the
     * search. The given DirContextProcessor will be called before and after the
     * search.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            ContextMapper mapper, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * SearchScope specified in the supplied SearchControls will be used in the
     * search. The given DirContextProcessor will be called before and after the
     * search.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            ContextMapper mapper, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. See
     * {@link #search(Name, String, SearchControls, NameClassPairCallbackHandler, DirContextProcessor)}
     * for details.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @param processor
     *            The DirContextProcessor to use before and after the search.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler, DirContextProcessor processor)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * values for search scope and return objects flag.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param returningObjFlag
     *            Whether the bound object should be returned in search results.
     *            Must be set to <code>true</code> if a ContextMapper is used.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * search scope and return objects flag in search controls.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param returningObjFlag
     *            whether the bound object should be returned in search results.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The default
     * Search scope (SearchControls.SUBTREE_SCOPE) will be used and the
     * returnObjects flag will be set to false.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The default
     * Search scope (SearchControls.SUBTREE_SCOPE) will be used and no the
     * returnObjects will be set to false.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param handler
     *            The NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Only search for the
     * specified attributes. The Attributes in each SearchResult is supplied to
     * the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param attrs
     *            The attributes to return, null means returning all attributes.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. Only search for the
     * specified attributes. The Attributes in each SearchResult is supplied to
     * the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param attrs
     *            The attributes to return, null means returning all attributes.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            AttributesMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            AttributesMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * default seach scope will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, AttributesMapper mapper)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * default seach scope will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, AttributesMapper mapper)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. Only
     * look for the supplied attributes.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param attrs
     *            The attributes to return, null means all attributes.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. Only
     * look for the supplied attributes.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param attrs
     *            The attributes to return, null means all attributes.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param searchScope
     *            The search scope to set in SearchControls.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * default search scope (SearchControls.SUBTREE_SCOPE) will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, ContextMapper mapper)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * default search scope (SearchControls.SUBTREE_SCOPE) will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, ContextMapper mapper)
            throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            The ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            ContextMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            AttributesMapper mapper) throws NamingException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            The filter to use in the search.
     * @param controls
     *            The SearchControls to use in the search.
     * @param mapper
     *            The AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            AttributesMapper mapper) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Each resulting NameClassPair is supplied to the
     * specified NameClassPairCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            The NameClassPairCallbackHandler to supply each
     *            {@link NameClassPair} to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void list(String base, NameClassPairCallbackHandler handler)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Each resulting NameClassPair is supplied to the
     * specified NameClassPairCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            The NameClassPairCallbackHandler to supply each
     *            {@link NameClassPair} to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void list(Name base, NameClassPairCallbackHandler handler)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Pass all the found NameClassPair objects to the
     * supplied NameClassPairMapper and return all the returned values as a
     * List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The NameClassPairMapper to supply each {@link NameClassPair}
     *            to.
     * @return a List containing the Objects returned from the Mapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List list(String base, NameClassPairMapper mapper)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Pass all the found NameClassPair objects to the
     * supplied NameClassPairMapper and return all the returned values as a
     * List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The NameClassPairMapper to supply each {@link NameClassPair}
     *            to.
     * @return a List containing the Objects returned from the Mapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List list(Name base, NameClassPairMapper mapper)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to
     *         <code>base</code>.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List list(String base) throws NamingException;

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to
     *         <code>base</code>.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List list(Name base) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Each resulting Binding is supplied to the specified
     * NameClassPairCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            The NameClassPairCallbackHandler to supply each
     *            {@link Binding} to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void listBindings(final String base,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Each resulting Binding is supplied to the specified
     * NameClassPairCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            The NameClassPairCallbackHandler to supply each
     *            {@link Binding} to.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void listBindings(final Name base,
            NameClassPairCallbackHandler handler) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Pass all the found Binding objects to the supplied
     * NameClassPairMapper and return all the returned values as a List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The NameClassPairMapper to supply each {@link Binding} to.
     * @return a List containing the Objects returned from the Mapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(String base, NameClassPairMapper mapper)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. Pass all the found Binding objects to the supplied
     * NameClassPairMapper and return all the returned values as a List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The NameClassPairMapper to supply each {@link Binding} to.
     * @return a List containing the Objects returned from the Mapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(Name base, NameClassPairMapper mapper)
            throws NamingException;

    /**
     * Perform a non-recursive listing of children of the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to
     *         <code>base</code>.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(final String base) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to
     *         <code>base</code>.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(final Name base) throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. The Object returned in each {@link Binding} is
     * supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(String base, ContextMapper mapper)
            throws NamingException;

    /**
     * Perform a non-recursive listing of the children of the given
     * <code>base</code>. The Object returned in each {@link Binding} is
     * supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return a List containing all entries received from the ContextMapper.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List listBindings(Name base, ContextMapper mapper)
            throws NamingException;

    /**
     * Lookup the supplied DN and return the found object. <b>WARNING</b>: This
     * method should only be used if a DirObjectFactory has been specified on
     * the ContextFactory. If this is not the case, you will get a new instance
     * of the actual DirContext, which is probably not what you want. If,
     * however this <b>is</b> what you want, be careful to close the context
     * after you finished working with it.
     * 
     * @param dn
     *            The distinguished name of the object to find.
     * @return the found object.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(Name dn) throws NamingException;

    /**
     * Lookup the supplied DN and return the found object. <b>WARNING</b>: This
     * method should only be used if a DirObjectFactory has been specified on
     * the ContextFactory. If this is not the case, you will get a new instance
     * of the actual DirContext, which is probably not what you want. If,
     * however this <b>is</b> what you want, be careful to close the context
     * after you finished working with it.
     * 
     * @param dn
     *            The distinguished name of the object to find.
     * @return the found object.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(String dn) throws NamingException;

    /**
     * Convenience method to get the attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param mapper
     *            The AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(Name dn, AttributesMapper mapper)
            throws NamingException;

    /**
     * Convenience method to get the attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param mapper
     *            The AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(String dn, AttributesMapper mapper)
            throws NamingException;

    /**
     * Convenience method to lookup a specified DN and automatically pass the
     * found object to a ContextMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(Name dn, ContextMapper mapper) throws NamingException;

    /**
     * Convenience method to lookup a specified DN and automatically pass the
     * found object to a ContextMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(String dn, ContextMapper mapper)
            throws NamingException;

    /**
     * Convenience method to get the specified attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param attributes
     *            The names of the attributes to pass to the mapper.
     * @param mapper
     *            The AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(Name dn, String[] attributes, AttributesMapper mapper)
            throws NamingException;

    /**
     * Convenience method to get the specified attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param attributes
     *            The names of the attributes to pass to the mapper.
     * @param mapper
     *            The AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(String dn, String[] attributes, AttributesMapper mapper)
            throws NamingException;

    /**
     * Convenience method to get the specified attributes of a specified DN and
     * automatically pass them to a ContextMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param attributes
     *            The names of the attributes to pass to the mapper.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(Name dn, String[] attributes, ContextMapper mapper)
            throws NamingException;

    /**
     * Convenience method to get the specified attributes of a specified DN and
     * automatically pass them to a ContextMapper.
     * 
     * @param dn
     *            The distinguished name to find.
     * @param attributes
     *            The names of the attributes to pass to the mapper.
     * @param mapper
     *            The ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws NamingException
     *             if any error occurs.
     */
    public Object lookup(String dn, String[] attributes, ContextMapper mapper)
            throws NamingException;

    /**
     * Modify an entry in the LDAP tree using the supplied ModificationItems.
     * 
     * @param dn
     *            The distinguished name of the node to modify.
     * @param mods
     *            The modifications to perform.
     * @throws NamingException
     *             if any error occurs.
     */
    public void modifyAttributes(Name dn, ModificationItem[] mods)
            throws NamingException;

    /**
     * Modify an entry in the LDAP tree using the supplied ModificationItems.
     * 
     * @param dn
     *            The distinguished name of the node to modify.
     * @param mods
     *            The modifications to perform.
     * @throws NamingException
     *             if any error occurs.
     */
    public void modifyAttributes(String dn, ModificationItem[] mods)
            throws NamingException;

    /**
     * Create an entry in the LDAP tree. The attributes used to create the entry
     * are either retrieved from the <code>obj</code> parameter or the
     * <code>attributes</code> parameter (or both). One of these parameters
     * may be null but not both.
     * 
     * @param dn
     *            The distinguished name to bind the object and attributes to.
     * @param obj
     *            The object to bind, may be null. Typically a DirContext
     *            implementation.
     * @param attributes
     *            The attributes to bind, may be null.
     * @throws NamingException
     *             if any error occurs.
     */
    public void bind(Name dn, Object obj, Attributes attributes)
            throws NamingException;

    /**
     * Create an entry in the LDAP tree. The attributes used to create the entry
     * are either retrieved from the <code>obj</code> parameter or the
     * <code>attributes</code> parameter (or both). One of these parameters
     * may be null but not both.
     * 
     * @param dn
     *            The distinguished name to bind the object and attributes to.
     * @param obj
     *            The object to bind, may be null. Typically a DirContext
     *            implementation.
     * @param attributes
     *            The attributes to bind, may be null.
     * @throws NamingException
     *             if any error occurs.
     */
    public void bind(String dn, Object obj, Attributes attributes)
            throws NamingException;

    /**
     * Remove an entry from the LDAP tree. The entry must not have any children -
     * if you suspect that the entry might have descendants, use
     * {@link #unbind(Name, boolean)} in stead.
     * 
     * @param dn
     *            The distinguished name of the entry to remove.
     * @throws NamingException
     *             if any error occurs.
     */
    public void unbind(Name dn) throws NamingException;

    /**
     * Remove an entry from the LDAP tree. The entry must not have any children -
     * if you suspect that the entry might have descendants, use
     * {@link #unbind(Name, boolean)} in stead.
     * 
     * @param dn
     *            The distinguished name to unbind.
     * @throws NamingException
     *             if any error occurs.
     */
    public void unbind(String dn) throws NamingException;

    /**
     * Remove an entry from the LDAP tree, optionally removing all descendants
     * in the process.
     * 
     * @param dn
     *            The distinguished name to unbind.
     * @param recursive
     *            Whether to unbind all subcontexts as well. If this parameter
     *            is <code>false</code> and the entry has children, the
     *            operation will fail.
     * @throws NamingException
     *             if any error occurs.
     */
    public void unbind(Name dn, boolean recursive) throws NamingException;

    /**
     * Remove an entry from the LDAP tree, optionally removing all descendants
     * in the process.
     * 
     * @param dn
     *            The distinguished name to unbind.
     * @param recursive
     *            Whether to unbind all subcontexts as well. If this parameter
     *            is <code>false</code> and the entry has children, the
     *            operation will fail.
     * @throws NamingException
     *             if any error occurs.
     */
    public void unbind(String dn, boolean recursive) throws NamingException;

    /**
     * Remove an entry and replace it with a new one. The attributes used to
     * create the entry are either retrieved from the <code>obj</code>
     * parameter or the <code>attributes</code> parameter (or both). One of
     * these parameters may be null but not both. This method assumes that the
     * specified context already exists - if not it will fail.
     * 
     * @param dn
     *            The distinguished name to rebind.
     * @param obj
     *            The object to bind to the DN, may be null. Typically a
     *            DirContext implementation.
     * @param attributes
     *            The attributes to bind, may be null.
     * @throws NamingException
     *             if any error occurs.
     */
    public void rebind(Name dn, Object obj, Attributes attributes)
            throws NamingException;

    /**
     * Remove an entry and replace it with a new one. The attributes used to
     * create the entry are either retrieved from the <code>obj</code>
     * parameter or the <code>attributes</code> parameter (or both). One of
     * these parameters may be null but not both. This method assumes that the
     * specified context already exists - if not it will fail.
     * 
     * @param dn
     *            The distinguished name to rebind.
     * @param obj
     *            The object to bind to the DN, may be null. Typically a
     *            DirContext implementation.
     * @param attributes
     *            The attributes to bind, may be null.
     * @throws NamingException
     *             if any error occurs.
     */
    public void rebind(String dn, Object obj, Attributes attributes)
            throws NamingException;

    /**
     * Move an entry in the LDAP tree to a new location.
     * 
     * @param oldDn
     *            The distinguished name of the entry to move; may not be null
     *            or empty.
     * @param newDn
     *            The distinguished name where the entry should be moved; may
     *            not be null or empty.
     * @throws ContextNotEmptyException
     *             if newDn is already bound
     * @throws NamingException
     *             if any other error occurs.
     */
    public void rename(final Name oldDn, final Name newDn)
            throws NamingException;

    /**
     * Move an entry in the LDAP tree to a new location.
     * 
     * @param oldDn
     *            The distinguished name of the entry to move; may not be null
     *            or empty.
     * @param newDn
     *            The distinguished name where the entry should be moved; may
     *            not be null or empty.
     * @throws ContextNotEmptyException
     *             if newDn is already bound
     * @throws NamingException
     *             if any other error occurs.
     */
    public void rename(final String oldDn, final String newDn)
            throws NamingException;
}
