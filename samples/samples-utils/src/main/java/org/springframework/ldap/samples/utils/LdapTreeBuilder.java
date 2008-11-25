package org.springframework.ldap.samples.utils;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;

public class LdapTreeBuilder {

	private LdapTemplate ldapTemplate;

	public LdapTreeBuilder(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public LdapTree getLdapTree(DistinguishedName root) {
		DirContextOperations context = ldapTemplate.lookupContext(root
				.toString());
		return getLdapTree(context);
	}

	private LdapTree getLdapTree(final DirContextOperations rootContext) {
		final LdapTree ldapTree = new LdapTree(rootContext);
		ldapTemplate.listBindings(rootContext.getDn(),
				new AbstractContextMapper() {
					@Override
					protected Object doMapFromContext(DirContextOperations ctx) {
						DistinguishedName dn = (DistinguishedName) ctx.getDn();
						dn.prepend((DistinguishedName) rootContext.getDn());
						ldapTree.addSubTree(getLdapTree(ldapTemplate
								.lookupContext(dn)));
						return null;
					}
				});

		return ldapTree;
	}
}
