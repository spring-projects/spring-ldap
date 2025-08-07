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

package org.springframework.ldap.transaction.compensating.manager;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * Tests for {@link TransactionAwareContextSourceProxy}.
 *
 * @author Mattias Hellborg Arthursson
 */
public class TransactionAwareContextSourceProxyTests {

	private ContextSource contextSourceMock;

	private TransactionAwareContextSourceProxy tested;

	private LdapContext ldapContextMock;

	private DirContext dirContextMock;

	@Before
	public void setUp() throws Exception {
		this.contextSourceMock = mock(ContextSource.class);
		this.ldapContextMock = mock(LdapContext.class);
		this.dirContextMock = mock(DirContext.class);

		this.tested = new TransactionAwareContextSourceProxy(this.contextSourceMock);
	}

	@Test
	public void testGetReadWriteContext_LdapContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.ldapContextMock);

		DirContext result = this.tested.getReadWriteContext();

		assertThat(result).isNotNull();
		assertThat(result instanceof LdapContext).isTrue();
		assertThat(result instanceof DirContextProxy).isTrue();
	}

	@Test
	public void testGetReadWriteContext_DirContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.dirContextMock);

		DirContext result = this.tested.getReadWriteContext();

		assertThat(result).as("Result should not be null").isNotNull();
		assertThat(result instanceof DirContext).isTrue();
		assertThat(result instanceof LdapContext).isFalse();
		assertThat(result instanceof DirContextProxy).isTrue();
	}

	@Test
	public void testGetReadOnlyContext_LdapContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.ldapContextMock);

		DirContext result = this.tested.getReadOnlyContext();

		assertThat(result).as("Result should not be null").isNotNull();
		assertThat(result instanceof LdapContext).isTrue();
		assertThat(result instanceof DirContextProxy).isTrue();
	}

}
