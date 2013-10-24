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

package org.springframework.ldap.itest.odm;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateOdmGroupManipulationITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private LdapTemplate tested;

    @Test
    public void testFindOne() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        assertNotNull(group);
        assertEquals("ROLE_USER", group.getName());
        assertEquals(5, group.getMembers().size());

        Set<Name> members = group.getMembers();
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person2,ou=company1,c=Sweden,dc=jayway,dc=se")));
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Norway,dc=jayway,dc=se")));
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company2,c=Sweden,dc=jayway,dc=se")));
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person3,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }

    @Test
    public void testRemoveMember() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.removeMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se"));
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();
        assertEquals(4, members.size());
        assertFalse(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }

    @Test
    public void testRemoveMemberSyntacticallyEqual() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.removeMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden, DC=jayway,DC=se"));
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();
        assertEquals(4, members.size());
        assertFalse(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }

    @Test
    public void testAddMember() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.addMember(LdapUtils.newLdapName("cn=Some Person+sn=Person,ou=company1,c=Norway,dc=jayway,dc=se"));
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();
        assertEquals(6, members.size());
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person+sn=Person,ou=company1,c=Norway,dc=jayway,dc=se")));
    }

    @Test
    public void testAddMemberDuplicate() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.addMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se"));
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();
        assertEquals(5, members.size());
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }

    @Test
    public void testAddMemberSyntacticallyEqualDuplicate() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.addMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,DC=jayway,DC=se"));
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();
        assertEquals(5, members.size());
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }

    @Test
    public void testSetMembersSyntacticallyEqual() {
        Group group = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        group.setMembers(new HashSet<Name>(){{
            add(LdapUtils.newLdapName("CN=Some Person,OU=company1, C=Sweden, DC=jayway,DC=se"));
            add(LdapUtils.newLdapName("CN=Some Person2, OU=company1,C=Sweden,DC=jayway, DC=se"));
        }});
        tested.update(group);

        Group verification = tested.findOne(query().where("cn").is("ROLE_USER"), Group.class);

        Set<Name> members = verification.getMembers();

        assertEquals(2, members.size());
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se")));
        assertTrue(members.contains(LdapUtils.newLdapName("cn=Some Person2,ou=company1,c=Sweden,dc=jayway,dc=se")));
    }
}
