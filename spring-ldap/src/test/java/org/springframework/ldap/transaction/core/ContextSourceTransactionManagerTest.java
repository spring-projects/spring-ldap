package org.springframework.ldap.transaction.core;

import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.DirContextHolder;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.core.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.core.TransactionAwareContextSourceProxy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.compensating.support.CompensatingTransactionObject;
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

    private CompensatingTransactionOperationManager transactionDataManagerMock;

    private TransactionDefinition transactionDefinitionMock;

    private MockControl renamingStrategyControl;

    private TempEntryRenamingStrategy renamingStrategyMock;

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
                .createControl(CompensatingTransactionOperationManager.class);
        transactionDataManagerMock = (CompensatingTransactionOperationManager) transactionDataManagerControl
                .getMock();

        renamingStrategyControl = MockControl
                .createControl(TempEntryRenamingStrategy.class);
        renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl
                .getMock();

        tested = new ContextSourceTransactionManager();
        tested.setContextSource(contextSourceMock);
        tested.setRenamingStrategy(renamingStrategyMock);
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

        renamingStrategyControl = null;
        renamingStrategyMock = null;

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    public void testDoGetTransaction() {
        Object result = tested.doGetTransaction();

        assertNotNull(result);
        assertTrue(result instanceof CompensatingTransactionObject);
        CompensatingTransactionObject transactionObject = (CompensatingTransactionObject) result;
        assertNull(transactionObject.getHolder());
    }

    public void testDoGetTransactionTransactionActive() {
        CompensatingTransactionHolderSupport expectedContextHolder = new DirContextHolder(null,
                null);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);
        Object result = tested.doGetTransaction();
        assertSame(expectedContextHolder,
                ((CompensatingTransactionObject) result).getHolder());
    }

    public void testDoBegin() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), contextMock);

        contextSourceControl.replay();

        CompensatingTransactionObject expectedTransactionObject = new CompensatingTransactionObject(
                null);
        tested.doBegin(expectedTransactionObject, transactionDefinitionMock);

        contextSourceControl.verify();

        DirContextHolder foundContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSourceMock);
        assertSame(contextMock, foundContextHolder.getCtx());
        assertSame(renamingStrategyMock, foundContextHolder
                .getRenamingStrategy());
    }

    public void testDoCommit() {
    }

    public void testDoRollback() {

        DirContextHolder expectedContextHolder = new DirContextHolder(
                contextMock, renamingStrategyMock);
        expectedContextHolder
                .setTransactionOperationManager(transactionDataManagerMock);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);

        transactionDataManagerMock.rollback();
        transactionDataManagerControl.replay();
        CompensatingTransactionObject transactionObject = new CompensatingTransactionObject(
                null);
        transactionObject.setHolder(expectedContextHolder);
        tested.doRollback(new DefaultTransactionStatus(transactionObject,
                false, false, false, false, null));
        transactionDataManagerControl.verify();
    }

    public void testDoCleanupAfterCompletion() throws Exception {
        DirContextHolder expectedContextHolder = new DirContextHolder(
                contextMock, renamingStrategyMock);
        TransactionSynchronizationManager.bindResource(contextSourceMock,
                expectedContextHolder);

        contextMock.close();
        contextControl.replay();

        tested.doCleanupAfterCompletion(new CompensatingTransactionObject(
                expectedContextHolder));

        contextControl.verify();
        assertNull(TransactionSynchronizationManager
                .getResource(contextSourceMock));
        assertNull(expectedContextHolder.getTransactionOperationManager());
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
