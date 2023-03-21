/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.transaction.compensating;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.naming.Name;

/**
 * Utility methods for working with LDAP transactions.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public final class LdapTransactionUtils {

	public static final String REBIND_METHOD_NAME = "rebind";

	public static final String BIND_METHOD_NAME = "bind";

	public static final String RENAME_METHOD_NAME = "rename";

	public static final String UNBIND_METHOD_NAME = "unbind";

	public static final String MODIFY_ATTRIBUTES_METHOD_NAME = "modifyAttributes";

	/**
	 * Not to be instantiated.
	 */
	private LdapTransactionUtils() {

	}

	/**
	 * Get the first parameter in the argument list as a Name.
	 * @param args arguments supplied to a ldap operation.
	 * @return a Name representation of the first argument, or the Name itself if it is a
	 * name.
	 */
	public static Name getFirstArgumentAsName(Object[] args) {
		Assert.notEmpty(args);

		Object firstArg = args[0];
		return getArgumentAsName(firstArg);
	}

	/**
	 * Get the argument as a Name.
	 * @param arg an argument supplied to an Ldap operation.
	 * @return a Name representation of the argument, or the Name itself if it is a Name.
	 */
	public static Name getArgumentAsName(Object arg) {
		if (arg instanceof String) {
			return LdapUtils.newLdapName((String) arg);
		}
		else if (arg instanceof Name) {
			return (Name) arg;
		}
		else {
			throw new IllegalArgumentException("First argument needs to be a Name or a String representation thereof");
		}
	}

	/**
	 * Check whether the supplied method is a method for which transactions is supported
	 * (and which should be recorded for possible rollback later).
	 * @param methodName name of the method to check.
	 * @return <code>true</code> if this is a supported transaction operation,
	 * <code>false</code> otherwise.
	 */
	public static boolean isSupportedWriteTransactionOperation(String methodName) {
		return (ObjectUtils.nullSafeEquals(methodName, BIND_METHOD_NAME)
				|| ObjectUtils.nullSafeEquals(methodName, REBIND_METHOD_NAME)
				|| ObjectUtils.nullSafeEquals(methodName, RENAME_METHOD_NAME)
				|| ObjectUtils.nullSafeEquals(methodName, MODIFY_ATTRIBUTES_METHOD_NAME)
				|| ObjectUtils.nullSafeEquals(methodName, UNBIND_METHOD_NAME));

	}

}
