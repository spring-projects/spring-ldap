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
package org.springframework.transaction.compensating.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Abstract superclass for Compensating TransactionManager delegates. The actual
 * transaction work is extracted to a delegate to enable composite Transaction Managers.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public abstract class AbstractCompensatingTransactionManagerDelegate {

	private static Logger log = LoggerFactory.getLogger(AbstractCompensatingTransactionManagerDelegate.class);

	/**
	 * Close the target resource - the implementation specific resource held in the
	 * specified {@link CompensatingTransactionHolderSupport}.
	 * @param transactionHolderSupport the {@link CompensatingTransactionHolderSupport}
	 * that holds the transaction specific target resource.
	 */
	protected abstract void closeTargetResource(CompensatingTransactionHolderSupport transactionHolderSupport);

	/**
	 * Get a new implementation specific {@link CompensatingTransactionHolderSupport}
	 * instance.
	 * @return a new {@link CompensatingTransactionHolderSupport} instance.
	 */
	protected abstract CompensatingTransactionHolderSupport getNewHolder();

	/**
	 * Get the key (normally, a DataSource or similar) that should be used for transaction
	 * synchronization.
	 * @return the transaction synchronization key
	 */
	protected abstract Object getTransactionSynchronizationKey();

	/*
	 * @seeorg.springframework.jdbc.datasource.DataSourceTransactionManager#
	 * doGetTransaction()
	 */
	public Object doGetTransaction() throws TransactionException {
		CompensatingTransactionHolderSupport holder = (CompensatingTransactionHolderSupport) TransactionSynchronizationManager
				.getResource(getTransactionSynchronizationKey());
		return new CompensatingTransactionObject(holder);
	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin
	 * (java.lang.Object, org.springframework.transaction.TransactionDefinition)
	 */
	public void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		try {
			CompensatingTransactionObject txObject = (CompensatingTransactionObject) transaction;
			if (txObject.getHolder() == null) {
				CompensatingTransactionHolderSupport contextHolder = getNewHolder();
				txObject.setHolder(contextHolder);

				TransactionSynchronizationManager.bindResource(getTransactionSynchronizationKey(), contextHolder);
			}
		}
		catch (Exception e) {
			throw new CannotCreateTransactionException("Could not create DirContext instance for transaction", e);
		}
	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doCommit
	 * (org.springframework.transaction.support.DefaultTransactionStatus)
	 */
	public void doCommit(DefaultTransactionStatus status) throws TransactionException {
		CompensatingTransactionObject txObject = (CompensatingTransactionObject) status.getTransaction();
		txObject.getHolder().getTransactionOperationManager().commit();

	}

	/*
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doRollback
	 * (org.springframework.transaction.support.DefaultTransactionStatus)
	 */
	public void doRollback(DefaultTransactionStatus status) throws TransactionException {
		CompensatingTransactionObject txObject = (CompensatingTransactionObject) status.getTransaction();
		txObject.getHolder().getTransactionOperationManager().rollback();
	}

	/*
	 * @seeorg.springframework.jdbc.datasource.DataSourceTransactionManager#
	 * doCleanupAfterCompletion(java.lang.Object)
	 */
	public void doCleanupAfterCompletion(Object transaction) {
		log.debug("Cleaning stored transaction synchronization");
		TransactionSynchronizationManager.unbindResource(getTransactionSynchronizationKey());

		CompensatingTransactionObject txObject = (CompensatingTransactionObject) transaction;
		CompensatingTransactionHolderSupport transactionHolderSupport = txObject.getHolder();

		closeTargetResource(transactionHolderSupport);

		txObject.getHolder().clear();
	}

}
