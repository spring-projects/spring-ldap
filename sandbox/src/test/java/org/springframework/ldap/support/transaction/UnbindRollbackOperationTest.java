package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class UnbindRollbackOperationTest extends TestCase {
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
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        UnbindRollbackOperation tested = new UnbindRollbackOperation(ldapOperationsMock, expectedDn);
        
        ldapOperationsMock.unbind(expectedDn);
        
        replay();
        //perform teste
        tested.rollback();
        verify();
    }

}
