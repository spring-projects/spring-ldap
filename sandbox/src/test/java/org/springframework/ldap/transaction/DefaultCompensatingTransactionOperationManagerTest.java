package org.springframework.ldap.transaction;

import java.util.Stack;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.transaction.CompensatingTransactionOperationExecutor;
import org.springframework.ldap.transaction.CompensatingTransactionOperationFactory;
import org.springframework.ldap.transaction.CompensatingTransactionOperationRecorder;
import org.springframework.ldap.transaction.DefaultCompensatingTransactionOperationManager;

public class DefaultCompensatingTransactionOperationManagerTest extends
        TestCase {

    private MockControl operationExecutorControl;

    private CompensatingTransactionOperationExecutor operationExecutorMock;

    private MockControl operationFactoryControl;

    private CompensatingTransactionOperationFactory operationFactoryMock;

    private MockControl operationRecorderControl;

    private CompensatingTransactionOperationRecorder operationRecorderMock;

    protected void setUp() throws Exception {
        super.setUp();
        operationExecutorControl = MockControl
                .createControl(CompensatingTransactionOperationExecutor.class);
        operationExecutorMock = (CompensatingTransactionOperationExecutor) operationExecutorControl
                .getMock();

        operationFactoryControl = MockControl
                .createControl(CompensatingTransactionOperationFactory.class);
        operationFactoryMock = (CompensatingTransactionOperationFactory) operationFactoryControl
                .getMock();

        operationRecorderControl = MockControl
                .createControl(CompensatingTransactionOperationRecorder.class);
        operationRecorderMock = (CompensatingTransactionOperationRecorder) operationRecorderControl
                .getMock();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        operationExecutorControl = null;
        operationExecutorMock = null;

        operationFactoryControl = null;
        operationFactoryMock = null;

        operationRecorderControl = null;
        operationRecorderMock = null;
    }

    protected void replay() {
        operationExecutorControl.replay();
        operationFactoryControl.replay();
        operationRecorderControl.replay();
    }

    protected void verify() {
        operationExecutorControl.verify();
        operationFactoryControl.verify();
        operationRecorderControl.verify();
    }

    public void testPerformOperation() {
        Object[] expectedArgs = new Object[0];

        operationFactoryControl
                .expectAndReturn(operationFactoryMock
                        .createRecordingOperation("some method"),
                        operationRecorderMock);
        operationRecorderControl.expectAndReturn(operationRecorderMock
                .recordOperation(expectedArgs), operationExecutorMock);
        operationExecutorMock.performOperation();

        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        replay();
        tested.performOperation("some method", expectedArgs);
        verify();

        Stack result = tested.getRollbackOperations();
        assertFalse(result.isEmpty());
        assertSame(operationExecutorMock, result.peek());
    }

    public void testRollback() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getRollbackOperations().push(operationExecutorMock);

        operationExecutorMock.rollback();

        replay();
        tested.rollback();
        verify();
    }

    public void testCommit() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getRollbackOperations().push(operationExecutorMock);

        operationExecutorMock.commit();

        replay();
        tested.commit();
        verify();
    }
}
