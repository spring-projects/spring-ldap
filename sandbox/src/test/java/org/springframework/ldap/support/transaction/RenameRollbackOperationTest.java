package org.springframework.ldap.support.transaction;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

import junit.framework.TestCase;

public class RenameRollbackOperationTest extends TestCase {
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
        DistinguishedName expectedNewName = new DistinguishedName("ou=newOu");
        DistinguishedName expectedOldName = new DistinguishedName("ou=someou");
        RenameRollbackOperation tested = new RenameRollbackOperation(
                ldapOperationsMock, expectedOldName, expectedNewName);

        ldapOperationsMock.rename(expectedOldName, expectedNewName);

        replay();
        // Perform test.
        tested.performOperation();
        verify();
    }

    public void testCommit() {
        DistinguishedName expectedNewName = new DistinguishedName("ou=newOu");
        DistinguishedName expectedOldName = new DistinguishedName("ou=someou");
        RenameRollbackOperation tested = new RenameRollbackOperation(
                ldapOperationsMock, expectedOldName, expectedNewName);

        // Nothing to do for this operation.

        replay();
        // Perform test.
        tested.commit();
        verify();
    }

    public void testRollback() {
        DistinguishedName expectedNewName = new DistinguishedName("ou=newOu");
        DistinguishedName expectedOldName = new DistinguishedName("ou=someou");
        RenameRollbackOperation tested = new RenameRollbackOperation(
                ldapOperationsMock, expectedOldName, expectedNewName);

        ldapOperationsMock.rename(expectedNewName, expectedOldName);

        replay();
        // Perform test.
        tested.rollback();
        verify();
    }

}
