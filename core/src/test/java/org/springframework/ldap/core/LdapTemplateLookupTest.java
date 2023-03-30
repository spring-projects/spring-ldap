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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.support.LdapUtils;

public class LdapTemplateLookupTest {

	private static final String DEFAULT_BASE_STRING = "o=example.com";

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private AttributesMapper attributesMapperMock;

	private Name nameMock;

	private ContextMapper contextMapperMock;

	private LdapTemplate tested;

	private ObjectDirectoryMapper odmMock;

	@Before
	public void setUp() throws Exception {
		// Setup ContextSource mock
		this.contextSourceMock = mock(ContextSource.class);

		// Setup LdapContext mock
		this.dirContextMock = mock(LdapContext.class);

		// Setup Name mock
		this.nameMock = mock(Name.class);
		this.contextMapperMock = mock(ContextMapper.class);
		this.attributesMapperMock = mock(AttributesMapper.class);
		this.odmMock = mock(ObjectDirectoryMapper.class);

		this.tested = new LdapTemplate(this.contextSourceMock);
		this.tested.setObjectDirectoryMapper(this.odmMock);
	}

	private void expectGetReadOnlyContext() {
		when(this.contextSourceMock.getReadOnlyContext()).thenReturn(this.dirContextMock);
	}

	// Tests for lookup(name)

	@Test
	public void testLookup() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		when(this.dirContextMock.lookup(this.nameMock)).thenReturn(expected);

		Object actual = this.tested.lookup(this.nameMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		when(this.dirContextMock.lookup(DEFAULT_BASE_STRING)).thenReturn(expected);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		when(this.dirContextMock.lookup(this.nameMock)).thenThrow(ne);

		try {
			this.tested.lookup(this.nameMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	// Tests for lookup(name, AttributesMapper)

	@Test
	public void testLookup_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		BasicAttributes expectedAttributes = new BasicAttributes();
		when(this.dirContextMock.getAttributes(this.nameMock)).thenReturn(expectedAttributes);

		Object expected = new Object();
		when(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

		Object actual = this.tested.lookup(this.nameMock, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		BasicAttributes expectedAttributes = new BasicAttributes();
		when(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING)).thenReturn(expectedAttributes);

		Object expected = new Object();
		when(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_AttributesMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		when(this.dirContextMock.getAttributes(this.nameMock)).thenThrow(ne);

		try {
			this.tested.lookup(this.nameMock, this.attributesMapperMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	// Tests for lookup(name, ContextMapper)

	@Test
	public void testLookup_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		Object transformed = new Object();
		Object expected = new Object();
		when(this.dirContextMock.lookup(this.nameMock)).thenReturn(expected);

		when(this.contextMapperMock.mapFromContext(expected)).thenReturn(transformed);

		Object actual = this.tested.lookup(this.nameMock, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

	@Test
	public void testFindByDn() throws NamingException {
		expectGetReadOnlyContext();

		Object transformed = new Object();
		Class<Object> expectedClass = Object.class;

		DirContextAdapter expectedContext = new DirContextAdapter();
		when(this.dirContextMock.lookup(this.nameMock)).thenReturn(expectedContext);
		when(this.odmMock.mapFromLdapDataEntry(expectedContext, expectedClass)).thenReturn(transformed);

		when(this.nameMock.getAll()).thenReturn(Collections.<String>enumeration(Collections.<String>emptyList()));
		// Perform test
		Object result = this.tested.findByDn(this.nameMock, expectedClass);
		assertThat(result).isSameAs(transformed);

		verify(this.odmMock).manageClass(expectedClass);
	}

	@Test
	public void testLookup_String_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		Object transformed = new Object();
		Object expected = new Object();
		when(this.dirContextMock.lookup(DEFAULT_BASE_STRING)).thenReturn(expected);

		when(this.contextMapperMock.mapFromContext(expected)).thenReturn(transformed);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

	@Test
	public void testLookup_ContextMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		when(this.dirContextMock.lookup(this.nameMock)).thenThrow(ne);

		try {
			this.tested.lookup(this.nameMock, this.contextMapperMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	// Tests for lookup(name, attributes, AttributesMapper)

	@Test
	public void testLookup_ReturnAttributes_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		String[] attributeNames = new String[] { "cn" };

		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("cn", "Some Name");

		when(this.dirContextMock.getAttributes(this.nameMock, attributeNames)).thenReturn(expectedAttributes);

		Object expected = new Object();
		when(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

		Object actual = this.tested.lookup(this.nameMock, attributeNames, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String_ReturnAttributes_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		String[] attributeNames = new String[] { "cn" };

		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("cn", "Some Name");

		when(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).thenReturn(expectedAttributes);

		Object expected = new Object();
		when(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, attributeNames, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	// Tests for lookup(name, attributes, ContextMapper)

	@Test
	public void testLookup_ReturnAttributes_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		String[] attributeNames = new String[] { "cn" };

		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("cn", "Some Name");

		LdapName name = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
		DirContextAdapter adapter = new DirContextAdapter(expectedAttributes, name);

		when(this.dirContextMock.getAttributes(name, attributeNames)).thenReturn(expectedAttributes);

		Object transformed = new Object();
		when(this.contextMapperMock.mapFromContext(adapter)).thenReturn(transformed);

		Object actual = this.tested.lookup(name, attributeNames, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

	@Test
	public void testLookup_String_ReturnAttributes_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		String[] attributeNames = new String[] { "cn" };

		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("cn", "Some Name");

		when(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).thenReturn(expectedAttributes);

		LdapName name = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
		DirContextAdapter adapter = new DirContextAdapter(expectedAttributes, name);

		Object transformed = new Object();
		when(this.contextMapperMock.mapFromContext(adapter)).thenReturn(transformed);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, attributeNames, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

}
