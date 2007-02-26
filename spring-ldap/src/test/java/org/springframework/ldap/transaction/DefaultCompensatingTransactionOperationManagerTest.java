package org.springframework.ldap.transaction;

import java.util.Stack;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.transaction.TransactionSystemException;

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

        Stack result = tested.getOperationExecutors();
        assertFalse(result.isEmpty());
        assertSame(operationExecutorMock, result.peek());
    }

    public void testRollback() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        operationExecutorMock.rollback();

        replay();
        tested.rollback();
        verify();
    }

    public void testRollback_Exception() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        operationExecutorMock.rollback();
        operationExecutorControl.setThrowable(new RuntimeException());

        replay();
        try {
            tested.rollback();
            fail("TransactionSystemException expected");
        } catch (TransactionSystemException expected) {
            assertTrue(true);
        }
        verify();
    }

    public void testCommit() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        operationExecutorMock.commit();

        replay();
        tested.commit();
        verify();
    }

    public void testCommit_Exception() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        operationExecutorMock.commit();
        operationExecutorControl.setThrowable(new RuntimeException());

        replay();
        try {
            tested.commit();
            fail("TransactionSystemException expected");
        } catch (TransactionSystemException expected) {
            assertTrue(true);
        }
        verify();
    }
}
