package org.springframework.ldap.core.simple;

import java.util.List;

import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;

public class SimpleLdapTemplate implements SimpleLdapOperations {

	private LdapOperations ldapOperations;

	public SimpleLdapTemplate(LdapOperations ldapOperations) {
		this.ldapOperations = ldapOperations;
	}

	public SimpleLdapTemplate(ContextSource contextSource) {
		this.ldapOperations = new LdapTemplate(contextSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#getLdapOperations()
	 */
	public LdapOperations getLdapOperations() {
		return ldapOperations;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#lookup(java.lang.String,
	 * org.springframework.ldap.core.simple.ParametrizedContextMapper)
	 */
	@SuppressWarnings("unchecked")
	public <T> T lookup(String dn, ParametrizedContextMapper<T> mapper) {
		return (T) ldapOperations.lookup(dn, mapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#search(java.lang.String,
	 * java.lang.String,
	 * org.springframework.ldap.core.simple.ParametrizedContextMapper)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> search(String base, String filter, ParametrizedContextMapper<T> mapper) {
		return ldapOperations.search(base, filter, mapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#search(java.lang.String,
	 * java.lang.String, javax.naming.directory.SearchControls,
	 * org.springframework.ldap.core.simple.ParametrizedContextMapper,
	 * org.springframework.ldap.core.DirContextProcessor)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> search(String base, String filter, SearchControls controls, ParametrizedContextMapper<T> mapper,
			DirContextProcessor processor) {
		return ldapOperations.search(base, filter, controls, mapper);
	}

}
