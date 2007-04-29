/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.transaction.compensating.support;

import java.util.Stack;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;
import org.springframework.transaction.compensating.support.DefaultCompensatingTransactionOperationManager;

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
        Object expectedResource = new Object();

        operationFactoryControl.expectAndReturn(operationFactoryMock
                .createRecordingOperation(expectedResource, "some method"),
                operationRecorderMock);
        operationRecorderControl.expectAndReturn(operationRecorderMock
                .recordOperation(expectedArgs), operationExecutorMock);
        operationExecutorMock.performOperation();

        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        replay();
        tested.performOperation(expectedResource, "some method", expectedArgs);
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
