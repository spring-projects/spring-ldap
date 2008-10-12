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
import org.springframework.ldap.transaction.compensating.BindOperationExecutor;
import org.springframework.ldap.transaction.compensating.BindOperationRecorder;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

public class BindOperationRecorderTest extends TestCase {
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

    public void testRecordOperation_DistinguishedName() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        DistinguishedName expectedDn = new DistinguishedName("cn=John Doe");

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        // Perform test.
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });

        assertTrue(operation instanceof BindOperationExecutor);
        BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
        assertSame(expectedDn, rollbackOperation.getDn());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedObject, rollbackOperation.getOriginalObject());
        assertSame(expectedAttributes, rollbackOperation
                .getOriginalAttributes());
    }

    public void testPerformOperation_String() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        String expectedDn = "cn=John Doe";

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        // Perform test.
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });

        assertTrue(operation instanceof BindOperationExecutor);
        BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
        assertEquals(expectedDn, rollbackOperation.getDn().toString());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
    }

    public void testPerformOperation_Invalid() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        Object expectedDn = new Object();

        try {
            // Perform test.
            tested.recordOperation(new Object[] { expectedDn });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

    }
}
