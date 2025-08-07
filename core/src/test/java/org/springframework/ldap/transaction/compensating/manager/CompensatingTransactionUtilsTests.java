/*
 * Copyright 2006-present the original author or authors.
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

import java.lang.reflect.Method;

import javax.naming.directory.DirContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.core.ContextSource;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.compensating.support.CompensatingTransactionUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompensatingTransactionUtilsTests {

	private DirContext dirContextMock;

	private ContextSource contextSourceMock;

	private CompensatingTransactionOperationManager operationManagerMock;

	@BeforeEach
	public void setUp() throws Exception {
		this.dirContextMock = mock(DirContext.class);
		this.contextSourceMock = mock(ContextSource.class);
		this.operationManagerMock = mock(CompensatingTransactionOperationManager.class);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void testPerformOperation() throws Throwable {
		CompensatingTransactionHolderSupport holder = new DirContextHolder(null, this.dirContextMock);
		holder.setTransactionOperationManager(this.operationManagerMock);

		TransactionSynchronizationManager.bindResource(this.contextSourceMock, holder);

		Object[] expectedArgs = new Object[] { "someDn" };

		CompensatingTransactionUtils.performOperation(this.contextSourceMock, this.dirContextMock, getUnbindMethod(),
				expectedArgs);
		verify(this.operationManagerMock).performOperation(this.dirContextMock, "unbind", expectedArgs);
	}

	@Test
	public void testPerformOperation_NoTransaction() throws Throwable {
		Object[] expectedArgs = new Object[] { "someDn" };

		CompensatingTransactionUtils.performOperation(this.contextSourceMock, this.dirContextMock, getUnbindMethod(),
				expectedArgs);
		verify(this.dirContextMock).unbind("someDn");
	}

	private Method getUnbindMethod() throws NoSuchMethodException {
		return DirContext.class.getMethod("unbind", new Class[] { String.class });
	}

	public void dummyMethod() {

	}

}
