package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DirContextOperations;
import org.springframework.ldap.support.DistinguishedName;

public class RebindRollbackOperationTest extends TestCase {

    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl dirContextOperationsControl;

    private DirContextOperations dirContextOperationsMock;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        dirContextOperationsControl = MockControl
                .createControl(DirContextOperations.class);
        dirContextOperationsMock = (DirContextOperations) dirContextOperationsControl
                .getMock();
    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        dirContextOperationsControl = null;
        dirContextOperationsMock = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
        dirContextOperationsControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        dirContextOperationsControl.verify();
    }

    public void testRollback() {
        RebindRollbackOperation tested = new RebindRollbackOperation(
                ldapOperationsMock, dirContextOperationsMock);

        DistinguishedName expectedName = new DistinguishedName("cn=john doe");
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getDn(), expectedName);
        ldapOperationsMock.rebind(expectedName, dirContextOperationsMock, null);

        replay();
        // perform test
        tested.rollback();
        verify();
    }
}
