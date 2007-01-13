package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class RebindRecordingOperationTest extends TestCase {
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

    public void testRecordOperation() {
        final DistinguishedName expectedDn = new DistinguishedName(
                "cn=john doe");
        final DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe");
        RebindRecordingOperation tested = new RebindRecordingOperation(
                ldapOperationsMock) {
            Name getTemporaryName(Name originalName) {
                assertSame(expectedDn, originalName);
                return expectedTempDn;
            }
        };

        ldapOperationsMock.rename(expectedDn, expectedTempDn);

        replay();
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        // perform test
        CompensatingTransactionRollbackOperation result = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });
        verify();

        assertTrue(result instanceof RebindRollbackOperation);
        RebindRollbackOperation rollbackOperation = (RebindRollbackOperation) result;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempDn, rollbackOperation.getTemporaryDn());
        assertSame(expectedObject, rollbackOperation.getOriginalObject());
        assertSame(expectedAttributes, rollbackOperation
                .getOriginalAttributes());
    }
}
