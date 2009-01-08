/*
 * Copyright 2005-2008 the original author or authors.
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

package org.springframework.ldap.control;

import javax.naming.ldap.Control;

/**
 * DirContextProcessor implementation for managing the paged results control.
 * Note that due to the internal workings of <code>LdapTemplate</code>, the
 * target connection is closed after each LDAP call. The PagedResults control
 * require the same connection be used for each call, which means we need to
 * make sure the target connection is never actually closed. There's basically
 * two ways of making this happen: use the <code>SingleContextSource</code>
 * implementation or make sure all calls happen within a single LDAP transaction
 * (using <code>ContextSourceTransactionManager</code>).
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultsDirContextProcessor extends AbstractFallbackRequestAndResponseControlDirContextProcessor {

	private static final String DEFAULT_REQUEST_CONTROL = "javax.naming.ldap.PagedResultsControl";

	private static final String FALLBACK_REQUEST_CONTROL = "com.sun.jndi.ldap.ctl.PagedResultsControl";

	private static final String DEFAULT_RESPONSE_CONTROL = "javax.naming.ldap.PagedResultsResponseControl";

	private static final String FALLBACK_RESPONSE_CONTROL = "com.sun.jndi.ldap.ctl.PagedResultsResponseControl";

	private int pageSize;

	private PagedResultsCookie cookie;

	private int resultSize;

	/**
	 * Constructs a new instance. This constructor should be used when
	 * performing the first paged search operation, when no other results have
	 * been retrieved.
	 * 
	 * @param pageSize the page size.
	 */
	public PagedResultsDirContextProcessor(int pageSize) {
		this(pageSize, null);
	}

	/**
	 * Constructs a new instance with the supplied page size and cookie. The
	 * cookie must be the exact same instance as received from a previous paged
	 * results search, or <code>null</code> if it is the first in an operation
	 * sequence.
	 * 
	 * @param pageSize the page size.
	 * @param cookie the cookie, as received from a previous search.
	 */
	public PagedResultsDirContextProcessor(int pageSize, PagedResultsCookie cookie) {
		this.pageSize = pageSize;
		this.cookie = cookie;

		defaultRequestControl = DEFAULT_REQUEST_CONTROL;
		defaultResponseControl = DEFAULT_RESPONSE_CONTROL;
		fallbackRequestControl = FALLBACK_REQUEST_CONTROL;
		fallbackResponseControl = FALLBACK_RESPONSE_CONTROL;
		
		loadControlClasses();
	}

	/**
	 * Get the cookie.
	 * 
	 * @return the cookie.
	 */
	public PagedResultsCookie getCookie() {
		return cookie;
	}

	/**
	 * Get the page size.
	 * 
	 * @return the page size.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Get the total estimated number of entries that matches the issued search.
	 * Note that this value is optional for the LDAP server to return, so it
	 * does not always contain any valid data.
	 * 
	 * @return the estimated result size, if returned from the server.
	 */
	public int getResultSize() {
		return resultSize;
	}

	/*
	 * @see
	 * org.springframework.ldap.control.AbstractRequestControlDirContextProcessor
	 * #createRequestControl()
	 */
	public Control createRequestControl() {
		byte[] actualCookie = null;
		if (cookie != null) {
			actualCookie = cookie.getCookie();
		}
		return super.createRequestControl(new Class[] { int.class, byte[].class, boolean.class }, 
				new Object[] {new Integer(pageSize), actualCookie, Boolean.valueOf(critical)});
	}

	/*
	 * @seeorg.springframework.ldap.control.
	 * AbstractFallbackRequestAndResponseControlDirContextProcessor
	 * #handleResponse(java.lang.Object)
	 */
	protected void handleResponse(Object control) {
		byte[] result = (byte[]) invokeMethod("getCookie", responseControlClass, control);
		this.cookie = new PagedResultsCookie(result);
		Integer wrapper = (Integer) invokeMethod("getResultSize", responseControlClass, control);
		this.resultSize = wrapper.intValue();
	}
}
