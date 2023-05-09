/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ldap.core;

import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.PartialResultException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Executes core LDAP functionality and helps to avoid common errors, relieving the user
 * of the burden of looking up contexts, looping through NamingEnumerations and closing
 * contexts.
 * <p>
 * <b>Note for Active Directory (AD) users:</b> AD servers are apparently unable to handle
 * referrals automatically, which causes a <code>PartialResultException</code> to be
 * thrown whenever a referral is encountered in a search. To avoid this, set the
 * <code>ignorePartialResultException</code> property to <code>true</code>. There is
 * currently no way of manually handling these referrals in the form of
 * <code>ReferralException</code>, i.e. either you get the exception (and your results are
 * lost) or all referrals are ignored (if the server is unable to handle them properly.
 * Neither is there any simple way to get notified that a
 * <code>PartialResultException</code> has been ignored (other than in the log).
 *
 * @see org.springframework.ldap.core.ContextSource
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplate implements LdapOperations, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(LdapTemplate.class);

	private static final boolean DONT_RETURN_OBJ_FLAG = false;

	private static final boolean RETURN_OBJ_FLAG = true;

	private static final String[] ALL_ATTRIBUTES = null;

	private ContextSource contextSource;

	private boolean ignorePartialResultException = false;

	private boolean ignoreNameNotFoundException = false;

	private boolean ignoreSizeLimitExceededException = true;

	private int defaultSearchScope = SearchControls.SUBTREE_SCOPE;

	private int defaultTimeLimit = 0;

	private int defaultCountLimit = 0;

	private ObjectDirectoryMapper odm = new DefaultObjectDirectoryMapper();

	/**
	 * Constructor for bean usage.
	 */
	public LdapTemplate() {
	}

	/**
	 * Constructor to setup instance directly.
	 * @param contextSource the ContextSource to use.
	 */
	public LdapTemplate(ContextSource contextSource) {
		this.contextSource = contextSource;
	}

	/**
	 * Set the ContextSource. Call this method when the default constructor has been used.
	 * @param contextSource the ContextSource.
	 */
	public void setContextSource(ContextSource contextSource) {
		this.contextSource = contextSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectDirectoryMapper getObjectDirectoryMapper() {
		return this.odm;
	}

	/**
	 * Set the ObjectDirectoryMapper instance to use.
	 * @param odm the ObejctDirectoryMapper to use.
	 * @since 2.0
	 */
	public void setObjectDirectoryMapper(ObjectDirectoryMapper odm) {
		this.odm = odm;
	}

	/**
	 * Get the ContextSource.
	 * @return the ContextSource.
	 */
	public ContextSource getContextSource() {
		return this.contextSource;
	}

	/**
	 * Specify whether <code>NameNotFoundException</code> should be ignored in searches.
	 * In previous version, <code>NameNotFoundException</code> caused by the search base
	 * not being found was silently ignored. The default behavior is now to treat this as
	 * an error (as it should), and to convert and re-throw the exception. The ability to
	 * revert to the previous behavior still exists. The only difference is that the
	 * incident is in that case no longer silently ignored, but logged as a warning.
	 * @param ignore <code>true</code> if <code>NameNotFoundException</code> should be
	 * ignored in searches, <code>false</code> otherwise. Default is <code>false</code>.
	 *
	 * @since 1.3
	 */
	public void setIgnoreNameNotFoundException(boolean ignore) {
		this.ignoreNameNotFoundException = ignore;
	}

	/**
	 * Specify whether <code>PartialResultException</code> should be ignored in searches.
	 * AD servers typically have a problem with referrals. Normally a referral should be
	 * followed automatically, but this does not seem to work with AD servers. The problem
	 * manifests itself with a <code>PartialResultException</code> being thrown when a
	 * referral is encountered by the server. Setting this property to <code>true</code>
	 * presents a workaround to this problem by causing
	 * <code>PartialResultException</code> to be ignored, so that the search method
	 * returns normally. Default value of this parameter is <code>false</code>.
	 * @param ignore <code>true</code> if <code>PartialResultException</code> should be
	 * ignored in searches, <code>false</code> otherwise. Default is <code>false</code>.
	 */
	public void setIgnorePartialResultException(boolean ignore) {
		this.ignorePartialResultException = ignore;
	}

	/**
	 * Specify whether <code>SizeLimitExceededException</code> should be ignored in
	 * searches. This is typically what you want if you specify count limit in your search
	 * controls.
	 * @param ignore <code>true</code> if <code>SizeLimitExceededException</code> should
	 * be ignored in searches, <code>false</code> otherwise. Default is <code>true</code>.
	 * @since 2.0
	 */
	public void setIgnoreSizeLimitExceededException(boolean ignore) {
		this.ignoreSizeLimitExceededException = ignore;
	}

	/**
	 * Set the default scope to be used in searches if not explicitly specified. Default
	 * is {@link javax.naming.directory.SearchControls#SUBTREE_SCOPE}.
	 * @param defaultSearchScope the default search scope to use in searches. One of
	 * {@link SearchControls#OBJECT_SCOPE}, {@link SearchControls#ONELEVEL_SCOPE}, or
	 * {@link SearchControls#SUBTREE_SCOPE}
	 * @since 2.0
	 */
	public void setDefaultSearchScope(int defaultSearchScope) {
		this.defaultSearchScope = defaultSearchScope;
	}

	/**
	 * Set the default time limit be used in searches if not explicitly specified. Default
	 * is 0, indicating no time limit.
	 * @param defaultTimeLimit the default time limit to use in searches.
	 * @since 2.0
	 */
	public void setDefaultTimeLimit(int defaultTimeLimit) {
		this.defaultTimeLimit = defaultTimeLimit;
	}

	/**
	 * Set the default count limit be used in searches if not explicitly specified.
	 * Default is 0, indicating no count limit.
	 * @param defaultCountLimit the default count limit to use in searches.
	 * @since 2.0
	 */
	public void setDefaultCountLimit(int defaultCountLimit) {
		this.defaultCountLimit = defaultCountLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(Name base, String filter, int searchScope, boolean returningObjFlag,
			NameClassPairCallbackHandler handler) {

		search(base, filter, getDefaultSearchControls(searchScope, returningObjFlag, ALL_ATTRIBUTES), handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(String base, String filter, int searchScope, boolean returningObjFlag,
			NameClassPairCallbackHandler handler) {

		search(base, filter, getDefaultSearchControls(searchScope, returningObjFlag, ALL_ATTRIBUTES), handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(final Name base, final String filter, final SearchControls controls,
			NameClassPairCallbackHandler handler) {

		// Create a SearchExecutor to perform the search.
		SearchExecutor se = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.search(base, filter, controls);
			}
		};
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(se, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(final String base, final String filter, final SearchControls controls,
			NameClassPairCallbackHandler handler) {

		// Create a SearchExecutor to perform the search.
		SearchExecutor se = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.search(base, filter, controls);
			}
		};
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(se, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(final Name base, final String filter, final SearchControls controls,
			NameClassPairCallbackHandler handler, DirContextProcessor processor) {

		// Create a SearchExecutor to perform the search.
		SearchExecutor se = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.search(base, filter, controls);
			}
		};
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(se, handler, processor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(final String base, final String filter, final SearchControls controls,
			NameClassPairCallbackHandler handler, DirContextProcessor processor) {

		// Create a SearchExecutor to perform the search.
		SearchExecutor se = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.search(base, filter, controls);
			}
		};
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(se, handler, processor);
	}

	/**
	 * Perform a search operation, such as a search(), list() or listBindings(). This
	 * method handles all the plumbing; getting a readonly context; looping through the
	 * NamingEnumeration and closing the context and enumeration. It also calls the
	 * supplied DirContextProcessor before and after the search, respectively. This
	 * enables custom pre-processing and post-processing, like for example when handling
	 * paged results or other search controls.
	 * <p>
	 * The actual list is delegated to the {@link SearchExecutor} and each
	 * {@link NameClassPair} (this might be a NameClassPair or a subclass thereof) is
	 * passed to the CallbackHandler. Any encountered NamingException will be translated
	 * using the NamingExceptionTranslator.
	 * @param se the SearchExecutor to use for performing the actual list.
	 * @param handler the NameClassPairCallbackHandler to which each found entry will be
	 * passed.
	 * @param processor DirContextProcessor for custom pre- and post-processing. Must not
	 * be <code>null</code>. If no custom processing should take place, please use e.g.
	 * {@link #search(SearchExecutor, NameClassPairCallbackHandler)}.
	 * @throws NamingException if any error occurs. Note that a NameNotFoundException will
	 * be ignored. Instead this is interpreted that no entries were found.
	 */
	@Override
	public void search(SearchExecutor se, NameClassPairCallbackHandler handler, DirContextProcessor processor) {
		DirContext ctx = this.contextSource.getReadOnlyContext();

		NamingEnumeration results = null;
		RuntimeException exception = null;
		try {
			processor.preProcess(ctx);
			results = se.executeSearch(ctx);

			while (results.hasMore()) {
				NameClassPair result = (NameClassPair) results.next();
				handler.handleNameClassPair(result);
			}
		}
		catch (NameNotFoundException ex) {
			// It is possible to ignore errors caused by base not found
			if (this.ignoreNameNotFoundException) {
				LOG.warn("Base context not found, ignoring: " + ex.getMessage());
			}
			else {
				exception = LdapUtils.convertLdapException(ex);
			}
		}
		catch (PartialResultException ex) {
			// Workaround for AD servers not handling referrals correctly.
			if (this.ignorePartialResultException) {
				LOG.debug("PartialResultException encountered and ignored", ex);
			}
			else {
				exception = LdapUtils.convertLdapException(ex);
			}
		}
		catch (SizeLimitExceededException ex) {
			if (this.ignoreSizeLimitExceededException) {
				LOG.debug("SizeLimitExceededException encountered and ignored", ex);
			}
			else {
				exception = LdapUtils.convertLdapException(ex);
			}
		}
		catch (javax.naming.NamingException ex) {
			exception = LdapUtils.convertLdapException(ex);
		}
		finally {
			try {
				processor.postProcess(ctx);
			}
			catch (javax.naming.NamingException ex) {
				if (exception == null) {
					exception = LdapUtils.convertLdapException(ex);
				}
				else {
					// We already had an exception from above and should ignore
					// this one.
					LOG.debug("Ignoring Exception from postProcess, " + "main exception thrown instead", ex);
				}
			}
			closeContextAndNamingEnumeration(ctx, results);
			// If we got an exception it should be thrown.
			if (exception != null) {
				throw exception;
			}
		}
	}

	/**
	 * Perform a search operation, such as a search(), list() or listBindings(). This
	 * method handles all the plumbing; getting a readonly context; looping through the
	 * NamingEnumeration and closing the context and enumeration.
	 * <p>
	 * The actual list is delegated to the {@link SearchExecutor} and each
	 * {@link NameClassPair} (this might be a NameClassPair or a subclass thereof) is
	 * passed to the CallbackHandler. Any encountered NamingException will be translated
	 * using the NamingExceptionTranslator.
	 * @param se the SearchExecutor to use for performing the actual list.
	 * @param handler the NameClassPairCallbackHandler to which each found entry will be
	 * passed.
	 * @throws NamingException if any error occurs. Note that a NameNotFoundException will
	 * be ignored. Instead this is interpreted that no entries were found.
	 */
	@Override
	public void search(SearchExecutor se, NameClassPairCallbackHandler handler) {
		search(se, handler, new NullDirContextProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(Name base, String filter, NameClassPairCallbackHandler handler) {

		SearchControls controls = getDefaultSearchControls(this.defaultSearchScope, DONT_RETURN_OBJ_FLAG,
				ALL_ATTRIBUTES);
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(base, filter, controls, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(String base, String filter, NameClassPairCallbackHandler handler) {

		SearchControls controls = getDefaultSearchControls(this.defaultSearchScope, DONT_RETURN_OBJ_FLAG,
				ALL_ATTRIBUTES);
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(base, filter, controls, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, int searchScope, String[] attrs, AttributesMapper<T> mapper) {
		return search(base, filter, getDefaultSearchControls(searchScope, DONT_RETURN_OBJ_FLAG, attrs), mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, int searchScope, String[] attrs, AttributesMapper<T> mapper) {
		return search(base, filter, getDefaultSearchControls(searchScope, DONT_RETURN_OBJ_FLAG, attrs), mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, int searchScope, AttributesMapper<T> mapper) {
		return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, int searchScope, AttributesMapper<T> mapper) {
		return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, AttributesMapper<T> mapper) {
		return search(base, filter, this.defaultSearchScope, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, AttributesMapper<T> mapper) {
		return search(base, filter, this.defaultSearchScope, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, int searchScope, String[] attrs, ContextMapper<T> mapper) {
		return search(base, filter, getDefaultSearchControls(searchScope, RETURN_OBJ_FLAG, attrs), mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, int searchScope, String[] attrs, ContextMapper<T> mapper) {
		return search(base, filter, getDefaultSearchControls(searchScope, RETURN_OBJ_FLAG, attrs), mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, int searchScope, ContextMapper<T> mapper) {
		return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, int searchScope, ContextMapper<T> mapper) {
		return search(base, filter, searchScope, ALL_ATTRIBUTES, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, ContextMapper<T> mapper) {
		return search(base, filter, this.defaultSearchScope, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, ContextMapper<T> mapper) {
		return search(base, filter, this.defaultSearchScope, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, SearchControls controls, ContextMapper<T> mapper) {
		return search(base, filter, controls, mapper, new NullDirContextProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, SearchControls controls, ContextMapper<T> mapper) {
		return search(base, filter, controls, mapper, new NullDirContextProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, SearchControls controls, AttributesMapper<T> mapper) {
		return search(base, filter, controls, mapper, new NullDirContextProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, SearchControls controls, AttributesMapper<T> mapper) {
		return search(base, filter, controls, mapper, new NullDirContextProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, SearchControls controls, AttributesMapper<T> mapper,
			DirContextProcessor processor) {
		AttributesMapperCallbackHandler<T> handler = new AttributesMapperCallbackHandler<T>(mapper);
		search(base, filter, controls, handler, processor);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, SearchControls controls, AttributesMapper<T> mapper,
			DirContextProcessor processor) {
		AttributesMapperCallbackHandler<T> handler = new AttributesMapperCallbackHandler<T>(mapper);
		search(base, filter, controls, handler, processor);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(String base, String filter, SearchControls controls, ContextMapper<T> mapper,
			DirContextProcessor processor) {
		assureReturnObjFlagSet(controls);
		ContextMapperCallbackHandler<T> handler = new ContextMapperCallbackHandler<T>(mapper);
		search(base, filter, controls, handler, processor);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(Name base, String filter, SearchControls controls, ContextMapper<T> mapper,
			DirContextProcessor processor) {
		assureReturnObjFlagSet(controls);
		ContextMapperCallbackHandler<T> handler = new ContextMapperCallbackHandler<T>(mapper);
		search(base, filter, controls, handler, processor);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void list(final String base, NameClassPairCallbackHandler handler) {
		SearchExecutor searchExecutor = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.list(base);
			}
		};

		search(searchExecutor, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void list(final Name base, NameClassPairCallbackHandler handler) {
		SearchExecutor searchExecutor = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.list(base);
			}
		};

		search(searchExecutor, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> list(String base, NameClassPairMapper<T> mapper) {
		CollectingNameClassPairCallbackHandler<T> handler = new MappingCollectingNameClassPairCallbackHandler<T>(
				mapper);
		list(base, handler);
		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> list(Name base, NameClassPairMapper<T> mapper) {
		CollectingNameClassPairCallbackHandler<T> handler = new MappingCollectingNameClassPairCallbackHandler<T>(
				mapper);
		list(base, handler);
		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> list(final Name base) {
		return list(base, new DefaultNameClassPairMapper());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> list(final String base) {
		return list(base, new DefaultNameClassPairMapper());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void listBindings(final String base, NameClassPairCallbackHandler handler) {
		SearchExecutor searchExecutor = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.listBindings(base);
			}
		};

		search(searchExecutor, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void listBindings(final Name base, NameClassPairCallbackHandler handler) {
		SearchExecutor searchExecutor = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
				return ctx.listBindings(base);
			}
		};

		search(searchExecutor, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listBindings(String base, NameClassPairMapper<T> mapper) {
		CollectingNameClassPairCallbackHandler<T> handler = new MappingCollectingNameClassPairCallbackHandler<T>(
				mapper);
		listBindings(base, handler);
		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listBindings(Name base, NameClassPairMapper<T> mapper) {
		CollectingNameClassPairCallbackHandler<T> handler = new MappingCollectingNameClassPairCallbackHandler<T>(
				mapper);
		listBindings(base, handler);
		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listBindings(final String base) {
		return listBindings(base, new DefaultNameClassPairMapper());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listBindings(final Name base) {
		return listBindings(base, new DefaultNameClassPairMapper());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listBindings(String base, ContextMapper<T> mapper) {
		ContextMapperCallbackHandler<T> handler = new ContextMapperCallbackHandler<T>(mapper);
		listBindings(base, handler);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listBindings(Name base, ContextMapper<T> mapper) {
		ContextMapperCallbackHandler<T> handler = new ContextMapperCallbackHandler<T>(mapper);
		listBindings(base, handler);

		return handler.getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T executeReadOnly(ContextExecutor<T> ce) {
		DirContext ctx = this.contextSource.getReadOnlyContext();
		return executeWithContext(ce, ctx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T executeReadWrite(ContextExecutor<T> ce) {
		DirContext ctx = this.contextSource.getReadWriteContext();
		return executeWithContext(ce, ctx);
	}

	private <T> T executeWithContext(ContextExecutor<T> ce, DirContext ctx) {
		try {
			return ce.executeWithContext(ctx);
		}
		catch (javax.naming.NamingException ex) {
			throw LdapUtils.convertLdapException(ex);
		}
		finally {
			closeContext(ctx);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object lookup(final Name dn) {
		return executeReadOnly(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				return ctx.lookup(dn);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object lookup(final String dn) {
		return executeReadOnly(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				return ctx.lookup(dn);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final Name dn, final AttributesMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes attributes = ctx.getAttributes(dn);
				return mapper.mapFromAttributes(attributes);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final String dn, final AttributesMapper<T> mapper) {

		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes attributes = ctx.getAttributes(dn);
				return mapper.mapFromAttributes(attributes);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final Name dn, final ContextMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Object object = ctx.lookup(dn);
				return mapper.mapFromContext(object);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final String dn, final ContextMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Object object = ctx.lookup(dn);
				return mapper.mapFromContext(object);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final Name dn, final String[] attributes, final AttributesMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes filteredAttributes = ctx.getAttributes(dn, attributes);
				return mapper.mapFromAttributes(filteredAttributes);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final String dn, final String[] attributes, final AttributesMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes filteredAttributes = ctx.getAttributes(dn, attributes);
				return mapper.mapFromAttributes(filteredAttributes);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final Name dn, final String[] attributes, final ContextMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes filteredAttributes = ctx.getAttributes(dn, attributes);
				DirContextAdapter contextAdapter = new DirContextAdapter(filteredAttributes, dn);
				return mapper.mapFromContext(contextAdapter);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T lookup(final String dn, final String[] attributes, final ContextMapper<T> mapper) {
		return executeReadOnly(new ContextExecutor<T>() {
			public T executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				Attributes filteredAttributes = ctx.getAttributes(dn, attributes);
				LdapName name = LdapUtils.newLdapName(dn);
				DirContextAdapter contextAdapter = new DirContextAdapter(filteredAttributes, name);
				return mapper.mapFromContext(contextAdapter);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(final Name dn, final ModificationItem[] mods) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.modifyAttributes(dn, mods);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(final String dn, final ModificationItem[] mods) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.modifyAttributes(dn, mods);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(final Name dn, final Object obj, final Attributes attributes) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.bind(dn, obj, attributes);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(final String dn, final Object obj, final Attributes attributes) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.bind(dn, obj, attributes);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(final Name dn) {
		doUnbind(dn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(final String dn) {
		doUnbind(dn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(final Name dn, boolean recursive) {
		if (!recursive) {
			doUnbind(dn);
		}
		else {
			doUnbindRecursively(dn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(final String dn, boolean recursive) {
		if (!recursive) {
			doUnbind(dn);
		}
		else {
			doUnbindRecursively(dn);
		}
	}

	private void doUnbind(final Name dn) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.unbind(dn);
				return null;
			}
		});
	}

	private void doUnbind(final String dn) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.unbind(dn);
				return null;
			}
		});
	}

	private void doUnbindRecursively(final Name dn) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) {
				deleteRecursively(ctx, LdapUtils.newLdapName(dn));
				return null;
			}
		});
	}

	private void doUnbindRecursively(final String dn) {
		executeReadWrite(new ContextExecutor<Object>() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				deleteRecursively(ctx, LdapUtils.newLdapName(dn));
				return null;
			}
		});
	}

	/**
	 * Delete all subcontexts including the current one recursively.
	 * @param ctx The context to use for deleting.
	 * @param name The starting point to delete recursively.
	 * @throws NamingException if any error occurs
	 */
	protected void deleteRecursively(DirContext ctx, Name name) {

		NamingEnumeration enumeration = null;
		try {
			enumeration = ctx.listBindings(name);
			while (enumeration.hasMore()) {
				Binding binding = (Binding) enumeration.next();
				LdapName childName = LdapUtils.newLdapName(binding.getName());
				childName.addAll(0, name);
				deleteRecursively(ctx, childName);
			}
			ctx.unbind(name);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entry " + name + " deleted");
			}
		}
		catch (javax.naming.NamingException ex) {
			throw LdapUtils.convertLdapException(ex);
		}
		finally {
			try {
				enumeration.close();
			}
			catch (Exception ex) {
				// Never mind this
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebind(final Name dn, final Object obj, final Attributes attributes) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.rebind(dn, obj, attributes);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebind(final String dn, final Object obj, final Attributes attributes) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.rebind(dn, obj, attributes);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rename(final Name oldDn, final Name newDn) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.rename(oldDn, newDn);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rename(final String oldDn, final String newDn) {
		executeReadWrite(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
				ctx.rename(oldDn, newDn);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.contextSource, "Property 'contextSource' must be set.");
	}

	private void closeContextAndNamingEnumeration(DirContext ctx, NamingEnumeration results) {

		closeNamingEnumeration(results);
		closeContext(ctx);
	}

	/**
	 * Close the supplied DirContext if it is not null. Swallow any exceptions, as this is
	 * only for cleanup.
	 * @param ctx the context to close.
	 */
	private void closeContext(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (Exception ex) {
				// Never mind this.
			}
		}
	}

	/**
	 * Close the supplied NamingEnumeration if it is not null. Swallow any exceptions, as
	 * this is only for cleanup.
	 * @param results the NamingEnumeration to close.
	 */
	private void closeNamingEnumeration(NamingEnumeration results) {
		if (results != null) {
			try {
				results.close();
			}
			catch (Exception ex) {
				// Never mind this.
			}
		}
	}

	private SearchControls getDefaultSearchControls(int searchScope, boolean returningObjFlag, String[] attrs) {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(searchScope);
		controls.setTimeLimit(this.defaultTimeLimit);
		controls.setCountLimit(this.defaultCountLimit);
		controls.setReturningObjFlag(returningObjFlag);
		controls.setReturningAttributes(attrs);
		return controls;
	}

	/**
	 * Make sure the returnObjFlag is set in the supplied SearchControls. Set it and log
	 * if it's not set.
	 * @param controls the SearchControls to check.
	 */
	private void assureReturnObjFlagSet(SearchControls controls) {
		Assert.notNull(controls, "controls must not be null");
		if (!controls.getReturningObjFlag()) {
			LOG.debug("The returnObjFlag of supplied SearchControls is not set"
					+ " but a ContextMapper is used - setting flag to true");
			controls.setReturningObjFlag(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContextOperations lookupContext(Name dn) {
		return (DirContextOperations) lookup(dn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContextOperations lookupContext(String dn) {
		return (DirContextOperations) lookup(dn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(DirContextOperations ctx) {
		Name dn = ctx.getDn();
		if (dn != null && ctx.isUpdateMode()) {
			modifyAttributes(dn, ctx.getModificationItems());
		}
		else {
			throw new IllegalStateException("The DirContextOperations instance needs to be properly initialized.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(DirContextOperations ctx) {
		Name dn = ctx.getDn();
		if (dn != null && !ctx.isUpdateMode()) {
			bind(dn, ctx, null);
		}
		else {
			throw new IllegalStateException("The DirContextOperations instance needs to be properly initialized.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebind(DirContextOperations ctx) {
		Name dn = ctx.getDn();
		if (dn != null && !ctx.isUpdateMode()) {
			rebind(dn, ctx, null);
		}
		else {
			throw new IllegalStateException("The DirContextOperations instance needs to be properly initialized.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(Name base, String filter, String password) {
		return authenticate(base, filter, password, new NullAuthenticatedLdapEntryContextCallback(),
				new NullAuthenticationErrorCallback());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(String base, String filter, String password) {
		return authenticate(LdapUtils.newLdapName(base), filter, password,
				new NullAuthenticatedLdapEntryContextCallback(), new NullAuthenticationErrorCallback());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(String base, String filter, String password,
			AuthenticatedLdapEntryContextCallback callback) {
		return authenticate(LdapUtils.newLdapName(base), filter, password, callback,
				new NullAuthenticationErrorCallback());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(Name base, String filter, String password,
			final AuthenticatedLdapEntryContextCallback callback) {
		return authenticate(base, filter, password, callback, new NullAuthenticationErrorCallback());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(String base, String filter, String password,
			AuthenticationErrorCallback errorCallback) {
		return authenticate(LdapUtils.newLdapName(base), filter, password,
				new NullAuthenticatedLdapEntryContextCallback(), errorCallback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(Name base, String filter, String password,
			final AuthenticationErrorCallback errorCallback) {
		return authenticate(base, filter, password, new NullAuthenticatedLdapEntryContextCallback(), errorCallback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(String base, String filter, String password,
			final AuthenticatedLdapEntryContextCallback callback, final AuthenticationErrorCallback errorCallback) {
		return authenticate(LdapUtils.newLdapName(base), filter, password, callback, errorCallback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(Name base, String filter, String password,
			final AuthenticatedLdapEntryContextCallback callback, final AuthenticationErrorCallback errorCallback) {

		return authenticate(base, filter, password,
				getDefaultSearchControls(this.defaultSearchScope, RETURN_OBJ_FLAG, null), callback, errorCallback)
						.isSuccess();
	}

	private AuthenticationStatus authenticate(Name base, String filter, String password, SearchControls searchControls,
			final AuthenticatedLdapEntryContextCallback callback, final AuthenticationErrorCallback errorCallback) {

		List<LdapEntryIdentification> result = search(base, filter, searchControls,
				new LdapEntryIdentificationContextMapper());
		if (result.size() == 0) {
			String msg = "No results found for search, base: '" + base + "'; filter: '" + filter + "'.";
			LOG.info(msg);
			return AuthenticationStatus.EMPTYRESULT;
		}
		else if (result.size() > 1) {
			String msg = "base: '" + base + "'; filter: '" + filter + "'.";
			throw new IncorrectResultSizeDataAccessException(msg, 1, result.size());
		}

		final LdapEntryIdentification entryIdentification = result.get(0);

		try {
			DirContext ctx = this.contextSource.getContext(entryIdentification.getAbsoluteName().toString(), password);
			executeWithContext(new ContextExecutor<Object>() {
				public Object executeWithContext(DirContext ctx) throws javax.naming.NamingException {
					callback.executeWithContext(ctx, entryIdentification);
					return null;
				}
			}, ctx);
			return AuthenticationStatus.SUCCESS;
		}
		catch (Exception ex) {
			LOG.debug("Authentication failed for entry with DN '" + entryIdentification.getAbsoluteName() + "'", ex);
			errorCallback.execute(ex);
			return AuthenticationStatus.UNDEFINED_FAILURE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T authenticate(LdapQuery query, String password, AuthenticatedLdapEntryContextMapper<T> mapper) {
		SearchControls searchControls = searchControlsForQuery(query, RETURN_OBJ_FLAG);
		ReturningAuthenticatedLdapEntryContext<T> mapperCallback = new ReturningAuthenticatedLdapEntryContext<T>(
				mapper);
		CollectingAuthenticationErrorCallback errorCallback = new CollectingAuthenticationErrorCallback();

		AuthenticationStatus authenticationStatus = authenticate(query.base(), query.filter().encode(), password,
				searchControls, mapperCallback, errorCallback);

		if (errorCallback.hasError()) {
			Exception error = errorCallback.getError();

			if (error instanceof NamingException) {
				throw (NamingException) error;
			}
			else {
				throw new UncategorizedLdapException(error);
			}
		}
		else if (AuthenticationStatus.EMPTYRESULT == authenticationStatus) {
			throw new EmptyResultDataAccessException(1);
		}
		else if (!authenticationStatus.isSuccess()) {
			throw new AuthenticationException();
		}

		return mapperCallback.collectedObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void authenticate(LdapQuery query, String password) {
		authenticate(query, password, new NullAuthenticatedLdapEntryContextCallback());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T searchForObject(Name base, String filter, ContextMapper<T> mapper) {
		return searchForObject(base, filter,
				getDefaultSearchControls(this.defaultSearchScope, RETURN_OBJ_FLAG, ALL_ATTRIBUTES), mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T searchForObject(String base, String filter, ContextMapper<T> mapper) {
		return searchForObject(LdapUtils.newLdapName(base), filter, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T searchForObject(Name base, String filter, SearchControls searchControls, ContextMapper<T> mapper) {
		List<T> result = search(base, filter, searchControls, mapper);

		if (result.size() == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		else if (result.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, result.size());
		}

		return result.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T searchForObject(String base, String filter, SearchControls searchControls, ContextMapper<T> mapper) {
		return searchForObject(LdapUtils.newLdapName(base), filter, searchControls, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void search(LdapQuery query, NameClassPairCallbackHandler callbackHandler) {
		SearchControls searchControls = searchControlsForQuery(query, DONT_RETURN_OBJ_FLAG);
		search(query.base(), query.filter().encode(), searchControls, callbackHandler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(LdapQuery query, ContextMapper<T> mapper) {
		SearchControls searchControls = searchControlsForQuery(query, RETURN_OBJ_FLAG);

		return search(query.base(), query.filter().encode(), searchControls, mapper);

	}

	private SearchControls searchControlsForQuery(LdapQuery query, boolean returnObjFlag) {
		SearchControls searchControls = getDefaultSearchControls(this.defaultSearchScope, returnObjFlag,
				query.attributes());

		if (query.searchScope() != null) {
			searchControls.setSearchScope(query.searchScope().getId());
		}

		if (query.countLimit() != null) {
			searchControls.setCountLimit(query.countLimit());
		}

		if (query.timeLimit() != null) {
			searchControls.setTimeLimit(query.timeLimit());
		}
		return searchControls;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> search(LdapQuery query, AttributesMapper<T> mapper) {
		SearchControls searchControls = searchControlsForQuery(query, DONT_RETURN_OBJ_FLAG);

		return search(query.base(), query.filter().encode(), searchControls, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContextOperations searchForContext(LdapQuery query) {
		return searchForObject(query, new ContextMapper<DirContextOperations>() {
			@Override
			public DirContextOperations mapFromContext(Object ctx) throws javax.naming.NamingException {
				return (DirContextOperations) ctx;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T searchForObject(LdapQuery query, ContextMapper<T> mapper) {
		SearchControls searchControls = searchControlsForQuery(query, DONT_RETURN_OBJ_FLAG);

		return searchForObject(query.base(), query.filter().encode(), searchControls, mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Stream<T> searchForStream(LdapQuery query, AttributesMapper<T> attributesMapper) {
		return searchForStream(query, (SearchResult result) -> {
			Attributes attributes = result.getAttributes();
			return unchecked(() -> attributesMapper.mapFromAttributes(attributes));
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Stream<T> searchForStream(LdapQuery query, ContextMapper<T> mapper) {
		return searchForStream(query, (SearchResult result) -> {
			Object object = result.getObject();
			if (object == null) {
				throw new ObjectRetrievalException("Binding did not contain any object.");
			}
			return unchecked(() -> mapper.mapFromContext(object));
		});
	}

	<T> Stream<T> searchForStream(LdapQuery query, Function<SearchResult, T> mapper) {
		Name base = query.base();
		Filter filter = query.filter();
		SearchControls searchControls = searchControlsForQuery(query, RETURN_OBJ_FLAG);
		DirContext ctx = this.contextSource.getReadOnlyContext();
		String encodedFilter = filter.encode();

		if (LOG.isDebugEnabled()) {
			LOG.debug(
					String.format("Searching - base=%1$s, finalFilter=%2$s, scope=%3$s", base, filter, searchControls));
		}

		assureReturnObjFlagSet(searchControls);

		NamingEnumeration<SearchResult> results = unchecked(() -> ctx.search(base, encodedFilter, searchControls));
		if (results == null) {
			return Stream.empty();
		}
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(CollectionUtils.toIterator(results), Spliterator.ORDERED),
						false)
				.map((nameClassPair) -> unchecked(() -> mapper.apply(nameClassPair))).filter(Objects::nonNull)
				.onClose(() -> closeContextAndNamingEnumeration(ctx, results));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T findByDn(Name dn, final Class<T> clazz) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Reading Entry at - %1$s", dn));
		}

		// Make sure the class is OK before doing the lookup
		String[] attributes = this.odm.manageClass(clazz);

		T result = lookup(dn, attributes, new ContextMapper<T>() {
			@Override
			public T mapFromContext(Object ctx) throws javax.naming.NamingException {
				return LdapTemplate.this.odm.mapFromLdapDataEntry((DirContextOperations) ctx, clazz);
			}
		});

		if (result == null) {
			throw new OdmException(String.format("Entry %1$s does not have the required objectclasses ", dn));
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Found entry - %1$s", result));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(Object entry) {
		Assert.notNull(entry, "Entry must not be null");

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Creating entry - %1$s", entry));
		}

		Name id = this.odm.getId(entry);
		if (id == null) {
			id = this.odm.getCalculatedId(entry);
			this.odm.setId(entry, id);
		}

		Assert.notNull(id, String.format("Unable to determine id for entry %s", entry.toString()));

		DirContextAdapter context = new DirContextAdapter(id);
		this.odm.mapToLdapDataEntry(entry, context);

		bind(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Object entry) {
		Assert.notNull(entry, "Entry must not be null");
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Updating entry - %1$s", entry));
		}

		Name originalId = this.odm.getId(entry);
		Name calculatedId = this.odm.getCalculatedId(entry);

		if (originalId != null && calculatedId != null && !originalId.equals(calculatedId)) {
			// The DN has changed - remove the original entry and bind the new one
			// (because other data may have changed as well
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format(
						"Calculated DN of %s; of entry %s differs from explicitly specified one; %s - moving",
						calculatedId, entry, originalId));
			}

			unbind(originalId);

			DirContextAdapter context = new DirContextAdapter(calculatedId);
			this.odm.mapToLdapDataEntry(entry, context);

			bind(context);
			this.odm.setId(entry, calculatedId);
		}
		else {
			// DN is the same, just modify the attributes

			Name id = originalId;
			if (id == null) {
				id = calculatedId;
				this.odm.setId(entry, calculatedId);
			}

			Assert.notNull(id, String.format("Unable to determine id for entry %s", entry.toString()));

			DirContextOperations context = lookupContext(id);
			this.odm.mapToLdapDataEntry(entry, context);
			modifyAttributes(context);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Object entry) {
		Assert.notNull(entry, "Entry must not be null");
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Deleting %1$s", entry));
		}

		Name id = this.odm.getId(entry);
		if (id == null) {
			id = this.odm.getCalculatedId(entry);
		}

		Assert.notNull(id, String.format("Unable to determine id for entry %s", entry.toString()));
		unbind(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> findAll(Name base, SearchControls searchControls, final Class<T> clazz) {
		return find(base, null, searchControls, clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> findAll(Class<T> clazz) {
		return findAll(LdapUtils.emptyLdapName(),
				getDefaultSearchControls(this.defaultSearchScope, RETURN_OBJ_FLAG, ALL_ATTRIBUTES), clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> find(Name base, Filter filter, SearchControls searchControls, final Class<T> clazz) {
		Filter finalFilter = this.odm.filterFor(clazz, filter);

		// Search from the root if we are not told where to search from
		Name localBase = base;
		if (base == null || base.size() == 0) {
			localBase = LdapUtils.emptyLdapName();
		}

		// extend search controls with the attributes to return
		if (searchControls.getReturningAttributes() == null) {
			String[] attributes = this.odm.manageClass(clazz);
			searchControls.setReturningAttributes(attributes);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Searching - base=%1$s, finalFilter=%2$s, scope=%3$s", base, finalFilter,
					searchControls));
		}

		List<T> result = search(localBase, finalFilter.encode(), searchControls, new ContextMapper<T>() {
			@Override
			public T mapFromContext(Object ctx) throws javax.naming.NamingException {
				return LdapTemplate.this.odm.mapFromLdapDataEntry((DirContextOperations) ctx, clazz);
			}
		});
		result.remove(null);

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Found %1$s Entries - %2$s", result.size(), result));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> find(LdapQuery query, Class<T> clazz) {
		SearchControls searchControls = searchControlsForQuery(query, RETURN_OBJ_FLAG);
		return find(query.base(), query.filter(), searchControls, clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T findOne(LdapQuery query, Class<T> clazz) {
		List<T> result = find(query, clazz);

		if (result.size() == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		else if (result.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, result.size());
		}

		return result.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Stream<T> findForStream(LdapQuery query, Class<T> clazz) {
		LdapQueryBuilder builder = LdapQueryBuilder.fromQuery(query);
		if (query.attributes() == null) {
			String[] attributes = this.odm.manageClass(clazz);
			builder.attributes(attributes);
		}
		Filter includeClass = this.odm.filterFor(clazz, query.filter());
		ContextMapper<T> contextMapper = (object) -> this.odm.mapFromLdapDataEntry((DirContextOperations) object,
				clazz);
		return searchForStream(builder.filter(includeClass), contextMapper);
	}

	private <T> T unchecked(CheckedSupplier<T> supplier) {
		try {
			return supplier.get();
		}
		catch (NameNotFoundException ex) {
			// It is possible to ignore errors caused by base not found
			if (!this.ignoreNameNotFoundException) {
				throw LdapUtils.convertLdapException(ex);
			}
			LOG.warn("Base context not found, ignoring: " + ex.getMessage());
		}
		catch (PartialResultException ex) {
			// Workaround for AD servers not handling referrals correctly.
			if (!this.ignorePartialResultException) {
				throw LdapUtils.convertLdapException(ex);
			}
			LOG.debug("PartialResultException encountered and ignored", ex);
		}
		catch (SizeLimitExceededException ex) {
			if (!this.ignoreSizeLimitExceededException) {
				throw LdapUtils.convertLdapException(ex);
			}
			LOG.debug("SizeLimitExceededException encountered and ignored", ex);
		}
		catch (javax.naming.NamingException ex) {
			throw LdapUtils.convertLdapException(ex);
		}
		return null;
	}

	private interface CheckedSupplier<T> {

		T get() throws javax.naming.NamingException;

	}

	/**
	 * The status of an authentication attempt.
	 *
	 * @author Rob Winch
	 */
	private enum AuthenticationStatus {

		/**
		 * Authentication was successful
		 */
		SUCCESS(true),
		/**
		 * The user was not found
		 */
		EMPTYRESULT(false),
		/**
		 * Authentication failed for other reason
		 */
		UNDEFINED_FAILURE(false);

		private boolean success;

		AuthenticationStatus(boolean success) {
			this.success = success;
		}

		/**
		 * Return true if the authentication attempt was successful
		 * @return
		 */
		public boolean isSuccess() {
			return this.success;
		}

	}

	private static final class NullAuthenticatedLdapEntryContextCallback
			implements AuthenticatedLdapEntryContextCallback, AuthenticatedLdapEntryContextMapper<Object> {

		public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
			// Do nothing
		}

		@Override
		public Object mapWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
			return null;
		}

	}

	/**
	 * Do-nothing implementation of {@link DirContextProcessor}.
	 *
	 * @author Mattias Hellborg Arthursson
	 * @since 2.0
	 */
	public static final class NullDirContextProcessor implements DirContextProcessor {

		public void postProcess(DirContext ctx) {
			// Do nothing
		}

		public void preProcess(DirContext ctx) {
			// Do nothing
		}

	}

	/**
	 * A {@link NameClassPairCallbackHandler} that passes the NameClassPairs found to a
	 * NameClassPairMapper and collects the results in a list.
	 *
	 * @author Mattias Hellborg Arthursson
	 */
	public final static class MappingCollectingNameClassPairCallbackHandler<T>
			extends CollectingNameClassPairCallbackHandler<T> {

		private NameClassPairMapper<T> mapper;

		public MappingCollectingNameClassPairCallbackHandler(NameClassPairMapper<T> mapper) {
			this.mapper = mapper;
		}

		/**
		 * {@inheritDoc}
		 */
		public T getObjectFromNameClassPair(NameClassPair nameClassPair) {
			try {
				return this.mapper.mapFromNameClassPair(nameClassPair);
			}
			catch (javax.naming.NamingException ex) {
				throw LdapUtils.convertLdapException(ex);
			}
		}

	}

	private static final class NullAuthenticationErrorCallback implements AuthenticationErrorCallback {

		public void execute(Exception ex) {
			// Do nothing
		}

	}

	private static final class ReturningAuthenticatedLdapEntryContext<T>
			implements AuthenticatedLdapEntryContextCallback {

		private final AuthenticatedLdapEntryContextMapper<T> mapper;

		private T collectedObject;

		private ReturningAuthenticatedLdapEntryContext(AuthenticatedLdapEntryContextMapper<T> mapper) {
			this.mapper = mapper;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
			this.collectedObject = this.mapper.mapWithContext(ctx, ldapEntryIdentification);
		}

	}

}
