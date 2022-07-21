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

package org.springframework.ldap.core;

import org.springframework.ldap.NamingException;

import javax.naming.directory.DirContext;

/**
 * A <code>ContextSource</code> is responsible for configuring and creating
 * <code>DirContext</code> instances. It is typically used from
 * {@link LdapTemplate} to acquiring contexts for LDAP operations, but may be
 * used standalone to perform LDAP authentication.
 * 
 * @see org.springframework.ldap.core.LdapTemplate
 * 
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 */
public interface ContextSource {

	/**
	 * Gets a read-only <code>DirContext</code>. The returned
	 * <code>DirContext</code> must be possible to perform read-only operations
	 * on.
	 * 
	 * @return A DirContext instance, never null.
	 * @throws NamingException if some error occurs creating an DirContext.
	 */
	DirContext getReadOnlyContext() throws NamingException;

	/**
	 * Gets a read-write <code>DirContext</code> instance.
	 * 
	 * @return A <code>DirContext</code> instance, never <code>null</code>.
	 * @throws NamingException if some error occurs creating an
	 * <code>DirContext</code>.
	 */
	DirContext getReadWriteContext() throws NamingException;

	/**
	 * Gets a <code>DirContext</code> instance authenticated using the supplied
	 * principal and credentials. Typically to be used for plain authentication
	 * purposes. <strong>Note</strong> that this method will never make use
	 * of native Java LDAP pooling, even though this instance is configured to do so.
	 * This is to force password changes in the target directory to take effect
	 * as soon as possible.
	 *
	 * @param principal The principal (typically a distinguished name of a user
	 * in the LDAP tree) to use for authentication.
	 * @param credentials The credentials to use for authentication.
	 * @return an authenticated <code>DirContext</code> instance, never
	 * <code>null</code>.
	 * @since 1.3
	 */
	DirContext getContext(String principal, String credentials) throws NamingException;
}