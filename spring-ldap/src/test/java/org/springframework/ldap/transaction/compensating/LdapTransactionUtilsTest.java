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
package org.springframework.ldap.transaction.compensating;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.transaction.compensating.DirContextHolder;
import org.springframework.ldap.transaction.compensating.LdapTransactionUtils;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class LdapTransactionUtilsTest extends TestCase {

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    protected void setUp() throws Exception {
        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    protected void tearDown() throws Exception {
        dirContextControl = null;
        dirContextMock = null;

        contextSourceControl = null;
        contextSourceMock = null;
    }

    protected void replay() {
        dirContextControl.replay();
        contextSourceControl.replay();
    }

    protected void verify() {
        dirContextControl.verify();
        contextSourceControl.verify();
    }

    public void testCloseContext() throws NamingException {
        dirContextMock.close();

        replay();
        LdapUtils.closeContext(dirContextMock);
        verify();
    }

    public void testCloseContext_NullContext() throws NamingException {
        replay();
        LdapUtils.closeContext(null);
        verify();
    }

    public void testDoCloseConnection_NoTransaction() throws NamingException {
        dirContextMock.close();

        replay();
        LdapTransactionUtils.doCloseConnection(dirContextMock,
                contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_ActiveTransaction()
            throws NamingException {
        CompensatingTransactionHolderSupport holder = new DirContextHolder(
                dirContextMock, null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        // Context should not be closed.

        replay();
        LdapTransactionUtils.doCloseConnection(dirContextMock,
                contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_NotTransactionalContext()
            throws NamingException {
        CompensatingTransactionHolderSupport holder = new DirContextHolder(
                dirContextMock, null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        MockControl dirContextControl2 = MockControl
                .createControl(DirContext.class);
        DirContext dirContextMock2 = (DirContext) dirContextControl2.getMock();

        dirContextMock2.close();

        dirContextControl2.replay();
        replay();
        LdapTransactionUtils.doCloseConnection(dirContextMock2,
                contextSourceMock);
        verify();
        dirContextControl2.verify();
    }

    public void testIsSupportedWriteTransactionOperation() {
        assertTrue(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("bind"));
        assertTrue(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("rebind"));
        assertTrue(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("unbind"));
        assertTrue(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("modifyAttributes"));
        assertTrue(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("rename"));
        assertFalse(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("lookup"));
        assertFalse(LdapTransactionUtils
                .isSupportedWriteTransactionOperation("search"));
    }

    public void dummyMethod() {

    }

}
