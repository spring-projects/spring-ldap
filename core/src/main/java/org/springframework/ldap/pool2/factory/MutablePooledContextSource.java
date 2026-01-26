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

package org.springframework.ldap.pool2.factory;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.DelegatingDirContext;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.MutableDelegatingLdapContext;

/**
 * A {@link PooledContextSource} subclass that creates
 * {@link MutableDelegatingLdapContext} instances. This enables the Spring LDAP pooling to
 * be used in scenarios that require request controls to be set, such as paged results.
 *
 * @author Anindya Chatterjee
 * @since 2.0
 */
public class MutablePooledContextSource extends PooledContextSource {

	/**
	 * Creates a new pooling context source, setting up the DirContext object factory and
	 * generic keyed object pool.
	 * @param poolConfig pool configurations to set.
	 * @deprecated Please supply the underlying {@link ContextSource} in the constructor
	 */
	@Deprecated
	public MutablePooledContextSource(@Nullable PoolConfig poolConfig) {
		super(poolConfig);
	}

	/**
	 * Creates a new pooling context source, setting up the DirContext object factory and
	 * generic keyed object pool.
	 * @param contextSource the underlying {@link ContextSource}
	 * @param poolConfig pool configurations to set.
	 * @since 4.1
	 */
	public MutablePooledContextSource(ContextSource contextSource, @Nullable PoolConfig poolConfig) {
		super(contextSource, poolConfig);
	}

	protected DirContext getContext(DirContextType dirContextType) {
		final DirContext dirContext;
		try {
			dirContext = (DirContext) this.keyedObjectPool.borrowObject(dirContextType);
		}
		catch (Exception ex) {
			throw new DataAccessResourceFailureException("Failed to borrow DirContext from pool.", ex);
		}

		if (dirContext instanceof LdapContext) {
			return new MutableDelegatingLdapContext(this.keyedObjectPool, (LdapContext) dirContext, dirContextType);
		}

		return new DelegatingDirContext(this.keyedObjectPool, dirContext, dirContextType);
	}

}
