/*
 * Copyright 2005-2010 the original author or authors.
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

import java.sql.Connection;

import javax.naming.directory.DirContext;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.compensating.support.CompensatingTransactionObject;
import org.springframework.transaction.support.DefaultTransactionDefinition;
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

		transactionDefinitionControl = MockControl.createControl(TransactionDefinition.class);
		transactionDefinitionMock = (TransactionDefinition) transactionDefinitionControl.getMock();

		transactionDataManagerControl = MockControl.createControl(CompensatingTransactionOperationManager.class);
		transactionDataManagerMock = (CompensatingTransactionOperationManager) transactionDataManagerControl.getMock();

		renamingStrategyControl = MockControl.createControl(TempEntryRenamingStrategy.class);
		renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl.getMock();

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
		CompensatingTransactionHolderSupport expectedContextHolder = new DirContextHolder(null, null);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);
		Object result = tested.doGetTransaction();
		assertSame(expectedContextHolder, ((CompensatingTransactionObject) result).getHolder());
	}

	public void testDoBegin() {
		contextSourceControl.expectAndReturn(contextSourceMock.getReadWriteContext(), contextMock);

		contextSourceControl.replay();

		CompensatingTransactionObject expectedTransactionObject = new CompensatingTransactionObject(null);
		tested.doBegin(expectedTransactionObject, transactionDefinitionMock);

		contextSourceControl.verify();

		DirContextHolder foundContextHolder = (DirContextHolder) TransactionSynchronizationManager
				.getResource(contextSourceMock);
		assertSame(contextMock, foundContextHolder.getCtx());
	}

	public void testDoCommit() {
	}

	public void testDoRollback() {

		DirContextHolder expectedContextHolder = new DirContextHolder(null, contextMock);
		expectedContextHolder.setTransactionOperationManager(transactionDataManagerMock);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);

		transactionDataManagerMock.rollback();
		transactionDataManagerControl.replay();
		CompensatingTransactionObject transactionObject = new CompensatingTransactionObject(null);
		transactionObject.setHolder(expectedContextHolder);
		tested.doRollback(new DefaultTransactionStatus(transactionObject, false, false, false, false, null));
		transactionDataManagerControl.verify();
	}

	public void testDoCleanupAfterCompletion() throws Exception {
		DirContextHolder expectedContextHolder = new DirContextHolder(null, contextMock);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);

		contextMock.close();
		contextControl.replay();

		tested.doCleanupAfterCompletion(new CompensatingTransactionObject(expectedContextHolder));

		contextControl.verify();
		assertNull(TransactionSynchronizationManager.getResource(contextSourceMock));
		assertNull(expectedContextHolder.getTransactionOperationManager());
	}

	public void testSetContextSource_Proxy() {
		TransactionAwareContextSourceProxy proxy = new TransactionAwareContextSourceProxy(contextSourceMock);

		// Perform test
		tested.setContextSource(proxy);
		ContextSource result = tested.getContextSource();

		// Verify result
		assertSame(contextSourceMock, result);
	}

	public void testTransactionSuspension_UnconnectableDataSource() throws Exception {
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connectionMock = (Connection) connectionControl.getMock();
		MockControl dataSourceControl=MockControl.createControl(DataSource.class);
		DataSource dataSourceMock = (DataSource) dataSourceControl.getMock();

		dataSourceControl.expectAndReturn(dataSourceMock.getConnection(), connectionMock);
		connectionControl.expectAndReturn(connectionMock.getAutoCommit(), false);
		connectionMock.rollback();

		MockControl unconnectableContextSourceControl = MockControl.createControl(ContextSource.class);
		ContextSource unconnectableContextSourceMock = (ContextSource) unconnectableContextSourceControl.getMock();

		UncategorizedLdapException connectException = new UncategorizedLdapException("dummy");
		unconnectableContextSourceControl.expectAndThrow(unconnectableContextSourceMock.getReadWriteContext(), connectException);
		
		connectionControl.replay();
		dataSourceControl.replay();
		unconnectableContextSourceControl.replay();
		
		try {
			// Create an outer transaction
			final PlatformTransactionManager txMgrOuter = new DataSourceTransactionManager(dataSourceMock);

			final TransactionStatus txOuter = txMgrOuter.getTransaction(new DefaultTransactionDefinition());

			try {
				// Create inner transaction (not nested, though: unrelated data
				// source)
				final ContextSourceTransactionManager txMgrInner = new ContextSourceTransactionManager();
				txMgrInner.setContextSource(unconnectableContextSourceMock);

				final TransactionStatus txInner = txMgrInner.getTransaction(new DefaultTransactionDefinition(
						TransactionDefinition.PROPAGATION_REQUIRES_NEW));

				try {
					// Do something with the connection that succeeds or fails
					// (but we dont get this far)
					// etc, etc...

					txMgrInner.commit(txInner);
				}
				catch (Exception e) {
					txMgrInner.rollback(txInner);
					throw e;
				}

				txMgrOuter.commit(txOuter);
			}
			catch (Exception e) {
				txMgrOuter.rollback(txOuter);
				throw e;
			}

			fail("Exception should be thrown");
		}
		catch (CannotCreateTransactionException expected) {
			assertSame("Should be thrown exception", connectException, expected.getCause());
		}

		connectionControl.verify();
		dataSourceControl.verify();
		unconnectableContextSourceControl.verify();

	}
}
