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

import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.support.LdapUtils;

/**
 * Executes core LDAP functionality and helps to avoid common errors, relieving
 * the user of the burden of looking up contexts, looping through
 * NamingEnumerations and closing contexts.
 * <p>
 * <b>Note for Active Directory (AD) users:</b> AD servers are apparently
 * unable to handle referrals automatically, which causes a
 * <code>PartialResultException</code> to be thrown whenever a referral is
 * encountered in a search. To avoid this, set the
 * <code>ignorePartialResultException</code> property to <code>true</code>.
 * There is currently no way of manually handling these referrals in the form of
 * <code>ReferralException</code>, i.e. either you get the exception (and
 * your results are lost) or all referrals are ignored (if the server is unable
 * to handle them properly. Neither is there any simple way to get notified that
 * a <code>PartialResultException</code> has been ignored (other than in the
 * log).
 * 
 * @see org.springframework.ldap.core.ContextSource
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplate implements LdapOperations, InitializingBean {

    private static final Log log = LogFactory.getLog(LdapTemplate.class);

    private static final int DEFAULT_SEARCH_SCOPE = SearchControls.SUBTREE_SCOPE;

    private static final boolean DONT_RETURN_OBJ_FLAG = false;

    private static final boolean RETURN_OBJ_FLAG = true;

    private static final String[] ALL_ATTRIBUTES = null;

    private ContextSource contextSource;

    private boolean ignorePartialResultException = false;

    /**
     * Constructor for bean usage.
     */
    public LdapTemplate() {
    }

    /**
     * Constructor to setup instance directly.
     * 
     * @param contextSource
     *            the ContextSource to use.
     */
    public LdapTemplate(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * Set the ContextSource. Call this method when the default constructor has
     * been used.
     * 
     * @param contextSource
     *            the ContextSource.
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * Get the ContextSource.
     * 
     * @return the ContextSource.
     */
    public ContextSource getContextSource() {
        return contextSource;
    }

    /**
     * Specify whether <code>PartialResultException</code> should be ignored
     * in searches. AD servers typically have a problem with referrals. Normally
     * a referral should be followed automatically, but this does not seem to
     * work with AD servers. The problem manifests itself with a a
     * <code>PartialResultException</code> being thrown when a referral is
     * encountered by the server. Setting this property to <code>true</code>
     * presents a workaround to this problem by causing
     * <code>PartialResultException</code> to be ignored, so that the search
     * method returns normally. Default value of this parameter is
     * <code>false</code>.
     * 
     * @param ignore
     *            <code>true</code> if <code>PartialResultException</code>
     *            should be ignored in searches, <code>false</code> otherwise.
     *            Default is <code>false</code>.
     */
    public void setIgnorePartialResultException(boolean ignore) {
        this.ignorePartialResultException = ignore;
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, int, boolean,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(Name base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler) {

        search(base, filter, getDefaultSearchControls(searchScope,
                returningObjFlag, ALL_ATTRIBUTES), handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, int, boolean,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(String base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler) {

        search(base, filter, getDefaultSearchControls(searchScope,
                returningObjFlag, ALL_ATTRIBUTES), handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(final Name base, final String filter,
            final SearchControls controls, NameClassPairCallbackHandler handler) {

        // Create a SearchExecutor to perform the search.
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.search(base, filter, controls);
            }
        };

        search(se, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(final String base, final String filter,
            final SearchControls controls, NameClassPairCallbackHandler handler) {

        // Create a SearchExecutor to perform the search.
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.search(base, filter, controls);
            }
        };

        search(se, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public void search(final Name base, final String filter,
            final SearchControls controls,
            NameClassPairCallbackHandler handler, DirContextProcessor processor) {

        // Create a SearchExecutor to perform the search.
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.search(base, filter, controls);
            }
        };

        search(se, handler, processor);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public void search(final String base, final String filter,
            final SearchControls controls,
            NameClassPairCallbackHandler handler, DirContextProcessor processor) {

        // Create a SearchExecutor to perform the search.
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.search(base, filter, controls);
            }
        };

        search(se, handler, processor);
    }

    /**
     * Perform a search operation, such as a search(), list() or listBindings().
     * This method handles all the plumbing; getting a readonly context; looping
     * through the NamingEnumeration and closing the context and enumeration. It
     * also calls the supplied DirContextProcessor before and after the search,
     * respectively. This enables custom pre-processing and post-processing,
     * like for example when handling paged results or other search controls.
     * <p>
     * The actual list is delegated to the {@link SearchExecutor} and each
     * {@link NameClassPair} (this might be a NameClassPair or a subclass
     * thereof) is passed to the CallbackHandler. Any encountered
     * NamingException will be translated using the NamingExceptionTranslator.
     * 
     * @param se
     *            the SearchExecutor to use for performing the actual list.
     * @param handler
     *            the NameClassPairCallbackHandler to which each found entry
     *            will be passed.
     * @param processor
     *            DirContextProcessor for custom pre- and post-processing. May
     *            be <code>null</code> if no custom processing should take
     *            place.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(SearchExecutor se, NameClassPairCallbackHandler handler,
            DirContextProcessor processor) {
        DirContext ctx = contextSource.getReadOnlyContext();

        NamingEnumeration results = null;
        RuntimeException ex = null;
        try {
            processor.preProcess(ctx);
            results = se.executeSearch(ctx);

            while (results.hasMore()) {
                NameClassPair result = (NameClassPair) results.next();
                handler.handleNameClassPair(result);
            }
        } catch (NameNotFoundException e) {
            // The base context was not found, which basically means
            // that the search did not return any results. Just clean up and
            // exit.
            // Note that this may present problems if a DirContextProcessor was
            // supplied - there's no guarantee that the postProcess() operation
            // will go well after a NamingException has been thrown. It is
            // however quite possible that information will be available for
            // retrieval either way.
        } catch (PartialResultException e) {
            // Workaround for AD servers not handling referrals correctly.
            if (ignorePartialResultException) {
                log.debug("PartialResultException encountered and ignored", e);
            } else {
                ex = LdapUtils.convertLdapException(e);
            }
        } catch (javax.naming.NamingException e) {
            ex = LdapUtils.convertLdapException(e);
        } finally {
            try {
                processor.postProcess(ctx);
            } catch (javax.naming.NamingException e) {
                if (ex == null) {
                    ex = LdapUtils.convertLdapException(e);
                } else {
                    // We already had an exception from above and should ignore
                    // this one.
                    log.debug("Ignoring Exception from postProcess, "
                            + "main exception thrown instead", e);
                }
            }
            closeContextAndNamingEnumeration(ctx, results);
            // If we got an exception it should be thrown.
            if (ex != null) {
                throw ex;
            }
        }
    }

    /**
     * Perform a search operation, such as a search(), list() or listBindings().
     * This method handles all the plumbing; getting a readonly context; looping
     * through the NamingEnumeration and closing the context and enumeration.
     * <p>
     * The actual list is delegated to the {@link SearchExecutor} and each
     * {@link NameClassPair} (this might be a NameClassPair or a subclass
     * thereof) is passed to the CallbackHandler. Any encountered
     * NamingException will be translated using the NamingExceptionTranslator.
     * 
     * @param se
     *            the SearchExecutor to use for performing the actual list.
     * @param handler
     *            the NameClassPairCallbackHandler to which each found entry
     *            will be passed.
     * @throws NamingException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(SearchExecutor se, NameClassPairCallbackHandler handler) {
        search(se, handler, new NullDirContextProcessor());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(Name base, String filter,
            NameClassPairCallbackHandler handler) {

        search(base, filter, getDefaultSearchControls(DEFAULT_SEARCH_SCOPE,
                DONT_RETURN_OBJ_FLAG, ALL_ATTRIBUTES), handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void search(String base, String filter,
            NameClassPairCallbackHandler handler) {

        search(base, filter, getDefaultSearchControls(DEFAULT_SEARCH_SCOPE,
                DONT_RETURN_OBJ_FLAG, ALL_ATTRIBUTES), handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, int, java.lang.String[],
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) {
        return search(base, filter, getDefaultSearchControls(searchScope,
                DONT_RETURN_OBJ_FLAG, attrs), mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, int, java.lang.String[],
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) {
        return search(base, filter, getDefaultSearchControls(searchScope,
                DONT_RETURN_OBJ_FLAG, attrs), mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, int,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(Name base, String filter, int searchScope,
            AttributesMapper mapper) {

        return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, int,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(String base, String filter, int searchScope,
            AttributesMapper mapper) {

        return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, org.springframework.ldap.core.AttributesMapper)
     */
    public List search(Name base, String filter, AttributesMapper mapper) {

        return search(base, filter, DEFAULT_SEARCH_SCOPE, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, org.springframework.ldap.core.AttributesMapper)
     */
    public List search(String base, String filter, AttributesMapper mapper) {

        return search(base, filter, DEFAULT_SEARCH_SCOPE, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, int, java.lang.String[],
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) {

        return search(base, filter, getDefaultSearchControls(searchScope,
                RETURN_OBJ_FLAG, attrs), mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, int, java.lang.String[],
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) {

        return search(base, filter, getDefaultSearchControls(searchScope,
                RETURN_OBJ_FLAG, attrs), mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, int, org.springframework.ldap.core.ContextMapper)
     */
    public List search(Name base, String filter, int searchScope,
            ContextMapper mapper) {

        return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, int, org.springframework.ldap.core.ContextMapper)
     */
    public List search(String base, String filter, int searchScope,
            ContextMapper mapper) {

        return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, org.springframework.ldap.core.ContextMapper)
     */
    public List search(Name base, String filter, ContextMapper mapper) {

        return search(base, filter, DEFAULT_SEARCH_SCOPE, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, org.springframework.ldap.core.ContextMapper)
     */
    public List search(String base, String filter, ContextMapper mapper) {

        return search(base, filter, DEFAULT_SEARCH_SCOPE, mapper);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List search(String base, String filter, SearchControls controls,
            ContextMapper mapper) {

        return search(base, filter, controls, mapper,
                new NullDirContextProcessor());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List search(Name base, String filter, SearchControls controls,
            ContextMapper mapper) {

        return search(base, filter, controls, mapper,
                new NullDirContextProcessor());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(Name base, String filter, SearchControls controls,
            AttributesMapper mapper) {

        return search(base, filter, controls, mapper,
                new NullDirContextProcessor());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public List search(String base, String filter, SearchControls controls,
            AttributesMapper mapper) {
        return search(base, filter, controls, mapper,
                new NullDirContextProcessor());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.AttributesMapper,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public List search(String base, String filter, SearchControls controls,
            AttributesMapper mapper, DirContextProcessor processor) {
        AttributesMapperCallbackHandler handler = new AttributesMapperCallbackHandler(
                mapper);
        search(base, filter, controls, handler, processor);

        return handler.getList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.AttributesMapper,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public List search(Name base, String filter, SearchControls controls,
            AttributesMapper mapper, DirContextProcessor processor) {
        AttributesMapperCallbackHandler handler = new AttributesMapperCallbackHandler(
                mapper);
        search(base, filter, controls, handler, processor);

        return handler.getList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#search(java.lang.String,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.ContextMapper,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public List search(String base, String filter, SearchControls controls,
            ContextMapper mapper, DirContextProcessor processor) {
        assureReturnObjFlagSet(controls);
        ContextMapperCallbackHandler handler = new ContextMapperCallbackHandler(
                mapper);
        search(base, filter, controls, handler, processor);

        return handler.getList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#search(javax.naming.Name,
     *      java.lang.String, javax.naming.directory.SearchControls,
     *      org.springframework.ldap.core.ContextMapper,
     *      org.springframework.ldap.core.DirContextProcessor)
     */
    public List search(Name base, String filter, SearchControls controls,
            ContextMapper mapper, DirContextProcessor processor) {
        assureReturnObjFlagSet(controls);
        ContextMapperCallbackHandler handler = new ContextMapperCallbackHandler(
                mapper);
        search(base, filter, controls, handler, processor);

        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(java.lang.String,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void list(final String base, NameClassPairCallbackHandler handler) {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.list(base);
            }
        };

        search(searchExecutor, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(javax.naming.Name,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void list(final Name base, NameClassPairCallbackHandler handler) {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.list(base);
            }
        };

        search(searchExecutor, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(java.lang.String,
     *      org.springframework.ldap.core.NameClassPairMapper)
     */
    public List list(String base, NameClassPairMapper mapper) {
        CollectingNameClassPairCallbackHandler handler = new MappingCollectingNameClassPairCallbackHandler(
                mapper);
        list(base, handler);
        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(javax.naming.Name,
     *      org.springframework.ldap.core.NameClassPairMapper)
     */
    public List list(Name base, NameClassPairMapper mapper) {
        CollectingNameClassPairCallbackHandler handler = new MappingCollectingNameClassPairCallbackHandler(
                mapper);
        list(base, handler);
        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(javax.naming.Name)
     */
    public List list(final Name base) {
        return list(base, new DefaultNameClassPairMapper());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#list(java.lang.String)
     */
    public List list(final String base) {
        return list(base, new DefaultNameClassPairMapper());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(java.lang.String,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void listBindings(final String base,
            NameClassPairCallbackHandler handler) {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.listBindings(base);
            }
        };

        search(searchExecutor, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(javax.naming.Name,
     *      org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    public void listBindings(final Name base,
            NameClassPairCallbackHandler handler) {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.listBindings(base);
            }
        };

        search(searchExecutor, handler);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(java.lang.String,
     *      org.springframework.ldap.core.NameClassPairMapper)
     */
    public List listBindings(String base, NameClassPairMapper mapper) {
        CollectingNameClassPairCallbackHandler handler = new MappingCollectingNameClassPairCallbackHandler(
                mapper);
        listBindings(base, handler);
        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(javax.naming.Name,
     *      org.springframework.ldap.core.NameClassPairMapper)
     */
    public List listBindings(Name base, NameClassPairMapper mapper) {
        CollectingNameClassPairCallbackHandler handler = new MappingCollectingNameClassPairCallbackHandler(
                mapper);
        listBindings(base, handler);
        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(java.lang.String)
     */
    public List listBindings(final String base) {
        return listBindings(base, new DefaultNameClassPairMapper());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(javax.naming.Name)
     */
    public List listBindings(final Name base) {
        return listBindings(base, new DefaultNameClassPairMapper());
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(java.lang.String,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List listBindings(String base, ContextMapper mapper) {

        ContextMapperCallbackHandler handler = new ContextMapperCallbackHandler(
                mapper);
        listBindings(base, handler);

        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#listBindings(javax.naming.Name,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public List listBindings(Name base, ContextMapper mapper) {

        ContextMapperCallbackHandler handler = new ContextMapperCallbackHandler(
                mapper);
        listBindings(base, handler);

        return handler.getList();
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#executeReadOnly(org.springframework.ldap.core.DirContextProcessor)
     */
    public Object executeReadOnly(ContextExecutor ce) {
        DirContext ctx = contextSource.getReadOnlyContext();
        return executeWithContext(ce, ctx);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#executeReadWrite(org.springframework.ldap.core.DirContextProcessor)
     */
    public Object executeReadWrite(ContextExecutor ce) {
        DirContext ctx = contextSource.getReadWriteContext();
        return executeWithContext(ce, ctx);
    }

    private Object executeWithContext(ContextExecutor ce, DirContext ctx) {
        try {
            return ce.executeWithContext(ctx);
        } catch (javax.naming.NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            closeContext(ctx);
        }
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(javax.naming.Name)
     */
    public Object lookup(final Name dn) {
        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.lookup(dn);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(java.lang.String)
     */
    public Object lookup(final String dn) {
        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                return ctx.lookup(dn);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(javax.naming.Name,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public Object lookup(final Name dn, final AttributesMapper mapper) {
        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes attributes = ctx.getAttributes(dn);
                return mapper.mapFromAttributes(attributes);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(java.lang.String,
     *      org.springframework.ldap.core.AttributesMapper)
     */
    public Object lookup(final String dn, final AttributesMapper mapper) {

        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes attributes = ctx.getAttributes(dn);
                return mapper.mapFromAttributes(attributes);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(javax.naming.Name,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public Object lookup(final Name dn, final ContextMapper mapper) {
        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Object object = ctx.lookup(dn);
                return mapper.mapFromContext(object);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(java.lang.String,
     *      org.springframework.ldap.core.ContextMapper)
     */
    public Object lookup(final String dn, final ContextMapper mapper) {

        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Object object = ctx.lookup(dn);
                return mapper.mapFromContext(object);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(javax.naming.Name,
     *      java.lang.String[], org.springframework.ldap.core.AttributesMapper)
     */
    public Object lookup(final Name dn, final String[] attributes,
            final AttributesMapper mapper) {

        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes filteredAttributes = ctx.getAttributes(dn,
                        attributes);
                return mapper.mapFromAttributes(filteredAttributes);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(java.lang.String,
     *      java.lang.String[], org.springframework.ldap.core.AttributesMapper)
     */
    public Object lookup(final String dn, final String[] attributes,
            final AttributesMapper mapper) {
        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes filteredAttributes = ctx.getAttributes(dn,
                        attributes);
                return mapper.mapFromAttributes(filteredAttributes);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(javax.naming.Name,
     *      java.lang.String[], org.springframework.ldap.core.ContextMapper)
     */
    public Object lookup(final Name dn, final String[] attributes,
            final ContextMapper mapper) {

        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes filteredAttributes = ctx.getAttributes(dn,
                        attributes);
                DirContextAdapter contextAdapter = new DirContextAdapter(
                        filteredAttributes, dn);
                return mapper.mapFromContext(contextAdapter);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#lookup(java.lang.String,
     *      java.lang.String[], org.springframework.ldap.core.ContextMapper)
     */
    public Object lookup(final String dn, final String[] attributes,
            final ContextMapper mapper) {

        return executeReadOnly(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                Attributes filteredAttributes = ctx.getAttributes(dn,
                        attributes);
                DistinguishedName name = new DistinguishedName(dn);
                DirContextAdapter contextAdapter = new DirContextAdapter(
                        filteredAttributes, name);
                return mapper.mapFromContext(contextAdapter);
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#modifyAttributes(javax.naming.Name,
     *      javax.naming.directory.ModificationItem[])
     */
    public void modifyAttributes(final Name dn, final ModificationItem[] mods) {
        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.modifyAttributes(dn, mods);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#modifyAttributes(java.lang.String,
     *      javax.naming.directory.ModificationItem[])
     */
    public void modifyAttributes(final String dn, final ModificationItem[] mods) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.modifyAttributes(dn, mods);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#bind(javax.naming.Name,
     *      java.lang.Object, javax.naming.directory.Attributes)
     */
    public void bind(final Name dn, final Object obj,
            final Attributes attributes) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.bind(dn, obj, attributes);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#bind(java.lang.String,
     *      java.lang.Object, javax.naming.directory.Attributes)
     */
    public void bind(final String dn, final Object obj,
            final Attributes attributes) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.bind(dn, obj, attributes);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#unbind(javax.naming.Name)
     */
    public void unbind(final Name dn) {
        doUnbind(dn);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#unbind(java.lang.String)
     */
    public void unbind(final String dn) {
        doUnbind(dn);
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#unbind(javax.naming.Name,
     *      boolean)
     */
    public void unbind(final Name dn, boolean recursive) {
        if (!recursive) {
            doUnbind(dn);
        } else {
            doUnbindRecursively(dn);
        }
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#unbind(java.lang.String,
     *      boolean)
     */
    public void unbind(final String dn, boolean recursive) {
        if (!recursive) {
            doUnbind(dn);
        } else {
            doUnbindRecursively(dn);
        }
    }

    private void doUnbind(final Name dn) {
        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.unbind(dn);
                return null;
            }
        });
    }

    private void doUnbind(final String dn) {
        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.unbind(dn);
                return null;
            }
        });
    }

    private void doUnbindRecursively(final Name dn) {
        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx) {
                deleteRecursively(ctx, new DistinguishedName(dn));
                return null;
            }
        });
    }

    private void doUnbindRecursively(final String dn) {
        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                deleteRecursively(ctx, new DistinguishedName(dn));
                return null;
            }
        });
    }

    /**
     * Delete all subcontexts including the current one recursively.
     * 
     * @param ctx
     *            The context to use for deleting.
     * @param name
     *            The starting point to delete recursively.
     * @throws NamingException
     *             if any error occurs
     */
    protected void deleteRecursively(DirContext ctx, DistinguishedName name) {

        NamingEnumeration enumeration = null;
        try {
            enumeration = ctx.listBindings(name);
            while (enumeration.hasMore()) {
                Binding binding = (Binding) enumeration.next();
                DistinguishedName childName = new DistinguishedName(binding
                        .getName());
                childName.prepend((DistinguishedName) name);
                deleteRecursively(ctx, childName);
            }
            ctx.unbind(name);
            if (log.isDebugEnabled()) {
                log.debug("Entry " + name + " deleted");
            }
        } catch (javax.naming.NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            try {
                enumeration.close();
            } catch (Exception e) {
                // Never mind this
            }
        }
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#rebind(javax.naming.Name,
     *      java.lang.Object, javax.naming.directory.Attributes)
     */
    public void rebind(final Name dn, final Object obj,
            final Attributes attributes) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.rebind(dn, obj, attributes);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#rebind(java.lang.String,
     *      java.lang.Object, javax.naming.directory.Attributes)
     */
    public void rebind(final String dn, final Object obj,
            final Attributes attributes) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.rebind(dn, obj, attributes);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#rename(javax.naming.Name,
     *      javax.naming.Name)
     */
    public void rename(final Name oldDn, final Name newDn) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.rename(oldDn, newDn);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.ldap.core.LdapOperations#rename(java.lang.String,
     *      java.lang.String)
     */
    public void rename(final String oldDn, final String newDn) {

        executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                    throws javax.naming.NamingException {
                ctx.rename(oldDn, newDn);
                return null;
            }
        });
    }

    /*
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (contextSource == null) {
            throw new IllegalArgumentException(
                    "Property 'contextSource' must be set.");
        }
    }

    private void closeContextAndNamingEnumeration(DirContext ctx,
            NamingEnumeration results) {

        closeNamingEnumeration(results);
        closeContext(ctx);
    }

    /**
     * Close the supplied DirContext if it is not null. Swallow any exceptions,
     * as this is only for cleanup.
     * 
     * @param ctx
     *            the context to close.
     */
    private void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
                // Never mind this.
            }
        }
    }

    /**
     * Close the supplied NamingEnumeration if it is not null. Swallow any
     * exceptions, as this is only for cleanup.
     * 
     * @param results
     *            the NamingEnumeration to close.
     */
    private void closeNamingEnumeration(NamingEnumeration results) {
        if (results != null) {
            try {
                results.close();
            } catch (Exception e) {
                // Never mind this.
            }
        }
    }

    private SearchControls getDefaultSearchControls(int searchScope,
            boolean returningObjFlag, String[] attrs) {

        SearchControls controls = new SearchControls();
        controls.setSearchScope(searchScope);
        controls.setReturningObjFlag(returningObjFlag);
        controls.setReturningAttributes(attrs);
        return controls;
    }

    /**
     * Make sure the returnObjFlag is set in the supplied SearchControls. Set it
     * and log if it's not set.
     * 
     * @param controls
     *            the SearchControls to check.
     */
    private void assureReturnObjFlagSet(SearchControls controls) {
        Validate.notNull(controls);
        if (!controls.getReturningObjFlag()) {
            log.info("The returnObjFlag of supplied SearchControls is not set"
                    + " but a ContextMapper is used - setting flag to true");
            controls.setReturningObjFlag(true);
        }
    }

    private final class NullDirContextProcessor implements DirContextProcessor {
        public void postProcess(DirContext ctx) throws NamingException {
            // Do nothing
        }

        public void preProcess(DirContext ctx) throws NamingException {
            // Do nothing
        }
    }

    /**
     * A {@link NameClassPairCallbackHandler} that passes the NameClassPairs
     * found to a NameClassPairMapper and collects the results in a list.
     * 
     * @author Mattias Arthursson
     */
    public class MappingCollectingNameClassPairCallbackHandler extends
            CollectingNameClassPairCallbackHandler {

        private NameClassPairMapper mapper;

        public MappingCollectingNameClassPairCallbackHandler(
                NameClassPairMapper mapper) {
            this.mapper = mapper;
        }

        /*
         * @see org.springframework.ldap.CollectingNameClassPairCallbackHandler#getObjectFromNameClassPair(javax.naming.NameClassPair)
         */
        public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
            try {
                return mapper.mapFromNameClassPair(nameClassPair);
            } catch (javax.naming.NamingException e) {
                throw LdapUtils.convertLdapException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#lookupContext(javax.naming.Name)
     */
    public DirContextOperations lookupContext(Name dn) {
        return (DirContextOperations) lookup(dn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#lookupContext(java.lang.String)
     */
    public DirContextOperations lookupContext(String dn) {
        return (DirContextOperations) lookup(dn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.core.LdapOperations#modifyAttributes(org.springframework.ldap.core.DirContextOperations)
     */
    public void modifyAttributes(DirContextOperations ctx) {
        Name dn = ctx.getDn();
        if (dn != null && ctx.isUpdateMode()) {
            modifyAttributes(dn, ctx.getModificationItems());
        } else {
            throw new IllegalStateException(
                    "The DirContextOperations instance needs to be properly initialized.");
        }

    }
}
