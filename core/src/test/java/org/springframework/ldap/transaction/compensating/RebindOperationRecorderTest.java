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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RebindOperationRecorderTest {
    private LdapOperations ldapOperationsMock;

    private TempEntryRenamingStrategy renamingStrategyMock;

    @Before
    public void setUp() throws Exception {
        ldapOperationsMock = mock(LdapOperations.class);
        renamingStrategyMock = mock(TempEntryRenamingStrategy.class);

    }

    @Test
    public void testRecordOperation() {
        final LdapName expectedDn = LdapUtils.newLdapName(
                "cn=john doe");
        final LdapName expectedTempDn = LdapUtils.newLdapName(
                "cn=john doe");
        RebindOperationRecorder tested = new RebindOperationRecorder(
                ldapOperationsMock, renamingStrategyMock);

        when(renamingStrategyMock.getTemporaryName(expectedDn))
                .thenReturn(expectedTempDn);

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();

        // perform test
        CompensatingTransactionOperationExecutor result = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });
        assertTrue(result instanceof RebindOperationExecutor);
        RebindOperationExecutor rollbackOperation = (RebindOperationExecutor) result;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempDn, rollbackOperation.getTemporaryDn());
        assertSame(expectedObject, rollbackOperation.getOriginalObject());
        assertSame(expectedAttributes, rollbackOperation
                .getOriginalAttributes());
    }
}
