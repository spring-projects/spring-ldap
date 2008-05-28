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
package org.springframework.ldap.transaction.compensating;

import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.compensating.RebindOperationExecutor;

public class RebindOperationExecutorTest extends TestCase {

    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();
    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
    }

    public void testPerformOperation() {
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindOperationExecutor tested = new RebindOperationExecutor(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.rename(expectedOriginalDn, expectedTempDn);
        ldapOperationsMock.bind(expectedOriginalDn, expectedObject,
                expectedAttributes);

        replay();
        // perform test
        tested.performOperation();
        verify();
    }

    public void testCommit() {
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindOperationExecutor tested = new RebindOperationExecutor(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.unbind(expectedTempDn);

        replay();
        // perform test
        tested.commit();
        verify();
    }

    public void testRollback() {
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindOperationExecutor tested = new RebindOperationExecutor(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.unbind(expectedOriginalDn);
        ldapOperationsMock.rename(expectedTempDn, expectedOriginalDn);

        replay();
        // perform test
        tested.rollback();
        verify();
    }
}
