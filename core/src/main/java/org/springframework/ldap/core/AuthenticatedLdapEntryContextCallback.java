/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.core;

import javax.naming.directory.DirContext;

/**
 * Callback interface to be used in the authentication methods in
 * {@link LdapOperations} for performing operations on individually
 * authenticated contexts.
 * 
 * @author Mattias Hellborg Arthursson
 * @since 1.3
 */
public interface AuthenticatedLdapEntryContextCallback {
	/**
	 * Perform some LDAP operation on the supplied authenticated
	 * <code>DirContext</code> instance. The target context will be
	 * automatically closed.
	 * 
	 * @param ctx the <code>DirContext</code> instance to perform an operation
	 * on.
	 * @param ldapEntryIdentification the identification of the LDAP entry used
	 * to authenticate the supplied <code>DirContext</code>.
	 */
	void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification);
}
