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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * {@link DirContextAuthenticationStrategy} for using TLS and external (SASL)
 * authentication. This implementation requires a client certificate to be pointed out
 * using system variables, as described
 * <a href="https://java.sun.com/products/jndi/tutorial/ldap/ext/starttls.html" >here</a>.
 * Refer to {@link AbstractTlsDirContextAuthenticationStrategy} for other configuration
 * options.
 *
 * @author Mattias Hellborg Arthursson
 * @see AbstractTlsDirContextAuthenticationStrategy
 * @see AbstractContextSource
 */
public class ExternalTlsDirContextAuthenticationStrategy extends AbstractTlsDirContextAuthenticationStrategy {

	private static final String EXTERNAL_AUTHENTICATION = "EXTERNAL";

	protected void applyAuthentication(LdapContext ctx, String userDn, String password) throws NamingException {
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, EXTERNAL_AUTHENTICATION);
	}

}
