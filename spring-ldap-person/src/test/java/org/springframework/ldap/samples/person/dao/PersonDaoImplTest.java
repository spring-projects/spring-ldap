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

import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Unit tests for the PersonDaoImpl class.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PersonDaoImplTest extends TestCase {

    private static final String DEFAULT_DN = "cn=some person, ou=Some Company, c=SE";

    private static final DistinguishedName DEFAULT_DNAME = new DistinguishedName(
            DEFAULT_DN);

    private static final String MODIFIED_DN = "cn=some person, ou=Some Other Company, c=SE";

    private static final DistinguishedName MODIFIED_DNAME = new DistinguishedName(
            MODIFIED_DN);

    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl dirContextOperationsControl;

    private DirContextOperations dirContextOperationsMock;

    private MockControl contextMapperControl;

    private ContextMapper contextMapperMock;

    private PersonDaoImpl tested;

    private Person person;

    protected void setUp() throws Exception {
        super.setUp();
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        dirContextOperationsControl = MockControl
                .createControl(DirContextOperations.class);
        dirContextOperationsMock = (DirContextOperations) dirContextOperationsControl
                .getMock();

        contextMapperControl = MockControl.createControl(ContextMapper.class);
        contextMapperMock = (ContextMapper) contextMapperControl.getMock();

        person = new Person();
        person.setFullName("some person");
        person.setCompany("Some company");
        person.setCountry("SE");
        person.setPrimaryKey(DEFAULT_DN);

        tested = new PersonDaoImpl() {
            DirContextOperations setAttributes(DirContextOperations adapter,
                    Person p) {
                assertSame(person, p);
                return dirContextOperationsMock;
            }

            ContextMapper getContextMapper() {
                return contextMapperMock;
            }
        };
        tested.setLdapOperations(ldapOperationsMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        ldapOperationsControl = null;
        ldapOperationsMock = null;

        dirContextOperationsControl = null;
        dirContextOperationsMock = null;

        contextMapperControl = null;
        contextMapperMock = null;

        person = null;
        tested = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
        dirContextOperationsControl.replay();
        contextMapperControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        dirContextOperationsControl.verify();
        contextMapperControl.verify();
    }

    public void testBuildDn() {
        tested = new PersonDaoImpl();
        Person person = new Person();
        person.setCountry("Sweden");
        person.setCompany("Some company");
        person.setFullName("Some Person");

        DistinguishedName dn = tested.buildDn(person);

        assertEquals("cn=Some Person, ou=Some company, c=Sweden", dn.toString());
    }

    public void testCreate() {
        ldapOperationsMock.bind(DEFAULT_DNAME, dirContextOperationsMock, null);

        replay();

        tested.create(person);

        verify();
    }

    public void testUpdate() {
        ldapOperationsControl.expectAndReturn(ldapOperationsMock
                .lookup(DEFAULT_DNAME), dirContextOperationsMock);
        ModificationItem[] modificationItems = new ModificationItem[0];
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getModificationItems(), modificationItems);
        ldapOperationsMock.modifyAttributes(DEFAULT_DNAME, modificationItems);

        replay();

        tested.update(person);

        verify();
    }

    public void testUpdateWithChangedCompany() throws NamingException {
        person.setCompany("Some Other Company");

        ldapOperationsMock.rename(DEFAULT_DNAME, MODIFIED_DNAME);

        ldapOperationsControl.expectAndReturn(ldapOperationsMock
                .lookup(MODIFIED_DNAME), dirContextOperationsMock);

        ModificationItem[] modificationItems = new ModificationItem[0];
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getModificationItems(), modificationItems);
        ldapOperationsMock.modifyAttributes(MODIFIED_DNAME, modificationItems);

        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getNameInNamespace(), "cn=new dn, dc=jayway, dc=se");
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getDn(), new DistinguishedName("cn=new dn"));

        replay();

        tested.update(person);

        verify();
        assertEquals("cn=new dn", person.getPrimaryKey());
        assertEquals("cn=new dn, dc=jayway, dc=se", person.getDn());
    }

    public void testDelete() {
        ldapOperationsMock.unbind(DEFAULT_DN);

        replay();

        tested.delete(person);

        verify();
    }

    public void testFindAll() {
        List expectedList = Collections.singletonList(null);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.search(
                DistinguishedName.EMPTY_PATH, "(objectclass=person)",
                contextMapperMock), expectedList);

        replay();

        List result = tested.findAll();

        verify();

        assertSame(expectedList, result);
    }

    public void testFindByPrimaryKey() {
        String dn = "cn=Some Person, ou=Some company, c=Sweden";

        ldapOperationsControl.expectAndReturn(ldapOperationsMock.lookup(dn,
                contextMapperMock), person);

        replay();

        Person result = tested.findByPrimaryKey(dn);

        verify();

        assertSame(person, result);

    }

    public void testFind_Name() {
        List expectedList = Collections.singletonList(null);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.search(
                DistinguishedName.EMPTY_PATH,
                "(&(objectclass=person)(cn=*some*))", contextMapperMock),
                expectedList);

        replay();

        SearchCriteria criteria = new SearchCriteria();
        criteria.setName("some");
        List result = tested.find(criteria);

        verify();

        assertSame(expectedList, result);
    }
}
