package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.transaction.ContextSourceTransactionManager;
import org.springframework.ldap.support.transaction.DirContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ContextSourceTransactionManagerTest extends TestCase {

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl contextControl;

    private DirContext contextMock;

    private ContextSourceTransactionManager tested;

    private MockControl transactionDefinitionControl;

    private MockControl transactionDataManagerControl;

    private CompensatingTransactionDataManager transactionDataManagerMock;

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

        transactionDataManagerControl = MockControl
                .createControl(CompensatingTransactionDataManager.class);
        transactionDataManagerMock = (CompensatingTransactionDataManager) transactionDataManagerControl
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

        transactionDataManagerControl = null;
        transactionDataManagerMock = null;

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

        DirContextHolder expectedContextHolder = new DirContextHolder(
                contextMock);
        expectedContextHolder
                .setTransactionDataManager(transactionDataManagerMock);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);

        transactionDataManagerMock.rollback();
        transactionDataManagerControl.replay();
        ContextSourceTransactionObject transactionObject = new ContextSourceTransactionObject(
                null);
        transactionObject.setContextHolder(expectedContextHolder);
        tested.doRollback(new DefaultTransactionStatus(transactionObject,
                false, false, false, false, null));
        transactionDataManagerControl.verify();
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
        assertNull(expectedContextHolder.getTransactionDataManager());
    }

    public void testSetContextSource_Proxy() {
        TransactionAwareContextSourceProxy proxy = new TransactionAwareContextSourceProxy(
                contextSourceMock);

        // Perform test
        tested.setContextSource(proxy);
        ContextSource result = tested.getContextSource();

        // Verify result
        assertSame(contextSourceMock, result);
    }
}
