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

package org.springframework.ldap.core.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;

/**
 * Authentication strategy for LDAP DIGEST-MD5 SASL mechanism.
 *
 * @author Marvin S. Addison
 * @since 1.3.1
 */
public class DigestMd5DirContextAuthenticationStrategy implements DirContextAuthenticationStrategy {

	/** Authentication type for DIGEST-MD5 auth */
	private static final String DIGEST_MD5_AUTHENTICATION = "DIGEST-MD5";

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.support.DirContextAuthenticationStrategy#
	 * processContextAfterCreation(javax.naming.directory.DirContext, java.lang.String,
	 * java.lang.String)
	 */
	public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password) {
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.support.DirContextAuthenticationStrategy#
	 * setupEnvironment(java.util.Hashtable, java.lang.String, java.lang.String)
	 */
	public void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) {
		env.put(Context.SECURITY_AUTHENTICATION, DIGEST_MD5_AUTHENTICATION);
		// userDn should be a bare username for DIGEST-MD5
		env.put(Context.SECURITY_PRINCIPAL, userDn);
		env.put(Context.SECURITY_CREDENTIALS, password);
	}

}
