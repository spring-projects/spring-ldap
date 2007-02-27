package org.springframework.ldap.support;

import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.CompensatingTransactionOperationManager;
import org.springframework.ldap.transaction.core.DirContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class LdapUtilsTest extends TestCase {

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl operationManagerControl;

    private CompensatingTransactionOperationManager operationManagerMock;

    protected void setUp() throws Exception {
        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        operationManagerControl = MockControl
                .createControl(CompensatingTransactionOperationManager.class);
        operationManagerMock = (CompensatingTransactionOperationManager) operationManagerControl
                .getMock();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    protected void tearDown() throws Exception {
        dirContextControl = null;
        dirContextMock = null;

        contextSourceControl = null;
        contextSourceMock = null;

        operationManagerControl = null;
        operationManagerMock = null;
    }

    protected void replay() {
        dirContextControl.replay();
        contextSourceControl.replay();
        operationManagerControl.replay();
    }

    protected void verify() {
        dirContextControl.verify();
        contextSourceControl.verify();
        operationManagerControl.verify();
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
        LdapUtils.doCloseConnection(dirContextMock, contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_ActiveTransaction()
            throws NamingException {
        DirContextHolder holder = new DirContextHolder(dirContextMock, null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        // Context should not be closed.

        replay();
        LdapUtils.doCloseConnection(dirContextMock, contextSourceMock);
        verify();
    }

    public void testDoCloseConnection_NotTransactionalContext()
            throws NamingException {
        DirContextHolder holder = new DirContextHolder(dirContextMock, null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        MockControl dirContextControl2 = MockControl
                .createControl(DirContext.class);
        DirContext dirContextMock2 = (DirContext) dirContextControl2.getMock();

        dirContextMock2.close();

        dirContextControl2.replay();
        replay();
        LdapUtils.doCloseConnection(dirContextMock2, contextSourceMock);
        verify();
        dirContextControl2.verify();
    }

    public void testIsSupportedWriteTransactionOperation() {
        assertTrue(LdapUtils.isSupportedWriteTransactionOperation("bind"));
        assertTrue(LdapUtils.isSupportedWriteTransactionOperation("rebind"));
        assertTrue(LdapUtils.isSupportedWriteTransactionOperation("unbind"));
        assertTrue(LdapUtils
                .isSupportedWriteTransactionOperation("modifyAttributes"));
        assertTrue(LdapUtils.isSupportedWriteTransactionOperation("rename"));
        assertFalse(LdapUtils.isSupportedWriteTransactionOperation("lookup"));
        assertFalse(LdapUtils.isSupportedWriteTransactionOperation("search"));
    }

    public void testPerformOperation() throws Throwable {
        DirContextHolder holder = new DirContextHolder(dirContextMock, null);
        holder.setTransactionOperationManager(operationManagerMock);

        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        Object[] expectedArgs = new Object[] { "someDn" };
        operationManagerMock.performOperation("unbind", expectedArgs);

        replay();
        LdapUtils.performOperation(contextSourceMock, dirContextMock,
                getUnbindMethod(), expectedArgs);
        verify();
    }

    public void testPerformOperation_NoTransaction() throws Throwable {
        Object[] expectedArgs = new Object[] { "someDn" };
        dirContextMock.unbind("someDn");

        replay();
        LdapUtils.performOperation(contextSourceMock, dirContextMock,
                getUnbindMethod(), expectedArgs);
        verify();
    }

    private Method getUnbindMethod() throws NoSuchMethodException {
        return DirContext.class.getMethod("unbind",
                new Class[] { String.class });
    }

    public void dummyMethod() {

    }

}
