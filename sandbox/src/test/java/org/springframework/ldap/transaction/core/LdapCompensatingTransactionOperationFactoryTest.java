package org.springframework.ldap.transaction.core;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.CompensatingTransactionOperationRecorder;
import org.springframework.ldap.transaction.core.BindOperationRecorder;
import org.springframework.ldap.transaction.core.LdapCompensatingTransactionOperationFactory;
import org.springframework.ldap.transaction.core.ModifyAttributesOperationRecorder;
import org.springframework.ldap.transaction.core.RebindOperationRecorder;
import org.springframework.ldap.transaction.core.RenameOperationRecorder;
import org.springframework.ldap.transaction.core.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.core.UnbindOperationRecorder;

public class LdapCompensatingTransactionOperationFactoryTest extends TestCase {
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

    public void testGetRecordingOperation_Bind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null, renamingStrategyMock);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation("bind");
        assertTrue(result instanceof BindOperationRecorder);
        BindOperationRecorder bindOperationRecorder = (BindOperationRecorder) result;
        assertSame(ldapOperationsMock, bindOperationRecorder
                .getLdapOperations());
    }

    public void testGetRecordingOperation_Rebind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null, renamingStrategyMock);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation("rebind");
        assertTrue(result instanceof RebindOperationRecorder);
        RebindOperationRecorder rebindOperationRecorder = (RebindOperationRecorder) result;
        assertSame(ldapOperationsMock, rebindOperationRecorder
                .getLdapOperations());
        assertSame(renamingStrategyMock, rebindOperationRecorder
                .getRenamingStrategy());
    }

    public void testGetRecordingOperation_Rename() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null, renamingStrategyMock);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation("rename");
        assertTrue(result instanceof RenameOperationRecorder);
        RenameOperationRecorder recordingOperation = (RenameOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    public void testGetRecordingOperation_ModifyAttributes() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null, renamingStrategyMock);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation("modifyAttributes");
        assertTrue(result instanceof ModifyAttributesOperationRecorder);
        ModifyAttributesOperationRecorder recordingOperation = (ModifyAttributesOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    public void testGetRecordingOperation_Unbind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null, renamingStrategyMock);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionOperationRecorder result = tested
                .createRecordingOperation("unbind");
        assertTrue(result instanceof UnbindOperationRecorder);
        UnbindOperationRecorder recordingOperation = (UnbindOperationRecorder) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
        assertSame(renamingStrategyMock, recordingOperation
                .getRenamingStrategy());
    }

}
