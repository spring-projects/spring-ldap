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
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RenameOperationRecorderTest {

    private LdapOperations ldapOperationsMock;

    @Before
    public void setUp() throws Exception {
        ldapOperationsMock = mock(LdapOperations.class);;
    }

    @Test
    public void testRecordOperation() {
        RenameOperationRecorder tested = new RenameOperationRecorder(
                ldapOperationsMock);

        // Perform test
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { "ou=someou", "ou=newou" });

        assertTrue(operation instanceof RenameOperationExecutor);
        RenameOperationExecutor rollbackOperation = (RenameOperationExecutor) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertEquals("ou=newou", rollbackOperation.getNewDn().toString());
        assertEquals("ou=someou", rollbackOperation.getOriginalDn().toString());
    }
}
