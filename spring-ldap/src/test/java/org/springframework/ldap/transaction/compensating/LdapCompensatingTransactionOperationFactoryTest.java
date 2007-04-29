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

import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

public class LdapCompensatingTransactionOperationFactoryTest extends TestCase {
    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl renamingStrategyControl;

    private TempEntryRenamingStrategy renamingStrategyMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private LdapCompensatingTransactionOperationFactory tested;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        renamingStrategyControl = MockControl
                .createControl(TempEntryRenamingStrategy.class);
        renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl
                .getMock();

        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        tested = new LdapCompensatingTransactionOperationFactory(
                renamingStrategyMock) {

            LdapOperations createLdapOperationsInstance(DirContext ctx) {
                assertEquals(dirContextMock, ctx);
                return ldapOperationsMock;
            }
        };
    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        renamingStrategyControl = null;
        renamingStrategyMock = null;

        dirContextControl = null;
        dirContextMock = null;

        tested = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
        renamingStrategyControl.replay();
        dirContextControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        renamingStrategyControl.verify();
        dirContextControl.verify();
    }

    public void testGetRecordingOperation_Bind() throws Exception {

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "bind");
        assertTrue(result instanceof BindOperationRecorder);
        BindOperationRecorder bindOperationRecorder = (BindOperationRecorder) result;
        assertSame(ldapOperationsMock, bindOperationRecorder
                .getLdapOperations());
    }

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

    public void testGetRecordingOperation_Rename() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "rename");
        assertTrue(result instanceof RenameOperationRecorder);
        RenameOperationRecorder recordingOperation = (RenameOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    public void testGetRecordingOperation_ModifyAttributes() throws Exception {
        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation(dirContextMock, "modifyAttributes");
        assertTrue(result instanceof ModifyAttributesOperationRecorder);
        ModifyAttributesOperationRecorder recordingOperation = (ModifyAttributesOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

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
