package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.ContextSource;
import org.springframework.ldap.support.transaction.ContextSourceTransactionManager;
import org.springframework.ldap.support.transaction.DirContextHolder;
import org.springframework.ldap.support.transaction.ContextSourceTransactionManager.ContextSourceTransactionObject;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ContextSourceTransactionManagerTest extends TestCase {

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl contextControl;

    private DirContext contextMock;

    private ContextSourceTransactionManager tested;

    private MockControl transactionDefinitionControl;

    private TransactionDefinition transactionDefinitionMock;

    protected void setUp() throws Exception {
        super.setUp();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        contextControl = MockControl.createControl(DirContext.class);
        contextMock = (DirContext) contextControl.getMock();

        transactionDefinitionControl = MockControl
                .createControl(TransactionDefinition.class);
        transactionDefinitionMock = (TransactionDefinition) transactionDefinitionControl
                .getMock();

        tested = new ContextSourceTransactionManager();
        tested.setContextSource(contextSourceMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextControl = null;
        contextMock = null;

        contextSourceControl = null;
        contextSourceMock = null;

        transactionDefinitionControl = null;
        transactionDefinitionMock = null;

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    public void testDoGetTransaction() {
        Object result = tested.doGetTransaction();

        assertNotNull(result);
        assertTrue(result instanceof ContextSourceTransactionObject);
        ContextSourceTransactionObject transactionObject = (ContextSourceTransactionObject) result;
        assertNull(transactionObject.getContextHolder());
    }

    public void testDoGetTransactionTransactionActive() {
        DirContextHolder expectedContextHolder = new DirContextHolder(null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);
        Object result = tested.doGetTransaction();

        assertSame(expectedContextHolder,
                ((ContextSourceTransactionObject) result).getContextHolder());
    }

    public void testDoBegin() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), contextMock);

        contextSourceControl.replay();

        ContextSourceTransactionObject expectedTransactionObject = new ContextSourceTransactionObject(
                null);
        tested.doBegin(expectedTransactionObject, transactionDefinitionMock);

        contextSourceControl.verify();

        DirContextHolder foundContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSourceMock);
        assertSame(contextMock, foundContextHolder.getCtx());
    }

    public void testDoCommit() {
    }

    public void testDoRollback() {
        fail("Not yet implemented");
    }

    public void testDoCleanupAfterCompletion() throws Exception {
        DirContextHolder expectedContextHolder = new DirContextHolder(
                contextMock);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);

        contextMock.close();
        contextControl.replay();
        
        tested.doCleanupAfterCompletion(new ContextSourceTransactionObject(
                expectedContextHolder));

        contextControl.verify();
        assertNull(TransactionSynchronizationManager
                .getResource(contextSourceMock));

    }
}
