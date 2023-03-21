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
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the rename operations in the LdapTemplate class.
 *
 * @author Josh Cummings
 */
public class DefaultLdapClientRenameTest {

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private final Name oldName = LdapUtils.newLdapName("ou=old");

	private final Name newName = LdapUtils.newLdapName("ou=new");

	private LdapClient tested;

	@Before
	public void setUp() throws Exception {
		// Setup ContextSource mock
		contextSourceMock = mock(ContextSource.class);

		// Setup LdapContext mock
		dirContextMock = mock(LdapContext.class);

		tested = LdapClient.create(contextSourceMock);
	}

	private void expectGetReadWriteContext() {
		when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
	}

	@Test
	public void testRename() throws Exception {
		expectGetReadWriteContext();

		tested.modify(oldName).name(newName).execute();

		verify(dirContextMock).rename(oldName, newName);
		verify(dirContextMock).close();
	}

	@Test
	public void testRename_NameAlreadyBoundException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NameAlreadyBoundException ne = new javax.naming.NameAlreadyBoundException();
		doThrow(ne).when(dirContextMock).rename(oldName, newName);

		try {
			tested.modify(oldName).name(newName).execute();
			fail("NameAlreadyBoundException expected");
		}
		catch (NameAlreadyBoundException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testRename_NamingException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NamingException ne = new javax.naming.NamingException();

		doThrow(ne).when(dirContextMock).rename(oldName, newName);

		try {
			tested.modify(oldName).name(newName).execute();
			fail("UncategorizedLdapException expected");
		}
		catch (UncategorizedLdapException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testRename_String() throws Exception {
		expectGetReadWriteContext();

		tested.modify("o=example.com").name("o=somethingelse.com").execute();

		verify(dirContextMock).rename(LdapUtils.newLdapName("o=example.com"),
				LdapUtils.newLdapName("o=somethingelse.com"));
		verify(dirContextMock).close();
	}

}
