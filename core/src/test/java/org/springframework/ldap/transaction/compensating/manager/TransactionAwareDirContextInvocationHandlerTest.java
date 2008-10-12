/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareDirContextInvocationHandler;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionAwareDirContextInvocationHandlerTest extends TestCase {

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private TransactionAwareDirContextInvocationHandler tested;

    private DirContextHolder holder;

    protected void setUp() throws Exception {
        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        holder = new DirContextHolder(null, dirContextMock);
        tested = new TransactionAwareDirContextInvocationHandler(null, null);
    }

    protected void tearDown() throws Exception {
        dirContextControl = null;
        dirContextMock = null;

        contextSourceControl = null;
        contextSourceMock = null;

        holder = null;

        tested = null;
    }

    protected void replay() {
        dirContextControl.replay();
        contextSourceControl.replay();
    }

    protected void verify() {
        dirContextControl.verify();
        contextSourceControl.verify();
    }

    public void testDoCloseConnection_NoTransaction() throws NamingException {
        dirContextMock.close();

        replay();
        tested.doCloseConnection(dirContextMock, contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_ActiveTransaction()
            throws NamingException {
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        // Context should not be closed.

        replay();
        tested.doCloseConnection(dirContextMock, contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_NotTransactionalContext()
            throws NamingException {
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        MockControl dirContextControl2 = MockControl
                .createControl(DirContext.class);
        DirContext dirContextMock2 = (DirContext) dirContextControl2.getMock();

        dirContextMock2.close();

        dirContextControl2.replay();
        replay();
        tested.doCloseConnection(dirContextMock2, contextSourceMock);
        verify();
        dirContextControl2.verify();
    }

}
