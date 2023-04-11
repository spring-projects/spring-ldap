/*
 * Copyright 2002-2007 the original author or authors.
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

import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.core.support.DelegatingBaseLdapPathContextSourceSupport;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A proxy for ContextSource to make sure that the returned DirContext objects are aware
 * of the surrounding transactions. This makes sure that the DirContext is not closed
 * during the transaction and that all modifying operations are recorded, keeping track of
 * the corresponding rollback operations. All returned DirContext instances will be of the
 * type {@link TransactionAwareDirContextInvocationHandler}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class TransactionAwareContextSourceProxy extends DelegatingBaseLdapPathContextSourceSupport
		implements ContextSource {

	private ContextSource target;

	/**
	 * Constructor.
	 * @param target the target ContextSource.
	 */
	public TransactionAwareContextSourceProxy(ContextSource target) {
		this.target = target;
	}

	@Override
	public ContextSource getTarget() {
		return this.target;
	}

	@Override
	public DirContext getReadOnlyContext() {
		return getReadWriteContext();
	}

	private DirContext getTransactionAwareDirContextProxy(DirContext context, ContextSource target) {
		return (DirContext) Proxy.newProxyInstance(DirContextProxy.class.getClassLoader(),
				new Class[] { LdapUtils.getActualTargetClass(context), DirContextProxy.class },
				new TransactionAwareDirContextInvocationHandler(context, target));

	}

	@Override
	public DirContext getReadWriteContext() {
		DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager.getResource(this.target);
		DirContext ctx = null;

		if (contextHolder != null) {
			ctx = contextHolder.getCtx();
		}

		if (ctx == null) {
			ctx = this.target.getReadWriteContext();
			if (contextHolder != null) {
				contextHolder.setCtx(ctx);
			}
		}
		return getTransactionAwareDirContextProxy(ctx, this.target);
	}

	@Override
	public DirContext getContext(String principal, String credentials) {
		return this.target.getContext(principal, credentials);
	}

}
