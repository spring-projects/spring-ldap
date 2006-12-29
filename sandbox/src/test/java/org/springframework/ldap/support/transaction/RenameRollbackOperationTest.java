package org.springframework.ldap.support.transaction;

import org.easymock.MockControl;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DistinguishedName;

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

    public void testRollback() {
        DistinguishedName expectedNewName = new DistinguishedName("ou=newOu");
        DistinguishedName expectedOldName = new DistinguishedName("ou=someou");
        RenameRollbackOperation tested = new RenameRollbackOperation(
                ldapOperationsMock, expectedNewName, expectedOldName);

        ldapOperationsMock.rename(expectedNewName, expectedOldName);

        replay();
        // Perform test.
        tested.rollback();
        verify();
    }

}
