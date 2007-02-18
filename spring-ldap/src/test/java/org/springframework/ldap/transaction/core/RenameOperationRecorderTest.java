package org.springframework.ldap.transaction.core;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.CompensatingTransactionOperationExecutor;
import org.springframework.ldap.transaction.core.RenameOperationExecutor;
import org.springframework.ldap.transaction.core.RenameOperationRecorder;

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
