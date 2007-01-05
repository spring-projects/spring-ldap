package org.springframework.ldap.support.transaction;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DirContextOperations;
import org.springframework.ldap.support.DistinguishedName;

import com.sun.org.apache.bcel.internal.generic.DNEG;

import junit.framework.TestCase;

public class BindRollbackOperationTest extends TestCase {
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
        BindRollbackOperation tested = new BindRollbackOperation(
                ldapOperationsMock, dirContextOperationsMock);

        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        dirContextOperationsControl.expectAndReturn(dirContextOperationsMock
                .getDn(), expectedDn);
        ldapOperationsMock.bind(expectedDn, dirContextOperationsMock, null);

        replay();
        // Perform test
        tested.rollback();
        verify();
    }

}
