/*
 * Copyright 2005-2015 the original author or authors.
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

package org.springframework.ldap.pool2;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool2.KeyedObjectPool;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.validation.DirContextValidator;

import static org.mockito.Mockito.mock;

/**
 * Contains mocks common to many tests for the connection pool.
 *
 * @author Ulrik Sandberg
 */
public abstract class AbstractPoolTestCase {

	protected Context contextMock;

	protected DirContext dirContextMock;

	protected LdapContext ldapContextMock;

	protected KeyedObjectPool keyedObjectPoolMock;

	protected ContextSource contextSourceMock;

	protected DirContextValidator dirContextValidatorMock;

	@BeforeEach
	public void setUp() throws Exception {
		this.contextMock = mock(Context.class);
		this.dirContextMock = mock(DirContext.class);
		this.ldapContextMock = mock(LdapContext.class);
		this.keyedObjectPoolMock = mock(KeyedObjectPool.class);
		this.contextSourceMock = mock(ContextSource.class);
		this.dirContextValidatorMock = mock(DirContextValidator.class);
	}

}
