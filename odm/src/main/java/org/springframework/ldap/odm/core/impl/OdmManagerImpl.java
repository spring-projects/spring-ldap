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

package org.springframework.ldap.odm.core.impl;

import java.util.List;
import java.util.Set;

import javax.naming.Name;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.odm.typeconversion.ConverterManager;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.util.StringUtils;

/**
 * An implementation of {@link org.springframework.ldap.odm.core.OdmManager} which uses
 * {@link org.springframework.ldap.odm.typeconversion.ConverterManager} to convert between
 * Java and LDAP representations of attribute values.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @author Mattias Hellborg Arthursson
 * @deprecated This functionality is automatically available in LdapTemplate as of version
 * 2.0
 */
@Deprecated
public final class OdmManagerImpl implements OdmManager {

	// The link to the LDAP directory
	private final LdapTemplate ldapTemplate;

	private DefaultObjectDirectoryMapper objectDirectoryMapper;

	public OdmManagerImpl(ConverterManager converterManager, LdapOperations ldapOperations,
			Set<Class<?>> managedClasses) {
		this.ldapTemplate = (LdapTemplate) ldapOperations;
		this.objectDirectoryMapper = new DefaultObjectDirectoryMapper();

		if (converterManager != null) {
			this.objectDirectoryMapper.setConverterManager(converterManager);
		}

		if (managedClasses != null) {
			for (Class<?> managedClass : managedClasses) {
				addManagedClass(managedClass);
			}
		}

		this.ldapTemplate.setObjectDirectoryMapper(this.objectDirectoryMapper);
	}

	public OdmManagerImpl(ConverterManager converterManager, ContextSource contextSource,
			Set<Class<?>> managedClasses) {
		this(converterManager, new LdapTemplate(contextSource), managedClasses);
	}

	public OdmManagerImpl(ConverterManager converterManager, ContextSource contextSource) {
		this(converterManager, contextSource, null);
	}

	/**
	 * Adds an {@link org.springframework.ldap.odm.annotations} annotated class to the set
	 * managed by this OdmManager.
	 * @param managedClass The class to add to the managed set.
	 */
	public void addManagedClass(Class<?> managedClass) {
		this.objectDirectoryMapper.manageClass(managedClass);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.core.OdmManager#create(java.lang.Object)
	 */
	public <T> T read(Class<T> clazz, Name dn) {
		return this.ldapTemplate.findByDn(dn, clazz);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.core.OdmManager#create(java.lang.Object)
	 */
	public void create(Object entry) {
		this.ldapTemplate.create(entry);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.core.OdmManager#update(java.lang.Object, boolean)
	 */
	public void update(Object entry) {
		this.ldapTemplate.update(entry);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.core.OdmManager#delete(javax.naming.Name)
	 */
	public void delete(Object entry) {
		this.ldapTemplate.delete(entry);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.core.OdmManager#search(java.lang.Class,
	 * javax.naming.Name, java.lang.String, javax.naming.directory.SearchControls)
	 */
	public <T> List<T> search(Class<T> managedClass, Name base, String filter, SearchControls scope) {
		Filter searchFilter = null;
		if (StringUtils.hasText(filter)) {
			searchFilter = new HardcodedFilter(filter);
		}

		return this.ldapTemplate.find(base, searchFilter, scope, managedClass);
	}

	@Override
	public <T> List<T> search(Class<T> clazz, LdapQuery query) {
		return this.ldapTemplate.find(query, clazz);
	}

	public <T> List<T> findAll(Class<T> managedClass, Name base, SearchControls scope) {
		return this.ldapTemplate.findAll(base, scope, managedClass);
	}

}
