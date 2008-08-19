package org.springframework.ldap.core.support;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * {@link DirContextAuthenticationStrategy} for using TLS and external (SASL)
 * authentication. This implementation requires a client certificate to be
 * pointed out using system variables, as described <a
 * href="http://java.sun.com/products/jndi/tutorial/ldap/ext/starttls.html"
 * >here</a>. Refer to {@link AbstractTlsDirContextAuthenticationStrategy} for
 * other configuration options.
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
