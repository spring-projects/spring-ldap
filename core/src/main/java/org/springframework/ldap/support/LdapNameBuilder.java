/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.support;

import org.springframework.util.Assert;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Helper class for building {@link javax.naming.ldap.LdapName} instances.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdapNameBuilder {

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
    public static LdapNameBuilder newLdapName(Name name) {
        return new LdapNameBuilder(LdapUtils.newLdapName(name));
    }

    /**
     * Construct a new instance, starting with an LdapName constructed from the supplied string.
     * @param name the starting point of the LdapName to be built.
     *
     * @return a new instance.
     */
    public static LdapNameBuilder newLdapName(String name) {
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
        Assert.notNull(key, "value must not be null");

        try {
            ldapName.add(new Rdn(key, value));
            return this;
        } catch (InvalidNameException e) {
            throw new org.springframework.ldap.InvalidNameException(e);
        }
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
