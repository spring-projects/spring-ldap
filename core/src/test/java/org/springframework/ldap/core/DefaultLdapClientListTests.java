/*
 * Copyright 2005-2022 the original author or authors.
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.PartialResultException;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

/**
 * Unit tests for the <code>list</code> operations in {@link LdapTemplate}.
 *
 * @author Ulrik Sandberg
 */
public class DefaultLdapClientListTests {

	private static final String NAME = "o=example.com";

	private static final String CLASS = "com.example.SomeClass";

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private NamingEnumeration namingEnumerationMock;

	private Name nameMock = LdapUtils.newLdapName(NAME);

	private ContextMapper<Object> contextMapperMock;

	private DefaultLdapClient tested;

	@Before
	public void setUp() throws Exception {
		// Setup ContextSource mock
		this.contextSourceMock = mock(ContextSource.class);

		// Setup LdapContext mock
		this.dirContextMock = mock(LdapContext.class);

		// Setup NamingEnumeration mock
		this.namingEnumerationMock = mock(NamingEnumeration.class);

		this.contextMapperMock = mock(ContextMapper.class);

		this.tested = (DefaultLdapClient) LdapClient.create(this.contextSourceMock);
	}

	private void expectGetReadOnlyContext() {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);
	}

	private void setupListAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		given(this.dirContextMock.list(this.nameMock)).willReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupListBindingsAndNamingEnumeration(NameClassPair listResult) throws NamingException {
		given(this.dirContextMock.listBindings(this.nameMock)).willReturn(this.namingEnumerationMock);

		setupNamingEnumeration(listResult);
	}

	private void setupNamingEnumeration(NameClassPair listResult) throws NamingException {
		given(this.namingEnumerationMock.hasMore()).willReturn(true, false);
		given(this.namingEnumerationMock.next()).willReturn(listResult);
	}

	@Test
	public void testList_Name() throws NamingException {
		expectGetReadOnlyContext();

		NameClassPair listResult = new NameClassPair(NAME, CLASS);

		setupListAndNamingEnumeration(listResult);

		List<String> list = this.tested.list(this.nameMock).toList(NameClassPair::getName);

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

		setupListAndNamingEnumeration(listResult);

		List<String> list = this.tested.list(NAME).toList(NameClassPair::getName);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testList_PartialResultException() throws NamingException {
		expectGetReadOnlyContext();
		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(pre);

		assertThatExceptionOfType(PartialResultException.class)
			.isThrownBy(() -> this.tested.list(NAME).toList(NameClassPair::getName));

		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_Stream_PartialResultException() throws NamingException {
		expectGetReadOnlyContext();
		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(pre);

		assertThatExceptionOfType(PartialResultException.class)
			.isThrownBy(() -> this.tested.list(NAME).toStream(NameClassPair::getName).collect(Collectors.toList()));

		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_PartialResultException_Ignore() throws NamingException {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(pre);

		this.tested.setIgnorePartialResultException(true);

		List<String> list = this.tested.list(NAME).toList(NameClassPair::getName);

		verify(this.dirContextMock).close();

		assertThat(list).isNotNull();
		assertThat(list).isEmpty();
	}

	@Test
	public void testList_AsStream_PartialResultException_Ignore() throws NamingException {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException pre = new javax.naming.PartialResultException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(pre);

		this.tested.setIgnorePartialResultException(true);

		try (Stream<String> results = this.tested.list(NAME).toStream(NameClassPair::getName)) {
			List<String> list = results.collect(Collectors.toList());
			assertThat(list).isNotNull();
			assertThat(list).isEmpty();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_NamingException() throws NamingException {
		expectGetReadOnlyContext();
		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(ne);
		assertThatExceptionOfType(LimitExceededException.class)
			.isThrownBy(() -> this.tested.list(NAME).toList(NameClassPair::getName));
		verify(this.dirContextMock).close();
	}

	@Test
	public void testList_AsStream_NamingException() throws NamingException {
		expectGetReadOnlyContext();
		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		given(this.dirContextMock.list(this.nameMock)).willThrow(ne);
		assertThatExceptionOfType(LimitExceededException.class)
			.isThrownBy(() -> this.tested.list(NAME).toStream(NameClassPair::getName).collect(Collectors.toList()));
		verify(this.dirContextMock).close();
	}

	// Tests for listBindings

	@Test
	public void testListBindings_String() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupListBindingsAndNamingEnumeration(listResult);

		List<String> list = this.tested.listBindings(NAME).toList(NameClassPair::getName);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testListBindings_AsStream_String() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupListBindingsAndNamingEnumeration(listResult);

		try (Stream<String> results = this.tested.listBindings(NAME).toStream(NameClassPair::getName)) {
			List<String> list = results.collect(Collectors.toList());
			assertThat(list).isNotNull();
			assertThat(list).hasSize(1);
			assertThat(list.get(0)).isSameAs(NAME);
		}

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();
	}

	@Test
	public void testListBindings_Name() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupListBindingsAndNamingEnumeration(listResult);

		List<String> list = this.tested.listBindings(this.nameMock).toList(NameClassPair::getName);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(NAME);
	}

	@Test
	public void testListBindings_Name_AsStream() throws NamingException {
		expectGetReadOnlyContext();

		Binding listResult = new Binding(NAME, CLASS, null);

		setupListBindingsAndNamingEnumeration(listResult);

		try (Stream<String> results = this.tested.listBindings(this.nameMock).toStream(NameClassPair::getName)) {
			List<String> list = results.collect(Collectors.toList());
			assertThat(list).isNotNull();
			assertThat(list).hasSize(1);
			assertThat(list.get(0)).isSameAs(NAME);
		}

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();
	}

	@Test
	public void testListBindings_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		given(this.contextMapperMock.mapFromContext(expectedObject)).willReturn(expectedResult);

		List list = this.tested.listBindings(NAME).toList(this.contextMapperMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(expectedResult);
	}

	@Test
	public void testListBindings_AsStream_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		given(this.contextMapperMock.mapFromContext(expectedObject)).willReturn(expectedResult);

		try (Stream<Object> results = this.tested.listBindings(NAME).toStream(this.contextMapperMock)) {
			List<Object> list = results.collect(Collectors.toList());
			assertThat(list).isNotNull();
			assertThat(list).hasSize(1);
			assertThat(list.get(0)).isSameAs(expectedResult);
		}

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();
	}

	@Test
	public void testListBindings_Name_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		given(this.contextMapperMock.mapFromContext(expectedObject)).willReturn(expectedResult);

		List list = this.tested.listBindings(this.nameMock).toList(this.contextMapperMock);

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();

		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isSameAs(expectedResult);
	}

	@Test
	public void testListBindings_Name_AsStream_ContextMapper() throws NamingException {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		Binding listResult = new Binding("", expectedObject);

		setupListBindingsAndNamingEnumeration(listResult);

		Object expectedResult = expectedObject;
		given(this.contextMapperMock.mapFromContext(expectedObject)).willReturn(expectedResult);

		try (Stream<Object> results = this.tested.listBindings(this.nameMock).toStream(this.contextMapperMock)) {
			List<Object> list = results.collect(Collectors.toList());
			assertThat(list).isNotNull();
			assertThat(list).hasSize(1);
			assertThat(list.get(0)).isSameAs(expectedResult);
		}

		verify(this.dirContextMock).close();
		verify(this.namingEnumerationMock).close();
	}

}
