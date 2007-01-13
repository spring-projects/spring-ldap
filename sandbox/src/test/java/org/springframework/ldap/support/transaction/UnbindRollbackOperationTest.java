package org.springframework.ldap.support.transaction;

import javax.naming.directory.BasicAttributes;

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

    public void testPerformOperation() {
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        UnbindRollbackOperation tested = new UnbindRollbackOperation(
                ldapOperationsMock, expectedDn, expectedObject,
                expectedAttributes);

        ldapOperationsMock.bind(expectedDn, expectedObject, expectedAttributes);

        replay();
        // perform teste
        tested.performOperation();
        verify();
    }

    public void testCommit() {
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        UnbindRollbackOperation tested = new UnbindRollbackOperation(
                ldapOperationsMock, expectedDn, expectedObject,
                expectedAttributes);

        // Nothing to do here.

        replay();
        // perform teste
        tested.commit();
        verify();
    }

    public void testRollback() {
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        UnbindRollbackOperation tested = new UnbindRollbackOperation(
                ldapOperationsMock, expectedDn, null, null);

        ldapOperationsMock.unbind(expectedDn);

        replay();
        // perform teste
        tested.rollback();
        verify();
    }

}
