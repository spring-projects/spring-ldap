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

import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.ldap.Control;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ReflectionUtils;

/**
 * DirContextProcessor implementation for managing a virtual list view.
 * <p>
 * This is the request control syntax:
 *
 * <pre>
 * VirtualListViewRequest ::= SEQUENCE {
 *		beforeCount	INTEGER (0..maxInt),
 *		afterCount	 INTEGER (0..maxInt),
 *		target	   CHOICE {
 *					   byOffset		[0] SEQUENCE {
 *							offset		  INTEGER (1 .. maxInt),
 *							contentCount	INTEGER (0 .. maxInt) },
 *					   greaterThanOrEqual [1] AssertionValue },
 *		contextID	 OCTET STRING OPTIONAL }
 * </pre>
 *
 * <p>
 * This is the response control syntax:
 *
 * <pre>
 * VirtualListViewResponse ::= SEQUENCE {
 *		targetPosition	INTEGER (0 .. maxInt),
 *		contentCount	 INTEGER (0 .. maxInt),
 *		virtualListViewResult ENUMERATED {
 *			 success (0),
 *			 operationsError (1),
 *			 protocolError (3),
 *			 unwillingToPerform (53),
 *			 insufficientAccessRights (50),
 *			 timeLimitExceeded (3),
 *			 adminLimitExceeded (11),
 *			 innapropriateMatching (18),
 *			 sortControlMissing (60),
 *			 offsetRangeError (61),
 *			 other(80),
 *			 ... },
 *		contextID	 OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author Ulrik Sandberg
 * @author Marius Scurtescu
 * @see <a href=
 * "https://www3.ietf.org/proceedings/02nov/I-D/draft-ietf-ldapext-ldapv3-vlv-09.txt">LDAP
 * Extensions for Scrolling View Browsing of Search Results</a>
 * @deprecated given that this draft was never finalized, please use
 * {@link ControlExchangeDirContextProcessor}, providing your own implementation of
 * {@link ControlExchange} with the control objects you choose to use
 */
@Deprecated
public class VirtualListViewControlDirContextProcessor
		extends AbstractFallbackRequestAndResponseControlDirContextProcessor {

	private static final String DEFAULT_REQUEST_CONTROL = "com.sun.jndi.ldap.ctl.VirtualListViewControl";

	private static final String DEFAULT_RESPONSE_CONTROL = "com.sun.jndi.ldap.ctl.VirtualListViewResponseControl";

	private static final boolean CRITICAL_CONTROL = true;

	private int pageSize;

	private VirtualListViewResultsCookie cookie;

	private int listSize;

	private int targetOffset;

	private NamingException exception;

	private boolean offsetPercentage;

	public VirtualListViewControlDirContextProcessor(int pageSize) {
		this(pageSize, 1, 0, null);
	}

	public VirtualListViewControlDirContextProcessor(int pageSize, int targetOffset, int listSize,
			VirtualListViewResultsCookie cookie) {
		this.pageSize = pageSize;
		this.targetOffset = targetOffset;
		this.listSize = listSize;
		this.cookie = cookie;

		defaultRequestControl = DEFAULT_REQUEST_CONTROL;
		defaultResponseControl = DEFAULT_RESPONSE_CONTROL;
		fallbackRequestControl = DEFAULT_REQUEST_CONTROL;
		fallbackResponseControl = DEFAULT_RESPONSE_CONTROL;

		loadControlClasses();
	}

	public VirtualListViewResultsCookie getCookie() {
		return this.cookie;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public int getListSize() {
		return this.listSize;
	}

	public NamingException getException() {
		return this.exception;
	}

	public int getTargetOffset() {
		return this.targetOffset;
	}

	/**
	 * Set whether the <code>targetOffset</code> should be interpreted as percentage of
	 * the list or an offset into the list.
	 * @param isPercentage <code>true</code> if targetOffset is a percentage
	 */
	public void setOffsetPercentage(boolean isPercentage) {
		this.offsetPercentage = isPercentage;
	}

	public boolean isOffsetPercentage() {
		return this.offsetPercentage;
	}

	/*
	 * @see org.springframework.ldap.control.AbstractRequestControlDirContextProcessor#
	 * createRequestControl()
	 */
	public Control createRequestControl() {
		Control control;

		if (this.offsetPercentage) {
			control = super.createRequestControl(new Class[] { int.class, int.class, boolean.class },
					new Object[] { Integer.valueOf(this.targetOffset), Integer.valueOf(this.pageSize),
							Boolean.valueOf(CRITICAL_CONTROL) });
		}
		else {
			control = super.createRequestControl(
					new Class[] { int.class, int.class, int.class, int.class, boolean.class },
					new Object[] { Integer.valueOf(this.targetOffset), Integer.valueOf(this.listSize),
							Integer.valueOf(0), Integer.valueOf(this.pageSize - 1),
							Boolean.valueOf(CRITICAL_CONTROL) });
		}

		if (this.cookie != null) {
			invokeMethod("setContextID", requestControlClass, control, new Class[] { byte[].class },
					new Object[] { this.cookie.getCookie() });
		}

		return control;
	}

	protected void handleResponse(Object control) {
		byte[] result = (byte[]) invokeMethod("getContextID", responseControlClass, control);
		Integer listSize = (Integer) invokeMethod("getListSize", responseControlClass, control);
		Integer targetOffset = (Integer) invokeMethod("getTargetOffset", responseControlClass, control);
		this.exception = (NamingException) invokeMethod("getException", responseControlClass, control);

		this.cookie = new VirtualListViewResultsCookie(result, targetOffset.intValue(), listSize.intValue());

		if (this.exception != null) {
			throw LdapUtils.convertLdapException(this.exception);
		}
	}

	protected static Object invokeMethod(String methodName, Class clazz, Object control, Class[] paramTypes,
			Object[] paramValues) {
		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);

		return ReflectionUtils.invokeMethod(method, control, paramValues);
	}

}
