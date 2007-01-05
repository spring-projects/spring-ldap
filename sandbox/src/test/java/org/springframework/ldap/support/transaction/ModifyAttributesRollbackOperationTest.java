package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DistinguishedName;

import junit.framework.TestCase;

public class ModifyAttributesRollbackOperationTest extends TestCase {
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
        ModificationItem[] expectedItems = new ModificationItem[0];
        Name expectedDn = new DistinguishedName("cn=john doe");

        ModifyAttributesRollbackOperation tested = new ModifyAttributesRollbackOperation(
                ldapOperationsMock, expectedDn, expectedItems);

        ldapOperationsMock.modifyAttributes(expectedDn, expectedItems);

        replay();
        // Perform test
        tested.rollback();

        verify();
    }

}
