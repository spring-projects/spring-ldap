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

package org.springframework.ldap.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * Tests {@link LdapClient.Builder#dirContextPostProcessor}.
 *
 * @author Josh Cummings
 */
public class DefaultLdapClientDirContextPostProcessorTests {

	private final Name name = LdapUtils.newLdapName("ou=name");

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	@BeforeEach
	public void setUp() {
		this.contextSourceMock = mock(ContextSource.class);
		this.dirContextMock = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);
	}

	@Test
	public void singleInvokesConfiguredDirContextPostProcessor() throws Exception {
		LdapClient client = LdapClient.withContextSource(this.contextSourceMock)
			.dirContextPostProcessor((adapter) -> adapter.setMayRemoveByValue((original, updated) -> false))
			.build();
		DirContextAdapter entry = new DirContextAdapter();
		entry.setAttributeValues("description", new String[] { "a", "b" });
		whenSearching(this.name).willReturn(result(entry, null));

		DirContextAdapter actual = client.search().name(this.name).single();
		actual.setUpdateMode(true);
		actual.setAttributeValues("description", new String[] { "a", "c" });

		ModificationItem[] items = actual.getModificationItems();
		assertThat(items).hasSize(1);
		assertThat(items[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
	}

	@Test
	public void deprecatedToEntryAlsoInvokesConfiguredDirContextPostProcessor() throws Exception {
		LdapClient client = LdapClient.withContextSource(this.contextSourceMock)
			.dirContextPostProcessor((adapter) -> adapter.setMayRemoveByValue((original, updated) -> false))
			.build();
		DirContextAdapter entry = new DirContextAdapter();
		entry.setAttributeValues("description", new String[] { "a", "b" });
		whenSearching(this.name).willReturn(result(entry, null));

		DirContextAdapter actual = client.search().name(this.name).toEntry();
		actual.setUpdateMode(true);
		actual.setAttributeValues("description", new String[] { "a", "c" });

		ModificationItem[] items = actual.getModificationItems();
		assertThat(items).hasSize(1);
		assertThat(items[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
	}

	@Test
	public void dirContextPostProcessorAppliesEvenWithExplicitContextMapper() throws Exception {
		LdapClient client = LdapClient.withContextSource(this.contextSourceMock)
			.dirContextPostProcessor((adapter) -> adapter.setMayRemoveByValue((original, updated) -> false))
			.build();
		DirContextAdapter entry = new DirContextAdapter();
		entry.setAttributeValues("description", new String[] { "a", "b" });
		whenSearching(this.name).willReturn(result(entry, null));

		ContextMapper<DirContextAdapter> passthrough = (ctx) -> (DirContextAdapter) ctx;
		DirContextAdapter actual = client.search().name(this.name).map(passthrough).single();
		actual.setUpdateMode(true);
		actual.setAttributeValues("description", new String[] { "a", "c" });

		ModificationItem[] items = actual.getModificationItems();
		assertThat(items).hasSize(1);
		assertThat(items[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
	}

	@Test
	public void modifyInvokesConfiguredDirContextPostProcessorOnInternalEntry() {
		List<Name> seen = new ArrayList<>();
		LdapClient client = LdapClient.withContextSource(this.contextSourceMock)
			.dirContextPostProcessor((adapter) -> seen.add(adapter.getDn()))
			.build();

		client.modify(this.name);

		assertThat(seen).containsExactly(this.name);
	}

	@Test
	public void dirContextPostProcessorsAreAdditive() {
		List<String> invoked = new ArrayList<>();
		LdapClient client = LdapClient.withContextSource(this.contextSourceMock)
			.dirContextPostProcessor((adapter) -> invoked.add("first"))
			.dirContextPostProcessor((adapter) -> invoked.add("second"))
			.build();

		client.modify(this.name);

		assertThat(invoked).containsExactly("first", "second");
	}

	private static NamingEnumeration result(Object object, Attributes attributes) {
		return results(new SearchResult("ou=name", object, attributes));
	}

	private static NamingEnumeration results(SearchResult... results) {
		return new NamingEnumeration(results);
	}

	private BDDMockito.BDDMyOngoingStubbing<javax.naming.NamingEnumeration<SearchResult>> whenSearching(Name name)
			throws Exception {
		return given(this.dirContextMock.search(eq(name), anyString(), any()));
	}

	private static class NamingEnumeration implements javax.naming.NamingEnumeration<SearchResult> {

		private final Iterator<SearchResult> names;

		NamingEnumeration(SearchResult... results) {
			this.names = Arrays.asList(results).iterator();
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
