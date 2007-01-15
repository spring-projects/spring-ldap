package org.springframework.ldap.support.transaction;

import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class BindOperationRecorderTest extends TestCase {
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

    public void testRecordOperation_DistinguishedName() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        DistinguishedName expectedDn = new DistinguishedName("cn=John Doe");

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        // Perform test.
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });

        assertTrue(operation instanceof BindOperationExecutor);
        BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
        assertSame(expectedDn, rollbackOperation.getDn());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedObject, rollbackOperation.getOriginalObject());
        assertSame(expectedAttributes, rollbackOperation
                .getOriginalAttributes());
    }

    public void testPerformOperation_String() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        String expectedDn = "cn=John Doe";

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        // Perform test.
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });

        assertTrue(operation instanceof BindOperationExecutor);
        BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
        assertEquals(expectedDn, rollbackOperation.getDn().toString());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
    }

    public void testPerformOperation_Invalid() {
        BindOperationRecorder tested = new BindOperationRecorder(
                ldapOperationsMock);
        Object expectedDn = new Object();

        try {
            // Perform test.
            tested.recordOperation(new Object[] { expectedDn });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

    }
}
