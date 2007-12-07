/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.core.simple;

import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Java-5-based convenience wrapper for the classic LdapTemplate, adding some
 * convenient shortcuts and taking advantage of Java 5 Generics.
 * 
 * Use the {@link #getLdapOperations()} method if you need to invoke less
 * commonly used template methods.
 * 
 * @author Mattias Arthursson
 */
public class SimpleLdapTemplate implements SimpleLdapOperations {

	private LdapOperations ldapOperations;

	/**
	 * Constructs a new SimpleLdapTemplate instance wrapping the supplied
	 * LdapOperations instance.
	 * 
	 * @param ldapOperations the LdapOperations instance to wrap.
	 */
	public SimpleLdapTemplate(LdapOperations ldapOperations) {
		this.ldapOperations = ldapOperations;
	}

	/**
	 * Constructs a new SimpleLdapTemplate instance, automatically creating a
	 * wrapped LdapTemplate instance to work with.
	 * 
	 * @param contextSource
	 */
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
	public <T> T lookup(String dn, ParameterizedContextMapper<T> mapper) {
		return (T) ldapOperations.lookup(dn, mapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#search(java.lang.String,
	 * java.lang.String,
	 * org.springframework.ldap.core.simple.ParametrizedContextMapper)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> search(String base, String filter, ParameterizedContextMapper<T> mapper) {
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
	public <T> List<T> search(String base, String filter, SearchControls controls, ParameterizedContextMapper<T> mapper,
			DirContextProcessor processor) {
		return ldapOperations.search(base, filter, controls, mapper, processor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#lookup(java.lang.String)
	 */
	public DirContextOperations lookupContext(String dn) {
		return ldapOperations.lookupContext(dn);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#modifyAttributes(org.springframework.ldap.core.DirContextOperations)
	 */
	public void modifyAttributes(DirContextOperations ctx) {
		ldapOperations.modifyAttributes(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#bind(java.lang.String,
	 * java.lang.Object, javax.naming.directory.Attributes)
	 */
	public void bind(String dn, Object obj, Attributes attributes) {
		ldapOperations.bind(dn, obj, attributes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.simple.SimpleLdapOperations#unbind(java.lang.String)
	 */
	public void unbind(String dn) {
		ldapOperations.unbind(dn);
	}

}
