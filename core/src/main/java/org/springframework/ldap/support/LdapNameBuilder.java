/*
 * Copyright 2005-2021 the original author or authors.
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

package org.springframework.ldap.support;

import org.springframework.util.Assert;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Helper class for building {@link javax.naming.ldap.LdapName} instances.
 *
 * Note that the first part of a Distinguished Name is the least significant, which means that when adding components,
 * they will be added to the <b>beginning</b> of the resulting string, e.g.
 * <pre>
 *	 LdapNameBuilder.newInstance("dc=261consulting,dc=com").add("ou=people").build().toString();
 * </pre>
 * will result in <code>ou=people,dc=261consulting,dc=com</code>.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public final class LdapNameBuilder {

	private final LdapName ldapName;

	private LdapNameBuilder(LdapName ldapName) {
		this.ldapName = ldapName;
	}

	/**
	 * Construct a new instance, starting with a blank LdapName.
	 *
	 * @return a new instance.
	 */
	public static LdapNameBuilder newInstance() {
		return new LdapNameBuilder(LdapUtils.emptyLdapName());
	}

	/**
	 * Construct a new instance, starting with a copy of the supplied LdapName.
	 * @param name the starting point of the LdapName to be built.
	 *
	 * @return a new instance.
	 */
	public static LdapNameBuilder newInstance(Name name) {
		return new LdapNameBuilder(LdapUtils.newLdapName(name));
	}

	/**
	 * Construct a new instance, starting with an LdapName constructed from the supplied string.
	 * @param name the starting point of the LdapName to be built.
	 *
	 * @return a new instance.
	 */
	public static LdapNameBuilder newInstance(String name) {
		return new LdapNameBuilder(LdapUtils.newLdapName(name));
	}

	/**
	 * Add a Rdn to the built LdapName.
	 * @param key the rdn attribute key.
	 * @param value the rdn value.
	 *
	 * @return this builder.
	 */
	public LdapNameBuilder add(String key, Object value) {
		Assert.hasText(key, "key must not be blank");
		Assert.notNull(value, "value must not be null");

		try {
			ldapName.add(new Rdn(key, value));
			return this;
		} catch (InvalidNameException e) {
			throw new org.springframework.ldap.InvalidNameException(e);
		}
	}

	/**
	 * Append the specified name to the currently built LdapName.
	 *
	 * @param name the name to add.
	 * @return this builder.
	 */
	public LdapNameBuilder add(Name name) {
		Assert.notNull(name, "name must not be null");

		try {
			ldapName.addAll(ldapName.size(), name);
			return this;
		} catch (InvalidNameException e) {
			throw new org.springframework.ldap.InvalidNameException(e);
		}
	}

	/**
	 * Append the LdapName represented by the specified string to the currently built LdapName.
	 *
	 * @param name the name to add.
	 * @return this builder.
	 */
	public LdapNameBuilder add(String name) {
		Assert.notNull(name, "name must not be null");

		return add(LdapUtils.newLdapName(name));
	}

	/**
	 * Build the LdapName instance.
	 *
	 * @return the LdapName instance that has been built.
	 */
	public LdapName build() {
		return LdapUtils.newLdapName(ldapName);
	}
}
