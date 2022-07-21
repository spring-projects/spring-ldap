/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.transaction.compensating.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultCompensatingTransactionOperationManagerTest {

    private CompensatingTransactionOperationExecutor operationExecutorMock;
    private CompensatingTransactionOperationFactory operationFactoryMock;
    private CompensatingTransactionOperationRecorder operationRecorderMock;

    @Before
    public void setUp() throws Exception {
        operationExecutorMock = mock(CompensatingTransactionOperationExecutor.class);
        operationFactoryMock = mock(CompensatingTransactionOperationFactory.class);
        operationRecorderMock = mock(CompensatingTransactionOperationRecorder.class);

    }

    @Test
    public void testPerformOperation() {
        Object[] expectedArgs = new Object[0];
        Object expectedResource = new Object();

        when(operationFactoryMock.createRecordingOperation(expectedResource, "some method"))
                .thenReturn(operationRecorderMock);
        when(operationRecorderMock.recordOperation(expectedArgs)).thenReturn(operationExecutorMock);

        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.performOperation(expectedResource, "some method", expectedArgs);
        verify(operationExecutorMock).performOperation();

        Stack result = tested.getOperationExecutors();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.peek()).isSameAs(operationExecutorMock);
    }

    @Test
    public void testRollback() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        tested.rollback();
        verify(operationExecutorMock).rollback();
    }

    @Test(expected = TransactionSystemException.class)
    public void testRollback_Exception() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        doThrow(new RuntimeException()).when(operationExecutorMock).rollback();

        tested.rollback();
    }

    @Test
    public void testCommit() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        tested.commit();
        verify(operationExecutorMock).commit();
    }

    @Test(expected = TransactionSystemException.class)
    public void testCommit_Exception() {
        DefaultCompensatingTransactionOperationManager tested = new DefaultCompensatingTransactionOperationManager(
                operationFactoryMock);
        tested.getOperationExecutors().push(operationExecutorMock);

        doThrow(new RuntimeException()).when(operationExecutorMock).commit();

        tested.commit();
    }
}
