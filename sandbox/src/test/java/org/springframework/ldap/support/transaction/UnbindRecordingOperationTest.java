package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.easymock.MockControl;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

import junit.framework.TestCase;

public class UnbindRecordingOperationTest extends TestCase {
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

    public void testGetTemporaryDN() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe, ou=somecompany, c=SE");
        UnbindRecordingOperation tested = new UnbindRecordingOperation(
                ldapOperationsMock);

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp, ou=somecompany, c=SE", result
                .toString());
        assertNotSame(expectedOriginalName, result);
    }

    public void testGetTemporaryDN_MultivalueDN() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe+sn=doe, ou=somecompany, c=SE");
        UnbindRecordingOperation tested = new UnbindRecordingOperation(
                ldapOperationsMock);

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp+sn=doe, ou=somecompany, c=SE", result
                .toString());
    }

    public void testRecordOperation() {
        final DistinguishedName expectedTempName = new DistinguishedName(
                "cn=john doe_temp");
        final DistinguishedName expectedDn = new DistinguishedName(
                "cn=john doe");
        UnbindRecordingOperation tested = new UnbindRecordingOperation(
                ldapOperationsMock) {
            Name getTemporaryName(Name originalName) {
                assertSame(expectedDn, originalName);
                return expectedTempName;
            }
        };

        ldapOperationsMock.rename(expectedDn, expectedTempName);

        replay();
        // Perform test
        CompensatingTransactionRollbackOperation operation = tested
                .recordOperation(new Object[] { expectedDn });
        verify();

        // Verify result
        assertTrue(operation instanceof BindRollbackOperation);
        BindRollbackOperation rollbackOperation = (BindRollbackOperation) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempName, rollbackOperation.getTemporaryDn());
    }

}
