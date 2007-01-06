package org.springframework.ldap.support.transaction;

import java.util.Stack;

import junit.framework.TestCase;

import org.easymock.MockControl;

public class DefaultCompensatingTransactionDataManagerTest extends TestCase {

    private MockControl rollbackOperationControl;

    private CompensatingTransactionRollbackOperation rollbackOperationMock;

    protected void setUp() throws Exception {
        super.setUp();
        rollbackOperationControl = MockControl
                .createControl(CompensatingTransactionRollbackOperation.class);
        rollbackOperationMock = (CompensatingTransactionRollbackOperation) rollbackOperationControl
                .getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        rollbackOperationControl = null;
        rollbackOperationMock = null;
    }

    protected void replay() {
        rollbackOperationControl.replay();
    }

    protected void verify() {
        rollbackOperationControl.verify();
    }

    public void testOperationPerformed() {
        DefaultCompensatingTransactionDataManager tested = new DefaultCompensatingTransactionDataManager();

        replay();
        tested.operationPerformed(rollbackOperationMock);
        verify();

        Stack result = tested.getRollbackOperations();
        assertFalse(result.isEmpty());
        assertSame(rollbackOperationMock, result.peek());
    }

    public void testRollback() {
        DefaultCompensatingTransactionDataManager tested = new DefaultCompensatingTransactionDataManager();
        tested.getRollbackOperations().push(rollbackOperationMock);

        rollbackOperationMock.rollback();

        replay();
        tested.rollback();
        verify();
    }
}
