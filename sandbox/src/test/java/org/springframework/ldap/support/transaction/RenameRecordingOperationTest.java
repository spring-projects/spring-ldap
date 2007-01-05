package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;

public class RenameRecordingOperationTest extends TestCase {

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

    public void testPerformOperation() {
        RenameRecordingOperation tested = new RenameRecordingOperation(
                ldapOperationsMock);

        replay();
        // Perform test
        CompensatingTransactionRollbackOperation operation = tested
                .performOperation(new Object[] { "ou=someou", "ou=newou" });
        verify();

        assertTrue(operation instanceof RenameRollbackOperation);
        RenameRollbackOperation rollbackOperation = (RenameRollbackOperation) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertEquals("ou=newou", rollbackOperation.getFromDn().toString());
        assertEquals("ou=someou", rollbackOperation.getToDn().toString());
    }

}
