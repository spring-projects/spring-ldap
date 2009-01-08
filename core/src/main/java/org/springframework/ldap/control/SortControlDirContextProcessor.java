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
 * DirContextProcessor implementation for managing the SortControl. Note that
 * this class is stateful, so a new instance needs to be instantiated for each
 * new search.
 * 
 * @author Ulrik Sandberg
 */
public class SortControlDirContextProcessor extends AbstractFallbackRequestAndResponseControlDirContextProcessor {

	private static final String DEFAULT_REQUEST_CONTROL = "javax.naming.ldap.SortControl";

	private static final String FALLBACK_REQUEST_CONTROL = "com.sun.jndi.ldap.ctl.SortControl";

	private static final String DEFAULT_RESPONSE_CONTROL = "javax.naming.ldap.SortResponseControl";

	private static final String FALLBACK_RESPONSE_CONTROL = "com.sun.jndi.ldap.ctl.SortResponseControl";

	/**
	 * What key to sort on.
	 */
	String sortKey;

	/**
	 * Whether the search result actually was sorted.
	 */
	private boolean sorted;

	/**
	 * The result code of the supposedly sorted search.
	 */
	private int resultCode;

	/**
	 * Constructs a new instance using the supplied sort key.
	 * 
	 * @param sortKey the sort key, i.e. the attribute name to sort on.
	 */
	public SortControlDirContextProcessor(String sortKey) {
		this.sortKey = sortKey;
		this.sorted = false;
		this.resultCode = -1;

		defaultRequestControl = DEFAULT_REQUEST_CONTROL;
		defaultResponseControl = DEFAULT_RESPONSE_CONTROL;

		fallbackRequestControl = FALLBACK_REQUEST_CONTROL;
		fallbackResponseControl = FALLBACK_RESPONSE_CONTROL;

		loadControlClasses();
	}

	/**
	 * Check whether the returned values were actually sorted by the server.
	 * 
	 * @return <code>true</code> if the result was sorted, <code>false</code>
	 * otherwise.
	 */
	public boolean isSorted() {
		return sorted;
	}

	/**
	 * Get the result code returned by the control.
	 * 
	 * @return result code.
	 */
	public int getResultCode() {
		return resultCode;
	}

	/**
	 * Get the sort key.
	 * 
	 * @return the sort key.
	 */
	public String getSortKey() {
		return sortKey;
	}

	/*
	 * @see
	 * org.springframework.ldap.control.AbstractRequestControlDirContextProcessor
	 * #createRequestControl()
	 */
	public Control createRequestControl() {
		return super.createRequestControl(new Class[] { String[].class, boolean.class }, new Object[] {
				new String[] { sortKey }, Boolean.valueOf(critical) });
	}

	/*
	 * @see org.springframework.ldap.control.
	 * AbstractFallbackRequestAndResponseControlDirContextProcessor
	 * #handleResponse(java.lang.Object)
	 */
	protected void handleResponse(Object control) {
		Boolean result = (Boolean) invokeMethod("isSorted", responseControlClass, control);
		this.sorted = result.booleanValue();
		Integer code = (Integer) invokeMethod("getResultCode", responseControlClass, control);
		this.resultCode = code.intValue();
	}
}
