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

package org.springframework.ldap.core;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Interface to be called in search by {@link LdapTemplate} before and after the actual
 * search and enumeration traversal. Implementations may be used to apply search controls
 * on the <code>Context</code> and retrieve the results of such controls afterwards.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public interface DirContextProcessor {

	/**
	 * Perform pre-processing on the supplied DirContext.
	 * @param ctx the <code>DirContext</code> instance.
	 * @throws NamingException if thrown by the underlying operation.
	 */
	void preProcess(DirContext ctx) throws NamingException;

	/**
	 * Perform post-processing on the supplied <code>DirContext</code>.
	 * @param ctx the <code>DirContext</code> instance.
	 * @throws NamingException if thrown by the underlying operation.
	 */
	void postProcess(DirContext ctx) throws NamingException;

}
