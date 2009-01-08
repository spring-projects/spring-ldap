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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * DirContextProcessor implementation for managing the SortControl. Note that
 * this class is stateful, so a new instance needs to be instantiated for each
 * new search.
 * 
 * @author Ulrik Sandberg
 */
public class SortControlDirContextProcessor extends AbstractRequestControlDirContextProcessor {

	private static final boolean CRITICAL_CONTROL = true;

	private static final String DEFAULT_REQUEST_CONTROL = "javax.naming.ldap.SortControl";

	private static final String LDAPBP_REQUEST_CONTROL = "com.sun.jndi.ldap.ctl.SortControl";

	private static final String DEFAULT_RESPONSE_CONTROL = "javax.naming.ldap.SortResponseControl";

	private static final String LDAPBP_RESPONSE_CONTROL = "com.sun.jndi.ldap.ctl.SortResponseControl";

	/**
	 * What key to sort on.
	 */
	private String sortKey;

	/**
	 * Whether the search result actually was sorted.
	 */
	private boolean sorted;

	/**
	 * The result code of the supposedly sorted search.
	 */
	private int resultCode;

	private Class responseControlClass;

	private Class requestControlClass;

	private boolean critical = CRITICAL_CONTROL;

	/**
	 * Constructs a new instance using the supplied sort key.
	 * 
	 * @param sortKey the sort key, i.e. the attribute name to sort on.
	 */
	public SortControlDirContextProcessor(String sortKey) {
		this.sortKey = sortKey;
		setSorted(false);
		setResultCode(-1);

		loadControlClasses();
	}

	private void loadControlClasses() {
		try {
			requestControlClass = Class.forName(DEFAULT_REQUEST_CONTROL);
			responseControlClass = Class.forName(DEFAULT_RESPONSE_CONTROL);
		}
		catch (ClassNotFoundException e) {
			log.debug("Default control classes not found - falling back to LdapBP classes", e);

			try {
				requestControlClass = Class.forName(LDAPBP_REQUEST_CONTROL);
				responseControlClass = Class.forName(LDAPBP_RESPONSE_CONTROL);
			}
			catch (ClassNotFoundException e1) {
				throw new UncategorizedLdapException(
						"Neither default nor fallback classes are available - unable to proceed", e);
			}
		}
	}

	/**
	 * Set the class of the expected ResponseControl for the sorted result
	 * response.
	 * 
	 * @param responseControlClass Class of the expected response control.
	 */
	public void setResponseControlClass(Class responseControlClass) {
		this.responseControlClass = responseControlClass;
	}

	public void setRequestControlClass(Class requestControlClass) {
		this.requestControlClass = requestControlClass;
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

	private void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	/**
	 * Get the result code returned by the control.
	 * 
	 * @return result code.
	 */
	public int getResultCode() {
		return resultCode;
	}

	private void setResultCode(int sortResult) {
		this.resultCode = sortResult;
	}

	/**
	 * Get the sort key.
	 * 
	 * @return the sort key.
	 */
	public String getSortKey() {
		return sortKey;
	}

	/**
	 * Set the sort key, i.e. the attribute on which to sort on.
	 * 
	 * @param sortKey the sort key.
	 */
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	/*
	 * @see
	 * org.springframework.ldap.control.AbstractRequestControlDirContextProcessor
	 * #createRequestControl()
	 */
	public Control createRequestControl() {
		Constructor constructor = ClassUtils.getConstructorIfAvailable(requestControlClass, new Class[] {
				String[].class, boolean.class });
		if (constructor == null) {
			throw new IllegalArgumentException("Failed to find an appropriate RequestControl constructor");
		}

		Control result = null;
		try {
			result = (Control) constructor.newInstance(new Object[] { new String[] { sortKey },
					Boolean.valueOf(critical) });
		}
		catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}

		return result;
	}

	/*
	 * @see
	 * org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming
	 * .directory.DirContext)
	 */
	public void postProcess(DirContext ctx) throws NamingException {

		LdapContext ldapContext = (LdapContext) ctx;
		Control[] responseControls = ldapContext.getResponseControls();
		if (responseControls == null) {
			responseControls = new Control[0];
		}

		// Go through response controls and get info, regardless of class
		for (int i = 0; i < responseControls.length; i++) {
			Control responseControl = responseControls[i];

			// check for match, try fallback otherwise
			if (responseControl.getClass().isAssignableFrom(responseControlClass)) {
				Object control = responseControl;
				Boolean result = (Boolean) invokeMethod("isSorted", responseControlClass, control);
				setSorted(result.booleanValue());
				Integer code = (Integer) invokeMethod("getResultCode", responseControlClass, control);
				setResultCode(code.intValue());
				return;
			}
		}

		log.fatal("No matching response control found for paged results - looking for '" + responseControlClass);
	}

	private Object invokeMethod(String method, Class clazz, Object control) {
		Method actualMethod = ReflectionUtils.findMethod(clazz, method);
		return ReflectionUtils.invokeMethod(actualMethod, control);
	}
}
