package org.springframework.ldap.core.support;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

public class DefaultTlsDirContextAuthenticationStrategy extends AbstractTlsDirContextAuthenticationStrategy {
	private static final String SIMPLE_AUTHENTICATION = "simple";

	protected void applyAuthentication(LdapContext ctx, String userDn, String password) throws NamingException {
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION);
		ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDn);
		ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
	}

}
