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

import java.util.Set;

import org.jspecify.annotations.NullUnmarked;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.typeconversion.ConverterManager;

/**
 * A Spring Factory bean which creates {@link OdmManagerImpl} instances.
 * <p>
 * Typical configuration would appear as follows: <pre>
 *   &lt;bean id="odmManager" class="org.springframework.ldap.odm.core.impl.OdmManagerImplFactoryBean"&gt;
 *	   &lt;property name="converterManager" ref="converterManager" /&gt;
 *	   &lt;property name="contextSource" ref="contextSource" /&gt;
 *	   &lt;property name="managedClasses"&gt;
 *		   &lt;set&gt;
 *			   &lt;value&gt;org.myorg.myldapentries.Person&lt;/value&gt;
 *			   &lt;value&gt;org.myorg.myldapentries.OrganizationalUnit&lt;/value&gt;
 *		   &lt;/set&gt;
 *	   &lt;/property&gt;
 *   &lt;/bean&gt;
 * </pre>
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @deprecated This functionality is automatically available in LdapTemplate as of version
 * 2.0
 */
@Deprecated
@NullUnmarked
public final class OdmManagerImplFactoryBean implements FactoryBean {

	private LdapOperations ldapOperations = null;

	private Set<Class<?>> managedClasses = null;

	private ConverterManager converterManager = null;

	/**
	 * Set the LdapOperations instance to use to interact with the LDAP directory.
	 * @param ldapOperations the LdapOperations instance to use.
	 */
	public void setLdapOperations(LdapOperations ldapOperations) {
		this.ldapOperations = ldapOperations;
	}

	/**
	 * Set the ContextSource to use to interact with the LDAP directory.
	 * @param contextSource The ContextSource to use.
	 */
	public void setContextSource(ContextSource contextSource) {
		this.ldapOperations = new LdapTemplate(contextSource);
	}

	/**
	 * Set the list of {@link org.springframework.ldap.odm.annotations} annotated classes
	 * the OdmManager will process.
	 * @param managedClasses The list of classes to manage.
	 */
	public void setManagedClasses(Set<Class<?>> managedClasses) {
		this.managedClasses = managedClasses;
	}

	/**
	 * Set the ConverterManager to use to convert between LDAP and Java representations of
	 * attributes.
	 * @param converterManager The ConverterManager to use.
	 */
	public void setConverterManager(ConverterManager converterManager) {
		this.converterManager = converterManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		if (this.ldapOperations == null) {
			throw new FactoryBeanNotInitializedException("contextSource ldapOperations property has not been set");
		}
		if (this.managedClasses == null) {
			throw new FactoryBeanNotInitializedException("managedClasses property has not been set");
		}
		if (this.converterManager == null) {
			throw new FactoryBeanNotInitializedException("converterManager property has not been set");
		}

		return new OdmManagerImpl(this.converterManager, this.ldapOperations, this.managedClasses);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		return OdmManagerImpl.class;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

}
