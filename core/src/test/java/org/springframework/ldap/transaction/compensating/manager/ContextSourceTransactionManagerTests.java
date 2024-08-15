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

import java.sql.Connection;

import javax.naming.directory.DirContext;
import javax.sql.DataSource;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

public class ContextSourceTransactionManagerTests {

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

		this.contextSourceMock = mock(ContextSource.class);
		this.contextMock = mock(DirContext.class);
		this.transactionDefinitionMock = mock(TransactionDefinition.class);
		this.transactionDataManagerMock = mock(CompensatingTransactionOperationManager.class);
		this.renamingStrategyMock = mock(TempEntryRenamingStrategy.class);

		this.tested = new ContextSourceTransactionManager();
		this.tested.setContextSource(this.contextSourceMock);
		this.tested.setRenamingStrategy(this.renamingStrategyMock);
	}

	@Test
	public void testDoGetTransaction() {
		Object result = this.tested.doGetTransaction();

		assertThat(result).isNotNull();
		assertThat(result instanceof CompensatingTransactionObject).isTrue();
		CompensatingTransactionObject transactionObject = (CompensatingTransactionObject) result;
		assertThat(transactionObject.getHolder()).isNull();
	}

	@Test
	public void testDoGetTransactionTransactionActive() {
		CompensatingTransactionHolderSupport expectedContextHolder = new DirContextHolder(null, null);
		TransactionSynchronizationManager.bindResource(this.contextSourceMock, expectedContextHolder);
		Object result = this.tested.doGetTransaction();
		assertThat(((CompensatingTransactionObject) result).getHolder()).isSameAs(expectedContextHolder);
	}

	@Test
	public void testDoBegin() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.contextMock);

		CompensatingTransactionObject expectedTransactionObject = new CompensatingTransactionObject(null);
		this.tested.doBegin(expectedTransactionObject, this.transactionDefinitionMock);

		DirContextHolder foundContextHolder = (DirContextHolder) TransactionSynchronizationManager
			.getResource(this.contextSourceMock);
		assertThat(foundContextHolder.getCtx()).isSameAs(this.contextMock);
	}

	@Test
	public void testDoRollback() {
		DirContextHolder expectedContextHolder = new DirContextHolder(null, this.contextMock);
		expectedContextHolder.setTransactionOperationManager(this.transactionDataManagerMock);
		TransactionSynchronizationManager.bindResource(this.contextSourceMock, expectedContextHolder);

		CompensatingTransactionObject transactionObject = new CompensatingTransactionObject(null);
		transactionObject.setHolder(expectedContextHolder);
		this.tested.doRollback(new DefaultTransactionStatus(transactionObject, false, false, false, false, null));

		verify(this.transactionDataManagerMock).rollback();
	}

	@Test
	public void testDoCleanupAfterCompletion() throws Exception {
		DirContextHolder expectedContextHolder = new DirContextHolder(null, this.contextMock);
		TransactionSynchronizationManager.bindResource(this.contextSourceMock, expectedContextHolder);

		this.tested.doCleanupAfterCompletion(new CompensatingTransactionObject(expectedContextHolder));

		assertThat(TransactionSynchronizationManager.getResource(this.contextSourceMock)).isNull();
		assertThat(expectedContextHolder.getTransactionOperationManager()).isNull();
		verify(this.contextMock).close();
	}

	@Test
	public void testSetContextSource_Proxy() {
		TransactionAwareContextSourceProxy proxy = new TransactionAwareContextSourceProxy(this.contextSourceMock);

		// Perform test
		this.tested.setContextSource(proxy);
		ContextSource result = this.tested.getContextSource();

		// Verify result
		assertThat(result).isSameAs(this.contextSourceMock);
	}

	@Test
	public void testTransactionSuspension_UnconnectableDataSource() throws Exception {
		Connection connectionMock = mock(Connection.class);
		DataSource dataSourceMock = mock(DataSource.class);

		given(dataSourceMock.getConnection()).willReturn(connectionMock);
		given(connectionMock.getAutoCommit()).willReturn(false);

		ContextSource unconnectableContextSourceMock = mock(ContextSource.class);

		UncategorizedLdapException connectException = new UncategorizedLdapException("dummy");
		given(unconnectableContextSourceMock.getReadWriteContext()).willThrow(connectException);

		try {
			// Create an outer transaction
			final PlatformTransactionManager txMgrOuter = new DataSourceTransactionManager(dataSourceMock);

			final TransactionStatus txOuter = txMgrOuter.getTransaction(new DefaultTransactionDefinition());

			try {
				// Create inner transaction (not nested, though: unrelated data
				// source)
				final ContextSourceTransactionManager txMgrInner = new ContextSourceTransactionManager();
				txMgrInner.setContextSource(unconnectableContextSourceMock);

				final TransactionStatus txInner = txMgrInner
					.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

				try {
					// Do something with the connection that succeeds or fails
					// (but we dont get this far)
					// etc, etc...

					txMgrInner.commit(txInner);
				}
				catch (Exception ex) {
					txMgrInner.rollback(txInner);
					throw ex;
				}

				txMgrOuter.commit(txOuter);
			}
			catch (Exception ex) {
				txMgrOuter.rollback(txOuter);
				throw ex;
			}

			fail("Exception should be thrown");
		}
		catch (CannotCreateTransactionException expected) {
			assertThat(expected.getCause()).as("Should be thrown exception").isSameAs(connectException);
		}

		verify(connectionMock).rollback();
	}

}
