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

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.PartialResultException;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the <code>list</code> operations in {@link LdapTemplate}.
 *
 * @author Ulrik Sandberg
 */
public class LdapTemplateListTests {

	private static final String NAME = "o=example.com";

	private static final String CLASS = "com.example.SomeClass";

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private NamingEnumeration namingEnumerationMock;

	private Name nameMock;

	private NameClassPairCallbackHandler handlerMock;

	private ContextMapper contextMapperMock;

	private LdapTemplate tested;

	@Before
	public void setUp() throws Exception {
		// Setup ContextSource mock
		this.contextSourceMock = mock(ContextSource.class);

		// Setup LdapContext mock
		this.dirContextMock = mock(LdapContext.class);

		// Setup NamingEnumeration mock
		this.namingEnumerationMock = mock(NamingEnumeration.class);

		// Setup Name mock
		this.nameMock = mock(Name.class);

		// Setup Handler mock
		this.handlerMock = mock(NameClassPairCallbackHandler.class);

		this.contextMapperMock = mock(ContextMapper.class);

		this.tested = new LdapTemplate(this.contextSourceMock);
	}

	private void expectGetReadOnlyContext() {
		when(this.contextSourceMock.getReadOnlyContext()).thenReturn(this.dirContextMock);
	}

	private void setupStringListAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		when(this.dirContextMock.list(NAME)).thenReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupListAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		when(this.dirContextMock.list(this.nameMock)).thenReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupStringListBindingsAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		when(this.dirContextMock.listBindings(NAME)).thenReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupListBindingsAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		when(this.dirContextMock.listBindings(this.nameMock)).thenReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupNamingEnumeration(NameClassPair listResult) throws NamingException {
		when(this.namingEnumerationMock.hasMore()).thenReturn(true, false);
		when(this.namingEnumerationMock.next()).thenReturn(listResult);
	}

	@Test
	public void testList_Name() throws NamingException {
		expectGetReadOnlyContext();

		NameClassPair listResult = new NameClassPair(NAME, CLASS);

		setupListAndNamingEnumeration(listResult);

		List list = this.tested.list(this.nameMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testList_String() throws NamingException {
		expectGetReadOnlyContext();

		NameClassPair listResult = new NameClassPair(NAME, CLASS);

		setupStringListAndNamingEnumeration(listResult);

		List list = this.tested.list(NAME);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testList_Name_CallbackHandler() throws NamingException {
		expectGetReadOnlyContext();

		NameClassPair listResult = new NameClassPair(NAME, CLASS);

		setupListAndNamingEnumeration(listResult);

		this.tested.list(this.nameMock, this.handlerMock);

		verify(this.handlerMock).handleNameClassPair(listResult);
		verify(this.namingEnumerationMock).close();
		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_String_CallbackHandler() throws NamingException {
		expectGetReadOnlyContext();

		NameClassPair listResult = new NameClassPair(NAME, CLASS);

		setupStringListAndNamingEnumeration(listResult);

		this.tested.list("o=example.com", this.handlerMock);

		verify(this.handlerMock).handleNameClassPair(listResult);
		verify(this.namingEnumerationMock).close();
		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_PartialResultException() throws NamingException {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		when(this.dirContextMock.list(NAME)).thenThrow(pre);

		try {
			this.tested.list(NAME);
			fail("PartialResultException expected");
		}
		catch (PartialResultException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_PartialResultException_Ignore() throws NamingException {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		when(this.dirContextMock.list(NAME)).thenThrow(pre);

		this.tested.setIgnorePartialResultException(true);

		List list = this.tested.list(NAME);

		verify(this.dirContextMock).close();

		assertThat(list).isNotNull();
		assertThat(list).isEmpty();
	}

	@Test
	public void testList_NamingException() throws NamingException {
		expectGetReadOnlyContext();

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		when(this.dirContextMock.list(NAME)).thenThrow(ne);

		try {
			this.tested.list(NAME);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	// Tests for listBindings

	@Test
	public void testListBindings_String() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupStringListBindingsAndNamingEnumeration(listResult);

		List list = this.tested.listBindings(NAME);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testListBindings_Name() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupListBindingsAndNamingEnumeration(listResult);

		List list = this.tested.listBindings(this.nameMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testListBindings_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupStringListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		when(this.contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = this.tested.listBindings(NAME, this.contextMapperMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(expectedResult);
	}

	@Test
	public void testListBindings_Name_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		when(this.contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = this.tested.listBindings(this.nameMock, this.contextMapperMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(expectedResult);
	}

}
