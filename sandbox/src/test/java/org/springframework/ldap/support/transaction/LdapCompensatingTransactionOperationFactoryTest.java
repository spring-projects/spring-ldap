package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;

public class LdapCompensatingTransactionOperationFactoryTest extends TestCase {
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

    public void testGetRecordingOperation_Bind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionRecordingOperation result = tested
                .createRecordingOperation("bind");
        assertTrue(result instanceof BindRecordingOperation);
        BindRecordingOperation bindRecordingOperation = (BindRecordingOperation) result;
        assertSame(ldapOperationsMock, bindRecordingOperation
                .getLdapOperations());
    }

    public void testGetRecordingOperation_Rebind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionRecordingOperation result = tested
                .createRecordingOperation("rebind");
        assertTrue(result instanceof RebindRecordingOperation);
        RebindRecordingOperation rebindRecordingOperation = (RebindRecordingOperation) result;
        assertSame(ldapOperationsMock, rebindRecordingOperation
                .getLdapOperations());
    }

    public void testGetRecordingOperation_Rename() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionRecordingOperation result = tested
                .createRecordingOperation("rename");
        assertTrue(result instanceof RenameRecordingOperation);
        RenameRecordingOperation recordingOperation = (RenameRecordingOperation) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    public void testGetRecordingOperation_ModifyAttributes() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionRecordingOperation result = tested
                .createRecordingOperation("modifyAttributes");
        assertTrue(result instanceof ModifyAttributesRecordingOperation);
        ModifyAttributesRecordingOperation recordingOperation = (ModifyAttributesRecordingOperation) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

    public void testGetRecordingOperation_Unbind() throws Exception {
        LdapCompensatingTransactionOperationFactory tested = new LdapCompensatingTransactionOperationFactory(
                null);
        tested.setLdapOperations(ldapOperationsMock);

        CompensatingTransactionRecordingOperation result = tested
                .createRecordingOperation("unbind");
        assertTrue(result instanceof UnbindRecordingOperation);
        UnbindRecordingOperation recordingOperation = (UnbindRecordingOperation) result;
        assertSame(ldapOperationsMock, recordingOperation.getLdapOperations());
    }

}
