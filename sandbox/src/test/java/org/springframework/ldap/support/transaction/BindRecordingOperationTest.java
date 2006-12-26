package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DistinguishedName;

public class BindRecordingOperationTest extends TestCase {
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

    public void testPerformOperation_DistinguishedName() {
        BindRecordingOperation tested = new BindRecordingOperation(
                ldapOperationsMock);
        DistinguishedName expectedDn = new DistinguishedName("cn=John Doe");

        // Perform test.
        CompensatingTransactionRollbackOperation operation = tested
                .performOperation(new Object[] { expectedDn });

        assertTrue(operation instanceof UnbindRollbackOperation);
        UnbindRollbackOperation rollbackOperation = (UnbindRollbackOperation) operation;
        assertSame(expectedDn, rollbackOperation.getDn());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
    }

    public void testPerformOperation_String() {
        BindRecordingOperation tested = new BindRecordingOperation(
                ldapOperationsMock);
        String expectedDn = "cn=John Doe";

        // Perform test.
        CompensatingTransactionRollbackOperation operation = tested
                .performOperation(new Object[] { expectedDn });

        assertTrue(operation instanceof UnbindRollbackOperation);
        UnbindRollbackOperation rollbackOperation = (UnbindRollbackOperation) operation;
        assertEquals(expectedDn, rollbackOperation.getDn().toString());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
    }

    public void testPerformOperation_Invalid() {
        BindRecordingOperation tested = new BindRecordingOperation(
                ldapOperationsMock);
        Object expectedDn = new Object();

        try {
            // Perform test.
            tested.performOperation(new Object[] { expectedDn });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

    }
}
