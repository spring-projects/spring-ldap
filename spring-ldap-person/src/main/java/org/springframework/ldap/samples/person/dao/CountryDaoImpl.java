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
package org.springframework.ldap.samples.person.dao;

import java.util.List;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * Default implementation of CountryDao. This implementation uses
 * DirContextOperations (DirContextAdapter really, but for mock testing purposes
 * we use the interface) for managing attribute values. It has been specified in
 * the Spring Context that the DirObjectFactory should be used when creating
 * objects from contexts, which defaults to creating DirContextAdapter objects.
 * This means that we can use a ContextMapper to map from the found contexts to
 * our domain objects.
 * 
 * @author Ulrik Sandberg
 */
public class CountryDaoImpl implements CountryDao {
    /**
     * The template object that performs all data access work.
     */
    LdapOperations ldapOperations;

    /*
     * @see org.springframework.ldap.samples.person.dao.CountryDao#findAll()
     */
    public List findAll() {
        EqualsFilter filter = new EqualsFilter("objectclass", "country");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    ContextMapper getContextMapper() {
        return new CountryContextMapper();
    }

    public void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }
}
