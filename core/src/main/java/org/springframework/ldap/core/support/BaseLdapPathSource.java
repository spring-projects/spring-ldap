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

package org.springframework.ldap.core.support;

import javax.naming.ldap.LdapName;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;

/**
 * Implementations of this interface are capable of providing a base LDAP path. The base
 * LDAP path is the root path to which all LDAP operations performed on a particular
 * context are relative.
 *
 * @see ContextSource
 * @author Mattias Hellborg Arthursson
 */
public interface BaseLdapPathSource {

	/**
	 * Get the base LDAP path as a {@link DistinguishedName}.
	 * @return the base LDAP path as a {@link DistinguishedName}. The path will be empty
	 * if no base path is specified.
	 * @deprecated {@link DistinguishedName} and associated classes and methods are
	 * deprecated as of 2.0. Use {@link #getBaseLdapName()} instead.
	 */
	DistinguishedName getBaseLdapPath();

	/**
	 * Get the base LDAP path as a {@link LdapName}.
	 * @return the base LDAP path as a {@link LdapName}. The path will be empty if no base
	 * path is specified.
	 * @since 2.0
	 */
	LdapName getBaseLdapName();

	/**
	 * Get the base LDAP path as a String.
	 * @return the base LDAP path as a An empty String will be returned if no base path is
	 * specified.
	 */
	String getBaseLdapPathAsString();

}
