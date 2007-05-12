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

import javax.naming.directory.ModificationItem;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.samples.person.domain.Group;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Unit tests for the GroupDaoImpl class.
 * 
 * @author Ulrik Sandberg
 */
public class GroupDaoImplTest extends TestCase {

    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl dirContextOperationsControl;

    private DirContextOperations dirContextOperationsMock;

    private MockControl contextMapperControl;

    private ContextMapper contextMapperMock;

    private GroupDaoImpl tested;

    private Group group;

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

        group = new Group();

        tested = new GroupDaoImpl() {
            DirContextOperations setAttributes(DirContextOperations adapter,
                    Group p) {
                assertSame(group, p);
                return dirContextOperationsMock;
            }

            DistinguishedName buildDn(Group p) {
                assertSame(group, p);
                return DistinguishedName.EMPTY_PATH;
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

        group = null;
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
        tested = new GroupDaoImpl();
        Group group = new Group();
        group.setName("Some Group");

        DistinguishedName dn = tested.buildDn(group);

        assertEquals("cn=Some Group, ou=groups", dn.toString());
    }

    public void testCreate() {
        ldapOperationsMock.bind(DistinguishedName.EMPTY_PATH,
                dirContextOperationsMock, null);

        replay();

        tested.create(group);

        verify();
    }

    public void testUpdate() {
        ldapOperationsControl
                .expectAndReturn(ldapOperationsMock
                        .lookup(DistinguishedName.EMPTY_PATH),
                        dirContextOperationsMock);
        ModificationItem[] modificationItems = new ModificationItem[0];
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getModificationItems(), modificationItems);
        ldapOperationsMock.modifyAttributes(DistinguishedName.EMPTY_PATH,
                modificationItems);

        replay();

        tested.update(group);

        verify();

    }

    public void testDelete() {
        ldapOperationsMock.unbind(DistinguishedName.EMPTY_PATH);

        replay();

        tested.delete(group);

        verify();
    }

    public void testFindAll() {
        List expectedList = Collections.singletonList(null);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.search(
                DistinguishedName.EMPTY_PATH,
                "(objectclass=groupOfUniqueNames)", contextMapperMock),
                expectedList);

        replay();

        List result = tested.findAll();

        verify();

        assertSame(expectedList, result);
    }

    public void testFindByPrimaryKey() {
        DistinguishedName dn = new DistinguishedName("cn=Some Group, ou=groups");

        ldapOperationsControl.expectAndReturn(ldapOperationsMock.lookup(dn,
                contextMapperMock), group);

        replay();

        Group result = tested.findByPrimaryKey("Some Group");

        verify();

        assertSame(group, result);
    }

    public void testFind_Name() {
        List expectedList = Collections.singletonList(null);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.search(
                DistinguishedName.EMPTY_PATH,
                "(&(objectclass=groupOfUniqueNames)(cn=*some*))",
                contextMapperMock), expectedList);

        replay();

        SearchCriteria criteria = new SearchCriteria();
        criteria.setName("some");
        List result = tested.find(criteria);

        verify();

        assertSame(expectedList, result);
    }
}
