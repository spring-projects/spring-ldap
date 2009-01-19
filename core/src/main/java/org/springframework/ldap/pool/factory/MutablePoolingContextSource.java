/*
 * Copyright 2005-2009 the original author or authors.
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

package org.springframework.ldap.pool.factory;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.pool.DelegatingDirContext;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.MutableDelegatingLdapContext;

/**
 * A {@link PoolingContextSource} subclass that creates
 * {@link MutableDelegatingLdapContext} instances. This enables the Spring LDAP
 * pooling to be used in scenarios that require request controls to be set, such
 * as paged results.
 */
public class MutablePoolingContextSource extends PoolingContextSource {
	protected DirContext getContext(DirContextType dirContextType) {
		final DirContext dirContext;
		try {
			dirContext = (DirContext) this.keyedObjectPool.borrowObject(dirContextType);
		}
		catch (Exception e) {
			throw new DataAccessResourceFailureException("Failed to borrow DirContext from pool.", e);
		}

		if (dirContext instanceof LdapContext) {
			return new MutableDelegatingLdapContext(this.keyedObjectPool, (LdapContext) dirContext, dirContextType);
		}

		return new DelegatingDirContext(this.keyedObjectPool, dirContext, dirContextType);
	}
}
