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

import java.util.Collections;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

public class LdapTemplateLookupTests {

	private static final String DEFAULT_BASE_STRING = "o=example.com";

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private AttributesMapper attributesMapperMock;

	private Name nameMock;

	private ContextMapper contextMapperMock;

	private LdapTemplate tested;

	private ObjectDirectoryMapper odmMock;

	@BeforeEach
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
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);
	}

	// Tests for lookup(name)

	@Test
	public void testLookup() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		given(this.dirContextMock.lookup(this.nameMock)).willReturn(expected);

		Object actual = this.tested.lookup(this.nameMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		given(this.dirContextMock.lookup(DEFAULT_BASE_STRING)).willReturn(expected);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		given(this.dirContextMock.lookup(this.nameMock)).willThrow(ne);

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
		given(this.dirContextMock.getAttributes(this.nameMock)).willReturn(expectedAttributes);

		Object expected = new Object();
		given(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).willReturn(expected);

		Object actual = this.tested.lookup(this.nameMock, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		BasicAttributes expectedAttributes = new BasicAttributes();
		given(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING)).willReturn(expectedAttributes);

		Object expected = new Object();
		given(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).willReturn(expected);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, this.attributesMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_AttributesMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		given(this.dirContextMock.getAttributes(this.nameMock)).willThrow(ne);

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
		given(this.dirContextMock.lookup(this.nameMock)).willReturn(expected);

		given(this.contextMapperMock.mapFromContext(expected)).willReturn(transformed);

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
		given(this.dirContextMock.lookup(this.nameMock)).willReturn(expectedContext);
		given(this.odmMock.mapFromLdapDataEntry(expectedContext, expectedClass)).willReturn(transformed);

		given(this.nameMock.getAll()).willReturn(Collections.<String>enumeration(Collections.<String>emptyList()));
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
		given(this.dirContextMock.lookup(DEFAULT_BASE_STRING)).willReturn(expected);

		given(this.contextMapperMock.mapFromContext(expected)).willReturn(transformed);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

	@Test
	public void testLookup_ContextMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		given(this.dirContextMock.lookup(this.nameMock)).willThrow(ne);

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

		given(this.dirContextMock.getAttributes(this.nameMock, attributeNames)).willReturn(expectedAttributes);

		Object expected = new Object();
		given(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).willReturn(expected);

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

		given(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).willReturn(expectedAttributes);

		Object expected = new Object();
		given(this.attributesMapperMock.mapFromAttributes(expectedAttributes)).willReturn(expected);

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

		given(this.dirContextMock.getAttributes(name, attributeNames)).willReturn(expectedAttributes);

		Object transformed = new Object();
		given(this.contextMapperMock.mapFromContext(adapter)).willReturn(transformed);

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

		given(this.dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).willReturn(expectedAttributes);

		LdapName name = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
		DirContextAdapter adapter = new DirContextAdapter(expectedAttributes, name);

		Object transformed = new Object();
		given(this.contextMapperMock.mapFromContext(adapter)).willReturn(transformed);

		Object actual = this.tested.lookup(DEFAULT_BASE_STRING, attributeNames, this.contextMapperMock);

		verify(this.dirContextMock).close();

		assertThat(actual).isSameAs(transformed);
	}

}
