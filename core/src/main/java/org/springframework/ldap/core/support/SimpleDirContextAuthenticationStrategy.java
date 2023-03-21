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
package org.springframework.ldap.core.support;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * The default {@link DirContextAuthenticationStrategy} implementation, setting the
 * <code>DirContext</code> environment up for 'SIMPLE' authentication, and specifying the
 * user DN and password as SECURITY_PRINCIPAL and SECURITY_CREDENTIALS respectively in the
 * authenticated environment before the context is created.
 *
 * @author Mattias Hellborg Arthursson
 */
public class SimpleDirContextAuthenticationStrategy implements DirContextAuthenticationStrategy {

	private static final String SIMPLE_AUTHENTICATION = "simple";

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.support.DirContextAuthenticationStrategy#
	 * setupEnvironment(java.util.Hashtable, java.lang.String, java.lang.String)
	 */
	public void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) {
		env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION);
		env.put(Context.SECURITY_PRINCIPAL, userDn);
		env.put(Context.SECURITY_CREDENTIALS, password);
	}

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

}
