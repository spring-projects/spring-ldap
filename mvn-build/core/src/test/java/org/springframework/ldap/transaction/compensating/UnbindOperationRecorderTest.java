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
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.UnbindOperationExecutor;
import org.springframework.ldap.transaction.compensating.UnbindOperationRecorder;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

public class UnbindOperationRecorderTest extends TestCase {
    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl renamingStrategyControl;

    private TempEntryRenamingStrategy renamingStrategyMock;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        renamingStrategyControl = MockControl
                .createControl(TempEntryRenamingStrategy.class);
        renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl
                .getMock();

    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        renamingStrategyControl = null;
        renamingStrategyMock = null;

    }

    protected void replay() {
        ldapOperationsControl.replay();
        renamingStrategyControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        renamingStrategyControl.verify();
    }

    public void testRecordOperation() {
        final DistinguishedName expectedTempName = new DistinguishedName(
                "cn=john doe_temp");
        final DistinguishedName expectedDn = new DistinguishedName(
                "cn=john doe");
        UnbindOperationRecorder tested = new UnbindOperationRecorder(
                ldapOperationsMock, renamingStrategyMock);

        renamingStrategyControl.expectAndReturn(renamingStrategyMock
                .getTemporaryName(expectedDn), expectedTempName);

        replay();
        // Perform test
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn });
        verify();

        // Verify result
        assertTrue(operation instanceof UnbindOperationExecutor);
        UnbindOperationExecutor rollbackOperation = (UnbindOperationExecutor) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempName, rollbackOperation.getTemporaryDn());
    }

}
