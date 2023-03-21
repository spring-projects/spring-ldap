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

import java.util.Arrays;
import java.util.Iterator;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import org.springframework.LdapDataEntry;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultLdapClientLookupTest {

	private static final Name DEFAULT_BASE = LdapUtils.newLdapName("o=example.com");

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private final Name name = LdapUtils.newLdapName("ou=name");

	private LdapClient tested;

	@Before
	public void setUp() throws Exception {
		contextSourceMock = mock(ContextSource.class);
		dirContextMock = mock(LdapContext.class);
		tested = LdapClient.create(contextSourceMock);
	}

	private void expectGetReadOnlyContext() {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
	}

	@Test
	public void testLookup() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expected = new DirContextAdapter();
		whenSearching(name).thenReturn(result(expected, null));

		LdapDataEntry actual = tested.search().name(name).toEntry();

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expected = new DirContextAdapter();
		whenSearching(DEFAULT_BASE).thenReturn(result(expected, null));

		LdapDataEntry actual = tested.search().name(DEFAULT_BASE.toString()).toEntry();

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		whenSearching(name).thenThrow(ne);

		assertThatExceptionOfType(NameNotFoundException.class).describedAs("NameNotFoundException expected")
				.isThrownBy(() -> tested.search().name(name).toEntry());
		verify(dirContextMock).close();
	}

	@Test
	public void testLookup_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		Attributes expected = new BasicAttributes();
		whenSearching(name).thenReturn(result(null, expected));

		AttributesMapper<Attributes> mapper = (attributes) -> attributes;
		Attributes actual = tested.search().name(name).toObject(mapper);

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		Attributes expected = new BasicAttributes();
		whenSearching(DEFAULT_BASE).thenReturn(result(null, expected));

		AttributesMapper<Attributes> mapper = (attributes) -> attributes;
		Attributes actual = tested.search().name(DEFAULT_BASE.toString()).toObject(mapper);

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_AttributesMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		whenSearching(name).thenThrow(ne);

		AttributesMapper<?> mapper = (attributes) -> attributes;
		assertThatExceptionOfType(NameNotFoundException.class).describedAs("NameNotFoundException expected")
				.isThrownBy(() -> tested.search().name(name).toObject(mapper));
		verify(dirContextMock).close();
	}

	// Tests for lookup(name, ContextMapper)

	@Test
	public void testLookup_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		whenSearching(name).thenReturn(result(expected, null));

		ContextMapper<?> mapper = (ctx) -> ctx;
		Object actual = tested.search().name(name).toObject(mapper);

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_String_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		Object expected = new Object();
		whenSearching(DEFAULT_BASE).thenReturn(result(expected, null));

		ContextMapper<?> mapper = (ctx) -> ctx;
		Object actual = tested.search().name(DEFAULT_BASE.toString()).toObject(mapper);

		verify(dirContextMock).close();
		assertThat(actual).isSameAs(expected);
	}

	@Test
	public void testLookup_ContextMapper_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		whenSearching(name).thenThrow(ne);

		ContextMapper<?> mapper = (ctx) -> ctx;
		assertThatExceptionOfType(NameNotFoundException.class).describedAs("NameNotFoundException expected")
				.isThrownBy(() -> tested.search().name(name).toObject(mapper));
		verify(dirContextMock).close();
	}

	private static NamingEnumeration result(Object object, Attributes attributes) {
		return results(new SearchResult("ou=name", object, attributes));
	}

	private static NamingEnumeration results(SearchResult... results) {
		return new NamingEnumeration(results);
	}

	private OngoingStubbing<javax.naming.NamingEnumeration<SearchResult>> whenSearching(Name name) throws Exception {
		return when(dirContextMock.search(eq(name), anyString(), any()));
	}

	private static class NamingEnumeration implements javax.naming.NamingEnumeration<SearchResult> {

		private final Iterator<SearchResult> names;

		public NamingEnumeration(SearchResult... results) {
			names = Arrays.asList(results).iterator();
		}

		@Override
		public SearchResult next() {
			return this.names.next();
		}

		@Override
		public boolean hasMore() {
			return this.names.hasNext();
		}

		@Override
		public void close() throws NamingException {

		}

		@Override
		public boolean hasMoreElements() {
			return this.names.hasNext();
		}

		@Override
		public SearchResult nextElement() {
			return this.names.next();
		}

	}

}
