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
package org.springframework.ldap.transaction.compensating;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class BindOperationRecorderTest {
    private LdapOperations ldapOperationsMock;

    @Before
    public void setUp() throws Exception {
        ldapOperationsMock = mock(LdapOperations.class);

    }

    @Test
    public void testRecordOperation_Name() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        LdapName expectedDn = LdapUtils.newLdapName("cn=John Doe");

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

    @Test
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

    @Test(expected = IllegalArgumentException.class)
    public void testPerformOperation_Invalid() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        Object expectedDn = new Object();

        // Perform test.
        tested.recordOperation(new Object[]{expectedDn});
    }
}
