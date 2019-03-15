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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TransactionAwareDirContextInvocationHandlerTest {

    private ContextSource contextSourceMock;
    private DirContext dirContextMock;
    private TransactionAwareDirContextInvocationHandler tested;
    private DirContextHolder holder;

    @Before
    public void setUp() throws Exception {
        dirContextMock = mock(DirContext.class);
        contextSourceMock = mock(ContextSource.class);

        holder = new DirContextHolder(null, dirContextMock);
        tested = new TransactionAwareDirContextInvocationHandler(null, null);
    }

    @Test
    public void testDoCloseConnection_NoTransaction() throws NamingException {
        tested.doCloseConnection(dirContextMock, contextSourceMock);

        verify(dirContextMock).close();
    }

    @Test
    public void testDoCloseConnection_ActiveTransaction()
            throws NamingException {
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        // Context should not be closed.
        verifyNoMoreInteractions(dirContextMock);

        tested.doCloseConnection(dirContextMock, contextSourceMock);
    }

    @Test
    public void testDoCloseConnection_NotTransactionalContext()
            throws NamingException {
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        DirContext dirContextMock2 = mock(DirContext.class);

        tested.doCloseConnection(dirContextMock2, contextSourceMock);
        verify(dirContextMock2).close();
    }

}
