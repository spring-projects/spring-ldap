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

package org.springframework.ldap.control;

import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.springframework.ldap.core.DirContextProcessor;

/**
 * A specialized {@link DirContextProcessor} for managing LDAP paged results controls.
 * <p>
 * This processor simplifies working with paged LDAP search results as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc2696.html">RFC 2696</a>. It provides a
 * convenient {@link #hasMore()} method for determining if additional pages are available,
 * making it easy to iterate through all pages of results.
 * <p>
 * Paging requires that the same LDAP connection be used across each page. Spring LDAP's
 * {@link org.springframework.ldap.core.support.SingleContextSource} and
 * {@link org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy}
 * provide this capability when wired into
 * {@link org.springframework.ldap.core.LdapTemplate} and
 * {@link org.springframework.ldap.core.LdapClient} instances.
 *
 * @author Josh Cummings
 * @since 4.1
 * @see PagedResultsControl
 * @see PagedResultsControlExchange
 * @see org.springframework.ldap.core.support.SingleContextSource
 * @see org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager
 * @see org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy
 * @see <a href="https://www.rfc-editor.org/rfc/rfc2696.html">RFC 2696 - LDAP Control
 * Extension for Simple Paged Results Manipulation</a>
 */
public final class PagedResultsControlExchangeDirContextProcessor
		extends ControlExchangeDirContextProcessor<PagedResultsControl, PagedResultsResponseControl> {

	/**
	 * Constructs a paged results processor with the specified page size.
	 * @param pageSize the number of entries to return in each page
	 */
	public PagedResultsControlExchangeDirContextProcessor(int pageSize) {
		super(new PagedResultsControlExchange(pageSize));
	}

	/**
	 * Determines whether there are more pages of results available.
	 * <p>
	 * This method checks if the server returned a non-null cookie in the response
	 * control, which indicates that additional results are available.
	 * @return {@code true} if more pages are available, {@code false} otherwise
	 */
	public boolean hasMore() {
		return getExchange().getResponse() != null && getExchange().getResponse().getCookie() != null;
	}

}
