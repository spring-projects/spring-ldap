/*
 * Copyright 2005-2016 the original author or authors.
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

import javax.naming.directory.DirContext;
import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContextSourceTransactionManagerTest {

	private ContextSource contextSourceMock;

	private DirContext contextMock;

	private ContextSourceTransactionManager tested;

	private CompensatingTransactionOperationManager transactionDataManagerMock;

	private TransactionDefinition transactionDefinitionMock;

	private TempEntryRenamingStrategy renamingStrategyMock;

	@Before
	public void setUp() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}

		contextSourceMock = mock(ContextSource.class);
		contextMock = mock(DirContext.class);
		transactionDefinitionMock = mock(TransactionDefinition.class);
		transactionDataManagerMock = mock(CompensatingTransactionOperationManager.class);
		renamingStrategyMock = mock(TempEntryRenamingStrategy.class);

		tested = new ContextSourceTransactionManager();
		tested.setContextSource(contextSourceMock);
		tested.setRenamingStrategy(renamingStrategyMock);
	}

	@Test
	public void testDoGetTransaction() {
		Object result = tested.doGetTransaction();

		assertThat(result).isNotNull();
		assertThat(result instanceof CompensatingTransactionObject).isTrue();
		CompensatingTransactionObject transactionObject = (CompensatingTransactionObject) result;
		assertThat(transactionObject.getHolder()).isNull();
	}

	@Test
	public void testDoGetTransactionTransactionActive() {
		CompensatingTransactionHolderSupport expectedContextHolder = new DirContextHolder(null, null);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);
		Object result = tested.doGetTransaction();
		assertThat(((CompensatingTransactionObject) result).getHolder()).isSameAs(expectedContextHolder);
	}

	@Test
	public void testDoBegin() {
		when(contextSourceMock.getReadWriteContext()).thenReturn(contextMock);

		CompensatingTransactionObject expectedTransactionObject = new CompensatingTransactionObject(null);
		tested.doBegin(expectedTransactionObject, transactionDefinitionMock);

		DirContextHolder foundContextHolder = (DirContextHolder) TransactionSynchronizationManager
				.getResource(contextSourceMock);
		assertThat(foundContextHolder.getCtx()).isSameAs(contextMock);
	}

	@Test
	public void testDoRollback() {
		DirContextHolder expectedContextHolder = new DirContextHolder(null, contextMock);
		expectedContextHolder.setTransactionOperationManager(transactionDataManagerMock);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);

		CompensatingTransactionObject transactionObject = new CompensatingTransactionObject(null);
		transactionObject.setHolder(expectedContextHolder);
		tested.doRollback(new DefaultTransactionStatus(transactionObject, false, false, false, false, null));

		verify(transactionDataManagerMock).rollback();
	}

	@Test
	public void testDoCleanupAfterCompletion() throws Exception {
		DirContextHolder expectedContextHolder = new DirContextHolder(null, contextMock);
		TransactionSynchronizationManager.bindResource(contextSourceMock, expectedContextHolder);

		tested.doCleanupAfterCompletion(new CompensatingTransactionObject(expectedContextHolder));

		assertThat(TransactionSynchronizationManager.getResource(contextSourceMock)).isNull();
		assertThat(expectedContextHolder.getTransactionOperationManager()).isNull();
		verify(contextMock).close();
	}

	@Test
	public void testSetContextSource_Proxy() {
		TransactionAwareContextSourceProxy proxy = new TransactionAwareContextSourceProxy(contextSourceMock);

		// Perform test
		tested.setContextSource(proxy);
		ContextSource result = tested.getContextSource();

		// Verify result
		assertThat(result).isSameAs(contextSourceMock);
	}

	@Test
	public void testTransactionSuspension_UnconnectableDataSource() throws Exception {
		Connection connectionMock = mock(Connection.class);
		DataSource dataSourceMock = mock(DataSource.class);

		when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		when(connectionMock.getAutoCommit()).thenReturn(false);

		ContextSource unconnectableContextSourceMock = mock(ContextSource.class);

		UncategorizedLdapException connectException = new UncategorizedLdapException("dummy");
		when(unconnectableContextSourceMock.getReadWriteContext()).thenThrow(connectException);

		try {
			// Create an outer transaction
			final PlatformTransactionManager txMgrOuter = new DataSourceTransactionManager(dataSourceMock);

			final TransactionStatus txOuter = txMgrOuter.getTransaction(new DefaultTransactionDefinition());

			try {
				// Create inner transaction (not nested, though: unrelated data
				// source)
				final ContextSourceTransactionManager txMgrInner = new ContextSourceTransactionManager();
				txMgrInner.setContextSource(unconnectableContextSourceMock);

				final TransactionStatus txInner = txMgrInner.getTransaction(
						new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

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
			assertThat(expected.getCause()).as("Should be thrown exception").isSameAs(connectException);
		}

		verify(connectionMock).rollback();
	}

}
