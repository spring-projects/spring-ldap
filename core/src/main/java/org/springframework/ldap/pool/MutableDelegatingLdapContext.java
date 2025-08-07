/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.pool;

import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool.KeyedObjectPool;

import org.springframework.ldap.pool.factory.MutablePoolingContextSource;

/**
 * Used by {@link MutablePoolingContextSource} to wrap a {@link LdapContext}, delegating
 * most methods to the underlying context. This class extends
 * {@link DelegatingLdapContext}, allowing request controls to be set on the wrapped ldap
 * context. This enables the Spring LDAP pooling to be used for scenarios such as paged
 * results.
 *
 * @author Ulrik Sandberg
 */
public class MutableDelegatingLdapContext extends DelegatingLdapContext {

	/**
	 * Create a new mutable delegating ldap context for the specified pool, context and
	 * context type.
	 * @param keyedObjectPool The pool the delegate context was checked out from.
	 * @param delegateLdapContext The ldap context to delegate operations to.
	 * @param dirContextType The type of context, used as a key for the pool.
	 * @throws IllegalArgumentException if any of the arguments are null
	 */
	public MutableDelegatingLdapContext(KeyedObjectPool keyedObjectPool, LdapContext delegateLdapContext,
			DirContextType dirContextType) {
		super(keyedObjectPool, delegateLdapContext, dirContextType);
	}

	public void setRequestControls(Control[] requestControls) throws NamingException {
		assertOpen();
		getDelegateLdapContext().setRequestControls(requestControls);
	}

}
