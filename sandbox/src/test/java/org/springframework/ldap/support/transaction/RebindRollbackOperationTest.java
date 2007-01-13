package org.springframework.ldap.support.transaction;

import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class RebindRollbackOperationTest extends TestCase {

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
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindRollbackOperation tested = new RebindRollbackOperation(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.bind(expectedOriginalDn, expectedObject,
                expectedAttributes);

        replay();
        // perform test
        tested.performOperation();
        verify();
    }

    public void testCommit() {
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindRollbackOperation tested = new RebindRollbackOperation(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.unbind(expectedTempDn);

        replay();
        // perform test
        tested.commit();
        verify();
    }

    public void testRollback() {
        DistinguishedName expectedOriginalDn = new DistinguishedName(
                "cn=john doe");
        DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe_temp");
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        RebindRollbackOperation tested = new RebindRollbackOperation(
                ldapOperationsMock, expectedOriginalDn, expectedTempDn,
                expectedObject, expectedAttributes);

        ldapOperationsMock.unbind(expectedOriginalDn);
        ldapOperationsMock.rename(expectedTempDn, expectedOriginalDn);

        replay();
        // perform test
        tested.rollback();
        verify();
    }
}
