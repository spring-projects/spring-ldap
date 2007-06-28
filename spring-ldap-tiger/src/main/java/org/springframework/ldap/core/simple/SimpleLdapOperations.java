package org.springframework.ldap.core.simple;

import java.util.List;

import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;

public interface SimpleLdapOperations {

	LdapOperations getLdapOperations();

	<T> List<T> search(String base, String filter, ParametrizedContextMapper<T> mapper);

	<T> List<T> search(String base, String filter, SearchControls controls, ParametrizedContextMapper<T> mapper,
			DirContextProcessor processor);

	<T> T lookup(String dn, ParametrizedContextMapper<T> mapper);
}
