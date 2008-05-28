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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.compensating.RenameOperationExecutor;
import org.springframework.ldap.transaction.compensating.RenameOperationRecorder;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

public class RenameOperationRecorderTest extends TestCase {

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

    public void testRecordOperation() {
        RenameOperationRecorder tested = new RenameOperationRecorder(
                ldapOperationsMock);

        replay();
        // Perform test
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { "ou=someou", "ou=newou" });
        verify();

        assertTrue(operation instanceof RenameOperationExecutor);
        RenameOperationExecutor rollbackOperation = (RenameOperationExecutor) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertEquals("ou=newou", rollbackOperation.getNewDn().toString());
        assertEquals("ou=someou", rollbackOperation.getOriginalDn().toString());
    }

}
