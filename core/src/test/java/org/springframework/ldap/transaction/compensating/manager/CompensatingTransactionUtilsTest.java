/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.ContextSource;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.compensating.support.CompensatingTransactionUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.naming.directory.DirContext;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompensatingTransactionUtilsTest {

    private DirContext dirContextMock;

    private ContextSource contextSourceMock;

    private CompensatingTransactionOperationManager operationManagerMock;

    @Before
    public void setUp() throws Exception {
        dirContextMock = mock(DirContext.class);
        contextSourceMock = mock(ContextSource.class);
        operationManagerMock = mock(CompensatingTransactionOperationManager.class);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void testPerformOperation() throws Throwable {
        CompensatingTransactionHolderSupport holder = new DirContextHolder(
                null, dirContextMock);
        holder.setTransactionOperationManager(operationManagerMock);

        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        Object[] expectedArgs = new Object[] { "someDn" };

        CompensatingTransactionUtils.performOperation(contextSourceMock,
                dirContextMock, getUnbindMethod(), expectedArgs);
        verify(operationManagerMock).performOperation(dirContextMock, "unbind",
                expectedArgs);
    }

    @Test
    public void testPerformOperation_NoTransaction() throws Throwable {
        Object[] expectedArgs = new Object[] { "someDn" };

        CompensatingTransactionUtils.performOperation(contextSourceMock,
                dirContextMock, getUnbindMethod(), expectedArgs);
        verify(dirContextMock).unbind("someDn");
    }

    private Method getUnbindMethod() throws NoSuchMethodException {
        return DirContext.class.getMethod("unbind",
                new Class[] { String.class });
    }

    public void dummyMethod() {

    }

}
