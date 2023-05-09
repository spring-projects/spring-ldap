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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.ContextSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TransactionAwareDirContextInvocationHandlerTests {

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private TransactionAwareDirContextInvocationHandler tested;

	private DirContextHolder holder;

	@Before
	public void setUp() throws Exception {
		this.dirContextMock = mock(DirContext.class);
		this.contextSourceMock = mock(ContextSource.class);

		this.holder = new DirContextHolder(null, this.dirContextMock);
		this.tested = new TransactionAwareDirContextInvocationHandler(null, null);
	}

	@Test
	public void testDoCloseConnection_NoTransaction() throws NamingException {
		this.tested.doCloseConnection(this.dirContextMock, this.contextSourceMock);

		verify(this.dirContextMock).close();
	}

	@Test
	public void testDoCloseConnection_ActiveTransaction() throws NamingException {
		TransactionSynchronizationManager.bindResource(this.contextSourceMock, this.holder);

		// Context should not be closed.
		verifyNoMoreInteractions(this.dirContextMock);

		this.tested.doCloseConnection(this.dirContextMock, this.contextSourceMock);
	}

	@Test
	public void testDoCloseConnection_NotTransactionalContext() throws NamingException {
		TransactionSynchronizationManager.bindResource(this.contextSourceMock, this.holder);

		DirContext dirContextMock2 = mock(DirContext.class);

		this.tested.doCloseConnection(dirContextMock2, this.contextSourceMock);
		verify(dirContextMock2).close();
	}

}
