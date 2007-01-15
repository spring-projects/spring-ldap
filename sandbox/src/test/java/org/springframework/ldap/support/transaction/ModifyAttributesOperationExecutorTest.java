package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

import junit.framework.TestCase;

public class ModifyAttributesOperationExecutorTest extends TestCase {
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
        ModificationItem[] expectedCompensatingItems = new ModificationItem[0];
        ModificationItem[] expectedActualItems = new ModificationItem[0];

        Name expectedDn = new DistinguishedName("cn=john doe");

        ModifyAttributesOperationExecutor tested = new ModifyAttributesOperationExecutor(
                ldapOperationsMock, expectedDn, expectedActualItems,
                expectedCompensatingItems);

        ldapOperationsMock.modifyAttributes(expectedDn, expectedActualItems);

        replay();
        // Perform test
        tested.performOperation();

        verify();
    }

    public void testCommit() {
        ModificationItem[] expectedCompensatingItems = new ModificationItem[0];
        ModificationItem[] expectedActualItems = new ModificationItem[0];

        Name expectedDn = new DistinguishedName("cn=john doe");

        ModifyAttributesOperationExecutor tested = new ModifyAttributesOperationExecutor(
                ldapOperationsMock, expectedDn, expectedActualItems,
                expectedCompensatingItems);

        // No operation here
        
        replay();
        // Perform test
        tested.commit();

        verify();
    }

    public void testRollback() {
        ModificationItem[] expectedCompensatingItems = new ModificationItem[0];
        ModificationItem[] expectedActualItems = new ModificationItem[0];

        Name expectedDn = new DistinguishedName("cn=john doe");

        ModifyAttributesOperationExecutor tested = new ModifyAttributesOperationExecutor(
                ldapOperationsMock, expectedDn, expectedActualItems,
                expectedCompensatingItems);

        ldapOperationsMock.modifyAttributes(expectedDn,
                expectedCompensatingItems);

        replay();
        // Perform test
        tested.rollback();

        verify();
    }

}
