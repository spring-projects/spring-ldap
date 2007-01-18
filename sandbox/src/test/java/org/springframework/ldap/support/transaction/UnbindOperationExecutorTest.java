package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class UnbindOperationExecutorTest extends TestCase {
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
        DistinguishedName expectedOldName = new DistinguishedName("cn=oldDn");
        DistinguishedName expectedTempName = new DistinguishedName("cn=newDn");
        UnbindOperationExecutor tested = new UnbindOperationExecutor(
                ldapOperationsMock, expectedOldName, expectedTempName);

        ldapOperationsMock.rename(expectedOldName, expectedTempName);
        
        replay();
        // Perform test
        tested.performOperation();
        verify();
    }

    public void testCommit() {
        DistinguishedName expectedOldName = new DistinguishedName("cn=oldDn");
        DistinguishedName expectedTempName = new DistinguishedName("cn=newDn");
        UnbindOperationExecutor tested = new UnbindOperationExecutor(
                ldapOperationsMock, expectedOldName, expectedTempName);

        ldapOperationsMock.unbind(expectedTempName);

        replay();
        // Perform test
        tested.commit();
        verify();
    }

    public void testRollback() {
        DistinguishedName expectedOldName = new DistinguishedName("cn=oldDn");
        DistinguishedName expectedTempName = new DistinguishedName("cn=newDn");
        UnbindOperationExecutor tested = new UnbindOperationExecutor(
                ldapOperationsMock, expectedOldName, expectedTempName);

        ldapOperationsMock.rename(expectedTempName, expectedOldName);

        replay();
        // Perform test
        tested.rollback();
        verify();
    }
}
