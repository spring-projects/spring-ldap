package org.springframework.ldap.core.support;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.support.LdapUtils;

/**
 * Attempts to perform an LDAP operation in the authenticated context, because
 * Active Directory might allow bind with incorrect password (specifically empty
 * password), and later refuse operations. We want to fail fast when
 * authenticating.
 * 
 * @author Hugo Josefson
 * @since 1.3.1
 */
public class LookupAttemptingCallback implements AuthenticatedLdapEntryContextCallback {
	public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
		try {
			ctx.lookup(ldapEntryIdentification.getRelativeDn());
		}
		catch (NamingException e) {
			// rethrow, because we aren't allowed to throw checked exceptions.
			throw LdapUtils.convertLdapException(e);
		}
	}
}
