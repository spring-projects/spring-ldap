/*
 * Copyright 2005-2016 the original author or authors.
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

import javax.naming.Name;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.UncategorizedLdapException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

/**
 * Unit tests for the rename operations in the LdapTemplate class.
 *
 * @author Ulrik Sandberg
 */
public class LdapTemplateRenameTests {

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private Name oldNameMock;

	private Name newNameMock;

	private LdapTemplate tested;

	@Before
	public void setUp() throws Exception {
		// Setup ContextSource mock
		this.contextSourceMock = mock(ContextSource.class);

		// Setup LdapContext mock
		this.dirContextMock = mock(LdapContext.class);

		// Setup Name mock for old name
		this.oldNameMock = mock(Name.class);

		// Setup Name mock for new name
		this.newNameMock = mock(Name.class);

		this.tested = new LdapTemplate(this.contextSourceMock);
	}

	private void expectGetReadWriteContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.dirContextMock);
	}

	@Test
	public void testRename() throws Exception {
		expectGetReadWriteContext();

		this.tested.rename(this.oldNameMock, this.newNameMock);

		verify(this.dirContextMock).rename(this.oldNameMock, this.newNameMock);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testRename_NameAlreadyBoundException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NameAlreadyBoundException ne = new javax.naming.NameAlreadyBoundException();
		willThrow(ne).given(this.dirContextMock).rename(this.oldNameMock, this.newNameMock);

		try {
			this.tested.rename(this.oldNameMock, this.newNameMock);
			fail("NameAlreadyBoundException expected");
		}
		catch (NameAlreadyBoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testRename_NamingException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NamingException ne = new javax.naming.NamingException();

		willThrow(ne).given(this.dirContextMock).rename(this.oldNameMock, this.newNameMock);

		try {
			this.tested.rename(this.oldNameMock, this.newNameMock);
			fail("UncategorizedLdapException expected");
		}
		catch (UncategorizedLdapException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testRename_String() throws Exception {
		expectGetReadWriteContext();

		this.tested.rename("o=example.com", "o=somethingelse.com");

		verify(this.dirContextMock).rename("o=example.com", "o=somethingelse.com");
		verify(this.dirContextMock).close();
	}

}
