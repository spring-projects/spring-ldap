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
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

import javax.naming.directory.DirContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LdapCompensatingTransactionOperationFactoryTest {
    private LdapOperations ldapOperationsMock;

    private TempEntryRenamingStrategy renamingStrategyMock;

    private DirContext dirContextMock;

    private LdapCompensatingTransactionOperationFactory tested;

    @Before
    public void setUp() throws Exception {
        ldapOperationsMock = mock(LdapOperations.class);
        renamingStrategyMock = mock(TempEntryRenamingStrategy.class);
        dirContextMock = mock(DirContext.class);

        tested = new LdapCompensatingTransactionOperationFactory(
                renamingStrategyMock) {

            LdapOperations createLdapOperationsInstance(DirContext ctx) {
                assertEquals(dirContextMock, ctx);
                return ldapOperationsMock;
            }
        };
    }

    @Test
    public void testGetRecordingOperation_Bind() throws Exception {

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "bind");
        assertTrue(result instanceof BindOperationRecorder);
        BindOperationRecorder bindOperationRecorder = (BindOperationRecorder) result;
        assertSame(ldapOperationsMock, bindOperationRecorder
                .getLdapOperations());
    }

    @Test
    public void testGetRecordingOperation_Rebind() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "rebind");
        assertTrue(result instanceof RebindOperationRecorder);
        RebindOperationRecorder rebindOperationRecorder = (RebindOperationRecorder) result;
        assertSame(ldapOperationsMock, rebindOperationRecorder
                .getLdapOperations());
        assertSame(renamingStrategyMock, rebindOperationRecorder
                .getRenamingStrategy());
    }

    @Test
    public void testGetRecordingOperation_Rename() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "rename");
        assertTrue(result instanceof RenameOperationRecorder);
        RenameOperationRecorder recordingOperation = (RenameOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    @Test
    public void testGetRecordingOperation_ModifyAttributes() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "modifyAttributes");
        assertTrue(result instanceof ModifyAttributesOperationRecorder);
        ModifyAttributesOperationRecorder recordingOperation = (ModifyAttributesOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    @Test
    public void testGetRecordingOperation_Unbind() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "unbind");
        assertTrue(result instanceof UnbindOperationRecorder);
        UnbindOperationRecorder recordingOperation = (UnbindOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
        assertSame(renamingStrategyMock, recordingOperation
                .getRenamingStrategy());
    }
}
