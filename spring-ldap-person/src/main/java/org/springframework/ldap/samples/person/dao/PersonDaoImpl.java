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
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Default implementation of PersonDao. This implementation uses
 * DirContextOperations (DirContextAdapter really, but for mock testing purposes
 * we use the interface) for managing attribute values. It has been specified in
 * the Spring Context that the DirObjectFactory should be used when creating
 * objects from contexts, which defaults to creating DirContextAdapter objects.
 * This means that we can use a ContextMapper to map from the found contexts to
 * our domain objects. This is especially useful since we in this case have
 * properties in our domain objects that depend on parts of the DN.
 * 
 * We could have worked with Attributes and an AttributesMapper implementation
 * instead, but working with Attributes is a bore and also, working with
 * AttributesMapper objects (or, indeed Attributes) does not give us access to
 * the distinguished name.
 * 
 * @author Mattias Arthursson
 */
public class PersonDaoImpl implements PersonDao {

    private LdapOperations ldapOperations;

    DistinguishedName buildDn(Person person) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("c", person.getCountry());
        dn.add("ou", person.getCompany());
        dn.add("cn", person.getFullName());
        return dn;
    }

    DirContextOperations setAttributes(DirContextOperations adapter,
            Person person) {
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person", "organizationalPerson", "inetOrgPerson" });
        adapter.setAttributeValue("cn", person.getFullName());
        adapter.setAttributeValue("sn", person.getLastName());
        adapter.setAttributeValues("description", person.getDescription());
        adapter.setAttributeValue("telephoneNumber", person.getPhone());
        return adapter;
    }

    ContextMapper getContextMapper() {
        PersonContextMapper personContextMapper = new PersonContextMapper();
        return personContextMapper;
    }

    public void create(Person person) {
        ldapOperations.bind(buildDn(person), setAttributes(
                new DirContextAdapter(), person), null);
    }

    public void update(Person person) {
        DistinguishedName originalDn = new DistinguishedName(person
                .getPrimaryKey());
        DistinguishedName newDn = buildDn(person);

        if (!originalDn.equals(newDn)) {
            ldapOperations.rename(originalDn, newDn);
        }

        DirContextOperations ctx = (DirContextOperations) ldapOperations
                .lookup(newDn);
        ldapOperations.modifyAttributes(newDn, setAttributes(ctx, person)
                .getModificationItems());

        if (!originalDn.equals(newDn)) {
            person.setDn(ctx.getNameInNamespace());
            person.setPrimaryKey(ctx.getDn().toString());
        }
    }

    public void delete(Person person) {
        ldapOperations.unbind(person.getPrimaryKey());
    }

    public List findAll() {
        EqualsFilter filter = new EqualsFilter("objectclass", "person");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    public Person findByPrimaryKeyData(String country, String company,
            String fullname) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("c", country);
        dn.add("ou", company);
        dn.add("cn", fullname);

        return (Person) ldapOperations.lookup(dn, getContextMapper());
    }

    public Person findByPrimaryKey(String dn) {
        return (Person) ldapOperations.lookup(dn, getContextMapper());
    }

    public List find(SearchCriteria criteria) {
        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "person"));
        andFilter.and(new WhitespaceWildcardsFilter("cn", criteria.getName()));
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, andFilter
                .encode(), getContextMapper());
    }

    public void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

}
