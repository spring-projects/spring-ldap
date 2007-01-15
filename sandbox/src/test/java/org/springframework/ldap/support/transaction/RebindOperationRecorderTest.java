package org.springframework.ldap.support.transaction;

import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

public class RebindOperationRecorderTest extends TestCase {
    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl renamingStrategyControl;

    private TempEntryRenamingStrategy renamingStrategyMock;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        renamingStrategyControl = MockControl
                .createControl(TempEntryRenamingStrategy.class);
        renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl
                .getMock();

    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        renamingStrategyControl = null;
        renamingStrategyMock = null;

    }

    protected void replay() {
        ldapOperationsControl.replay();
        renamingStrategyControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        renamingStrategyControl.verify();
    }

    public void testRecordOperation() {
        final DistinguishedName expectedDn = new DistinguishedName(
                "cn=john doe");
        final DistinguishedName expectedTempDn = new DistinguishedName(
                "cn=john doe");
        RebindOperationRecorder tested = new RebindOperationRecorder(
                ldapOperationsMock);
        tested.setRenamingStrategy(renamingStrategyMock);

        renamingStrategyControl.expectAndReturn(renamingStrategyMock
                .getTemporaryName(expectedDn), expectedTempDn);

        ldapOperationsMock.rename(expectedDn, expectedTempDn);

        replay();
        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        
        // perform test
        CompensatingTransactionOperationExecutor result = tested
                .recordOperation(new Object[] { expectedDn, expectedObject,
                        expectedAttributes });
        verify();

        assertTrue(result instanceof RebindOperationExecutor);
        RebindOperationExecutor rollbackOperation = (RebindOperationExecutor) result;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempDn, rollbackOperation.getTemporaryDn());
        assertSame(expectedObject, rollbackOperation.getOriginalObject());
        assertSame(expectedAttributes, rollbackOperation
                .getOriginalAttributes());
    }
}
