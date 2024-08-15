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

package org.springframework.ldap.transaction.compensating.manager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.directory.DirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.LdapTransactionUtils;
import org.springframework.transaction.compensating.support.CompensatingTransactionUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Proxy implementation for DirContext, making sure that the instance is not closed during
 * a transaction, and that all modifying operations are recorded, storing compensating
 * rollback operations for them.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class TransactionAwareDirContextInvocationHandler implements InvocationHandler {

	private static Logger log = LoggerFactory.getLogger(TransactionAwareDirContextInvocationHandler.class);

	private DirContext target;

	private ContextSource contextSource;

	/**
	 * Constructor.
	 * @param target The target DirContext.
	 * @param contextSource The transactional ContextSource, needed to get hold of the
	 * current transaction's {@link DirContextHolder}.
	 */
	public TransactionAwareDirContextInvocationHandler(DirContext target, ContextSource contextSource) {
		this.target = target;
		this.contextSource = contextSource;
	}

	/*
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		String methodName = method.getName();
		if (methodName.equals("getTargetContext")) {
			return this.target;
		}
		else if (methodName.equals("equals")) {
			// Only consider equal when proxies are identical.
			return (proxy != args[0]) ? Boolean.FALSE : Boolean.TRUE;
		}
		else if (methodName.equals("hashCode")) {
			// Use hashCode of Connection proxy.
			return hashCode();
		}
		else if (methodName.equals("close")) {
			doCloseConnection(this.target, this.contextSource);
			return null;
		}
		else if (LdapTransactionUtils.isSupportedWriteTransactionOperation(methodName)) {
			// Store transaction data and allow operation to proceed.
			CompensatingTransactionUtils.performOperation(this.contextSource, this.target, method, args);
			return null;
		}
		else {
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

	/**
	 * Close the supplied context, but only if it is not associated with the current
	 * transaction.
	 * @param context the DirContext to close.
	 * @param contextSource the ContextSource bound to the transaction.
	 * @throws NamingException
	 */
	void doCloseConnection(DirContext context, ContextSource contextSource) throws javax.naming.NamingException {
		DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
			.getResource(contextSource);
		if (transactionContextHolder == null || transactionContextHolder.getCtx() != context) {
			log.debug("Closing context");
			// This is not the transactional context or the transaction is
			// no longer active - we should close it.
			context.close();
		}
		else {
			log.debug("Leaving transactional context open");
		}
	}

}
