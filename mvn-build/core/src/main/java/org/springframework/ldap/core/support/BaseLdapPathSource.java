/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.core.support;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;

/**
 * Implementations of this interface are capable of providing a base LDAP path.
 * The base LDAP path is the root path to which all LDAP operations performed on
 * a particular context are relative.
 * 
 * @see ContextSource
 * 
 * @author Mattias Arthursson
 */
public interface BaseLdapPathSource {
	/**
	 * Get the base LDAP path as a {@link DistinguishedName}.
	 * 
	 * @return the base LDAP path as a {@link DistinguishedName}. The path will
	 * be empty if no base path is specified.
	 */
	DistinguishedName getBaseLdapPath();

	/**
	 * Get the base LDAP path as a String.
	 * 
	 * @return the base LDAP path as a An empty String will be returned if no
	 * base path is specified.
	 */
	String getBaseLdapPathAsString();
}
