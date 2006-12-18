/*
 * Copyright 2005-2006 the original author or authors.
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

import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.samples.person.domain.Group;
import org.springframework.ldap.samples.person.domain.SearchCriteria;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DirContextOperations;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.ldap.support.filter.AndFilter;
import org.springframework.ldap.support.filter.EqualsFilter;
import org.springframework.ldap.support.filter.WhitespaceWildcardsFilter;

/**
 * Default implementation of GroupDao. This implementation uses
 * DirContextOperations (DirContextAdapter really, but for mock testing purposes
 * we use the interface) for managing attribute values. It has been specified in
 * the Spring Context that the DirObjectFactory should be used when creating
 * objects from contexts, which defaults to creating DirContextAdapter objects.
 * This means that we can use a ContextMapper to map from the found contexts to
 * our domain objects.
 * 
 * @author Ulrik Sandberg
 */
public class GroupDaoImpl implements GroupDao {

    /**
     * The template object that performs all data access work.
     */
    LdapOperations ldapOperations;

    DistinguishedName buildDn(Group group) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("ou", "groups");
        dn.add("cn", group.getName());
        return dn;
    }

    DirContextOperations setAttributes(DirContextOperations adapter, Group group) {
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "groupOfUniqueNames" });
        adapter.setAttributeValue("cn", group.getName());
        if (group.getMembers() != null && group.getMembers().size() > 0) {
            adapter.setAttributeValues("uniqueMember", group.getMembers()
                    .toArray(new String[0]));
        }
        return adapter;
    }

    ContextMapper getContextMapper() {
        return new GroupContextMapper();
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#create(org.springframework.ldap.samples.person.domain.Group)
     */
    public void create(Group group) {
        ldapOperations.bind(buildDn(group), setAttributes(
                new DirContextAdapter(), group), null);
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#delete(org.springframework.ldap.samples.person.domain.Group)
     */
    public void delete(Group group) {
        ldapOperations.unbind(buildDn(group));
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#find(org.springframework.ldap.samples.person.domain.SearchCriteria)
     */
    public List find(SearchCriteria criteria) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "groupOfUniqueNames"));
        filter.and(new WhitespaceWildcardsFilter("cn", criteria.getName()));
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#findAll()
     */
    public List findAll() {
        EqualsFilter filter = new EqualsFilter("objectclass",
                "groupOfUniqueNames");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#findByPrimaryKey(java.lang.String)
     */
    public Group findByPrimaryKey(String name) {
        DistinguishedName dn = new DistinguishedName("ou=groups");
        dn.add("cn", name);
        return (Group) ldapOperations.lookup(dn, getContextMapper());
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.GroupDao#update(org.springframework.ldap.samples.person.domain.Group)
     */
    public void update(Group group) {
        DistinguishedName dn = buildDn(group);
        DirContextOperations adapter = (DirContextOperations) ldapOperations
                .lookup(dn);
        adapter = setAttributes(adapter, group);
        ldapOperations.modifyAttributes(dn, adapter.getModificationItems());
    }

    public void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }
}
