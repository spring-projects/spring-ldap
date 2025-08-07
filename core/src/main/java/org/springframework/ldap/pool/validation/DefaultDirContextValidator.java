/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.pool.validation;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.pool.DirContextType;
import org.springframework.util.Assert;

/**
 * Default {@link DirContext} validator that executes
 * {@link DirContext#search(String, String, SearchControls)}. The name, filter and
 * {@link SearchControls} are all configurable. There is no special handling for read only
 * versus read write {@link DirContext}s.
 *
 * <br>
 * <br>
 * Configuration:
 * <table border="1" summary="Configuration">
 * <tr>
 * <th align="left">Property</th>
 * <th align="left">Description</th>
 * <th align="left">Required</th>
 * <th align="left">Default</th>
 * </tr>
 * <tr>
 * <td valign="top">base</td>
 * <td valign="top">The name parameter to the search method.</td>
 * <td valign="top">No</td>
 * <td valign="top">""</td>
 * </tr>
 * <tr>
 * <td valign="top">filter</td>
 * <td valign="top">The filter parameter to the search method.</td>
 * <td valign="top">No</td>
 * <td valign="top">"objectclass=*"</td>
 * </tr>
 * <tr>
 * <td valign="top">searchControls</td>
 * <td valign="top">The {@link SearchControls} parameter to the search method.</td>
 * <td valign="top">No</td>
 * <td valign="top">{@link SearchControls#setCountLimit(long)} = 1<br>
 * {@link SearchControls#setReturningAttributes(String[])} = new String[] { "objectclass"
 * }<br>
 * {@link SearchControls#setTimeLimit(int)} = 500</td>
 * </tr>
 * </table>
 *
 * @author Eric Dalquist
 */
public class DefaultDirContextValidator implements DirContextValidator {

	public static final String DEFAULT_FILTER = "objectclass=*";

	private static final int DEFAULT_TIME_LIMIT = 500;

	/**
	 * Logger for this class and sub-classes
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String base;

	private String filter;

	private SearchControls searchControls;

	/**
	 * Create the default validator, creates {@link SearchControls} with search scope
	 * <code>OBJECT_SCOPE</code>, a countLimit of 1, returningAttributes of objectclass
	 * and timeLimit of 500. The default base is an empty string and the default filter is
	 * objectclass=*
	 */
	public DefaultDirContextValidator() {
		this(SearchControls.OBJECT_SCOPE);
	}

	/**
	 * Create a validator with all the defaults of the default constructor, but with the
	 * search scope set to the referred value.
	 * @param searchScope The searchScope to be set in the default
	 * <code>SearchControls</code>
	 */
	public DefaultDirContextValidator(int searchScope) {
		this.searchControls = new SearchControls();
		this.searchControls.setSearchScope(searchScope);
		this.searchControls.setCountLimit(1);
		this.searchControls.setReturningAttributes(new String[] { "objectclass" });
		this.searchControls.setTimeLimit(DEFAULT_TIME_LIMIT);

		this.base = "";

		this.filter = DEFAULT_FILTER;
	}

	/**
	 * @return the baseName
	 */
	public String getBase() {
		return this.base;
	}

	/**
	 * @param base the baseName to set
	 */
	public void setBase(String base) {
		this.base = base;
	}

	/**
	 * @return the filter
	 */
	public String getFilter() {
		return this.filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(String filter) {
		if (filter == null) {
			throw new IllegalArgumentException("filter may not be null");
		}

		this.filter = filter;
	}

	/**
	 * @return the searchControls
	 */
	public SearchControls getSearchControls() {
		return this.searchControls;
	}

	/**
	 * @param searchControls the searchControls to set
	 */
	public void setSearchControls(SearchControls searchControls) {
		if (searchControls == null) {
			throw new IllegalArgumentException("searchControls may not be null");
		}

		this.searchControls = searchControls;
	}

	/**
	 * @see DirContextValidator#validateDirContext(DirContextType,
	 * javax.naming.directory.DirContext)
	 */
	public boolean validateDirContext(DirContextType contextType, DirContext dirContext) {
		Assert.notNull(contextType, "contextType may not be null");
		Assert.notNull(dirContext, "dirContext may not be null");

		NamingEnumeration<SearchResult> searchResults = null;
		try {
			searchResults = dirContext.search(this.base, this.filter, this.searchControls);

			if (searchResults.hasMore()) {
				this.logger.debug("DirContext '{}' passed validation.", dirContext);

				return true;
			}
		}
		catch (Exception ex) {
			this.logger.debug("DirContext '{}' failed validation with an exception.", dirContext, ex);
			return false;
		}
		finally {
			if (searchResults != null) {
				try {
					searchResults.close();
				}
				catch (NamingException ignored) {
				}
			}
		}

		this.logger.debug("DirContext '{}' failed validation.", dirContext);
		return false;
	}

}
