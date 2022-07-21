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

package org.springframework.ldap.core.support;

import org.springframework.ldap.core.LdapOperations;

/**
 * Callback interface to be used together with {@link SingleContextSource}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 * @see SingleContextSource#doWithSingleContext(org.springframework.ldap.core.ContextSource, LdapOperationsCallback)
 * @see SingleContextSource#doWithSingleContext(org.springframework.ldap.core.ContextSource, LdapOperationsCallback, boolean, boolean, boolean)
 */
public interface LdapOperationsCallback<T> {
	/**
	 * Perform a sequence of LDAP operations on the supplied LdapOperations instance. The underlying DirContext
	 * that the operations will work on is guaranteed to always be exact same instance during the lifetime of this
	 * method.
	 *
	 * @param operations the LdapOperations instance to perform operations on.
	 * @return The aggregated result of all the performed operations.
	 */
	T doWithLdapOperations(LdapOperations operations);
}
