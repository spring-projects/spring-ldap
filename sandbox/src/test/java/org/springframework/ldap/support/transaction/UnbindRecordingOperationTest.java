package org.springframework.ldap.support.transaction;

import org.easymock.MockControl;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

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

    public void testRecordOperation() {
        UnbindRecordingOperation tested = new UnbindRecordingOperation(
                ldapOperationsMock);
        DistinguishedName expectedDn = new DistinguishedName("cn=john doe");
        DirContextAdapter expectedContext = new DirContextAdapter();

        ldapOperationsControl.expectAndReturn(ldapOperationsMock
                .lookup(expectedDn), expectedContext);
        replay();
        // Perform test
        CompensatingTransactionRollbackOperation operation = tested
                .recordOperation(new Object[] { expectedDn });
        verify();

        // Verify result
        assertTrue(operation instanceof BindRollbackOperation);
        BindRollbackOperation rollbackOperation = (BindRollbackOperation) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedContext, rollbackOperation.getDirContextOperations());
    }

}
