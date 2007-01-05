package org.springframework.ldap.support.transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

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

    public void testPerformOperation() {
        RebindRecordingOperation tested = new RebindRecordingOperation(
                ldapOperationsMock);
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");

        DirContextAdapter expectedValue = new DirContextAdapter();
        ldapOperationsControl.expectAndReturn(ldapOperationsMock
                .lookup(expectedDn), expectedValue);

        replay();
        // perform test
        CompensatingTransactionRollbackOperation result = tested
                .performOperation(new Object[] { expectedDn });
        verify();

        assertTrue(result instanceof RebindRollbackOperation);
        RebindRollbackOperation rollbackOperation = (RebindRollbackOperation) result;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedValue, rollbackOperation.getDirContextOperations());
    }

}
