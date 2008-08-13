package org.springframework.ldap.core.support;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * {@link DirContextAuthenticationStrategy} for using TLS and external (SASL)
 * authentication.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class ExternalTlsDirContextAuthenticationStrategy extends AbstractTlsDirContextAuthenticationStrategy {

	private static final String EXTERNAL_AUTHENTICATION = "EXTERNAL";

	protected void applyAuthentication(LdapContext ctx, String userDn, String password) throws NamingException {
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, EXTERNAL_AUTHENTICATION);
	}

}
