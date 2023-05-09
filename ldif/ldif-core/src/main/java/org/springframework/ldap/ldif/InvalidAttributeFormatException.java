/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ldap.ldif;

import org.springframework.ldap.NamingException;

/**
 * Thrown whenever a parsed attribute does not conform to LDAP specifications.
 *
 * @author Keith Barlow
 *
 */
public class InvalidAttributeFormatException extends NamingException {

	private static final long serialVersionUID = -4529380160785322985L;

	/**
	 * @param msg
	 */
	public InvalidAttributeFormatException(String msg) {
		super(msg);
	}

	/**
	 * @param cause
	 */
	public InvalidAttributeFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public InvalidAttributeFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
