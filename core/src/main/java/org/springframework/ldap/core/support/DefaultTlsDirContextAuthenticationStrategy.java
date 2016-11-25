/*
 * Copyright 2005-2010 the original author or authors.
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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * Default implementation of TLS authentication. Applies <code>SIMPLE</code>
 * authentication on top of the negotiated TLS session. Refer to
 * {@link AbstractTlsDirContextAuthenticationStrategy} for configuration
 * options.
 * 
 * @author Mattias Hellborg Arthursson
 * @see AbstractTlsDirContextAuthenticationStrategy
 * @see AbstractContextSource
 */
public class DefaultTlsDirContextAuthenticationStrategy extends AbstractTlsDirContextAuthenticationStrategy {
	private static final String SIMPLE_AUTHENTICATION = "simple";

	protected void applyAuthentication(LdapContext ctx, String userDn, String password) throws NamingException {
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION);
		ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDn);
		ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
		// Force reconnect with user credentials
		ctx.reconnect(null);
	}

}
