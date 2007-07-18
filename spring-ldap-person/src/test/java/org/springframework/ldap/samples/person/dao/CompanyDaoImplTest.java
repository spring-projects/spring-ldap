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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

/**
 * Unit tests for the CompanyDaoImpl class.
 * 
 * @author Ulrik Sandberg
 */
public class CompanyDaoImplTest extends TestCase {

    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl contextMapperControl;

    private ContextMapper contextMapperMock;

    private CompanyDaoImpl tested;

    protected void setUp() throws Exception {
        super.setUp();
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        contextMapperControl = MockControl.createControl(ContextMapper.class);
        contextMapperMock = (ContextMapper) contextMapperControl.getMock();

        tested = new CompanyDaoImpl() {
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

        contextMapperControl = null;
        contextMapperMock = null;

        tested = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
        contextMapperControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        contextMapperControl.verify();
    }

    public void testFindByCountry() {
        List expectedList = Collections.singletonList(null);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.search(
                new DistinguishedName("c=Sweden"), "(objectclass=organizationalUnit)",
                contextMapperMock), expectedList);

        replay();

        List result = tested.findByCountry("Sweden");

        verify();

        assertSame(expectedList, result);
    }
}
