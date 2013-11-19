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
package org.springframework.ldap.core.simple;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;

/**
 * Java-5-based convenience wrapper for the classic LdapTemplate, adding some
 * convenient shortcuts and taking advantage of Java 5 Generics.
 * 
 * Use the {@link #getLdapOperations()} method if you need to invoke less
 * commonly used template methods.
 * 
 * @author Mattias Hellborg Arthursson
 * @deprecated Core classes are parameterized as of 2.0.
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

    /**
     * {@inheritDoc}
     */
    @Override
	public LdapOperations getLdapOperations() {
		return ldapOperations;
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> T lookup(String dn, ParameterizedContextMapper<T> mapper) {
		return (T) ldapOperations.lookup(dn, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> List<T> search(String base, String filter, ParameterizedContextMapper<T> mapper) {
		return ldapOperations.search(base, filter, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> List<T> search(String base, String filter, SearchControls controls,
			ParameterizedContextMapper<T> mapper, DirContextProcessor processor) {
		return ldapOperations.search(base, filter, controls, mapper, processor);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public DirContextOperations lookupContext(String dn) {
		return ldapOperations.lookupContext(dn);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void modifyAttributes(DirContextOperations ctx) {
		ldapOperations.modifyAttributes(ctx);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void bind(String dn, Object obj, Attributes attributes) {
		ldapOperations.bind(dn, obj, attributes);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void unbind(String dn) {
		ldapOperations.unbind(dn);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void bind(Name dn, Object obj, Attributes attributes) {
		ldapOperations.bind(dn, obj, attributes);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> T lookup(Name dn, ParameterizedContextMapper<T> mapper) {
		return (T) ldapOperations.lookup(dn, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public DirContextOperations lookupContext(Name dn) {
		return ldapOperations.lookupContext(dn);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> List<T> search(Name base, String filter, ParameterizedContextMapper<T> mapper) {
		return ldapOperations.search(base, filter, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> List<T> search(Name base, String filter, SearchControls controls, ParameterizedContextMapper<T> mapper,
			DirContextProcessor processor) {
		return ldapOperations.search(base, filter, controls, mapper, processor);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void unbind(Name dn) {
		ldapOperations.unbind(dn);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void bind(DirContextOperations ctx) {
		ldapOperations.bind(ctx);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> T searchForObject(String base, String filter, ParameterizedContextMapper<T> mapper) {
		return (T) ldapOperations.searchForObject(base, filter, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	@SuppressWarnings("unchecked")
	public <T> T searchForObject(Name base, String filter, ParameterizedContextMapper<T> mapper) {
		return (T) ldapOperations.searchForObject(base, filter, mapper);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean authenticate(String base, String filter, String password) {
		return ldapOperations.authenticate(base, filter, password);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean authenticate(Name base, String filter, String password) {
		return ldapOperations.authenticate(base, filter, password);
	}
}
