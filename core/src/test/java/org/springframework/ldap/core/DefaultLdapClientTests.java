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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import org.springframework.LdapDataEntry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.PartialResultException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link LdapClient}
 *
 * @author Josh Cummings
 */
public class DefaultLdapClientTests {

	private static final Name DEFAULT_BASE = LdapUtils.newLdapName("o=example.com");

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private AttributesMapper<Object> attributesMapperMock;

	private NamingEnumeration namingEnumerationMock;

	private Name nameMock;

	private NameClassPairCallbackHandler handlerMock;

	private ContextMapper<Object> contextMapperMock;

	private ContextExecutor contextExecutorMock;

	private SearchExecutor searchExecutorMock;

	private LdapClient tested;

	private DirContextProcessor dirContextProcessorMock;

	private DirContextOperations dirContextOperationsMock;

	private DirContext authenticatedContextMock;

	private AuthenticatedLdapEntryContextCallback entryContextCallbackMock;

	private ObjectDirectoryMapper odmMock;

	private LdapQuery query;

	private AuthenticatedLdapEntryContextMapper authContextMapperMock;

	@BeforeEach
	public void setUp() throws Exception {

		// Setup ContextSource mock
		this.contextSourceMock = mock(ContextSource.class);
		// Setup LdapContext mock
		this.dirContextMock = mock(LdapContext.class);
		// Setup NamingEnumeration mock
		this.namingEnumerationMock = mock(NamingEnumeration.class);
		// Setup Name mock
		this.nameMock = LdapUtils.emptyLdapName();
		// Setup Handler mock
		this.handlerMock = mock(NameClassPairCallbackHandler.class);
		this.contextMapperMock = mock(ContextMapper.class);
		this.attributesMapperMock = mock(AttributesMapper.class);
		this.contextExecutorMock = mock(ContextExecutor.class);
		this.searchExecutorMock = mock(SearchExecutor.class);
		this.dirContextProcessorMock = mock(DirContextProcessor.class);
		this.dirContextOperationsMock = mock(DirContextOperations.class);
		this.authenticatedContextMock = mock(DirContext.class);
		this.entryContextCallbackMock = mock(AuthenticatedLdapEntryContextCallback.class);
		this.odmMock = mock(ObjectDirectoryMapper.class);
		this.query = LdapQueryBuilder.query().base("ou=spring").filter("ou=user");
		this.authContextMapperMock = mock(AuthenticatedLdapEntryContextMapper.class);

		this.tested = LdapClient.create(this.contextSourceMock);
	}

	private void expectGetReadWriteContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.dirContextMock);
	}

	private void expectGetReadOnlyContext() {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);
	}

	@Test
	public void testSearchContextMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(searchControlsOneLevel(), searchResult);

		this.tested.search()
			.query((builder) -> builder.base(this.nameMock).searchScope(SearchScope.ONELEVEL).filter("(ou=somevalue)"))
			.toObject(this.contextMapperMock);

		verify(this.contextMapperMock).mapFromContext(any());
		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_StringBase_CallbackHandler() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		this.tested.search()
			.query((builder) -> builder.base(DEFAULT_BASE.toString())
				.searchScope(SearchScope.ONELEVEL)
				.filter("(ou=somevalue)"))
			.toObject(this.contextMapperMock);

		verify(this.contextMapperMock).mapFromContext(any());
		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_AttributeMapper_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(controls, searchResult);

		this.tested.search()
			.query((builder) -> builder.base(this.nameMock).searchScope(SearchScope.SUBTREE).filter("(ou=somevalue)"))
			.toObject(this.attributesMapperMock);

		verify(this.attributesMapperMock).mapFromAttributes(any());
		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_String_AttributeMapper_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		this.tested.search()
			.query((builder) -> builder.base(DEFAULT_BASE.toString())
				.searchScope(SearchScope.SUBTREE)
				.filter("(ou=somevalue)"))
			.toObject(this.attributesMapperMock);

		verify(this.attributesMapperMock).mapFromAttributes(any());
		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_NameNotFoundException() throws Exception {
		expectGetReadOnlyContext();

		final SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException("some text");
		given(this.dirContextMock.search(eq(this.nameMock), eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls))))
			.willThrow(ne);

		try {
			this.tested.search()
				.query((builder) -> builder.base(this.nameMock)
					.searchScope(SearchScope.SUBTREE)
					.filter("(ou=somevalue)"))
				.toObject(this.attributesMapperMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_NamingException() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		given(this.dirContextMock.search(eq(this.nameMock), eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls))))
			.willThrow(ne);

		try {
			this.tested.search()
				.query((builder) -> builder.base(this.nameMock).filter("(ou=somevalue)"))
				.toObject(this.attributesMapperMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			// expected
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void verifyThatDefaultSearchControlParametersAreAutomaticallyAppliedInSearch() throws Exception {
		Supplier<SearchControls> defaults = mock(Supplier.class);
		given(defaults.get()).willReturn(new SearchControls());
		LdapClient tested = LdapClient.withContextSource(this.contextSourceMock)
			.defaultSearchControls(defaults)
			.build();

		expectGetReadOnlyContext();

		given(this.dirContextMock.search(eq(this.nameMock), anyString(), any())).willReturn(this.namingEnumerationMock);
		tested.search().name(this.nameMock).toEntry();

		verify(defaults).get();
		verify(this.namingEnumerationMock).close();
		verify(this.dirContextMock).close();
	}

	@Test
	public void testModifyAttributes() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		this.tested.modify(this.nameMock).attributes(mods).execute();

		verify(this.dirContextMock).modifyAttributes(this.nameMock, mods);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testModifyAttributes_String() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		this.tested.modify(DEFAULT_BASE.toString()).attributes(mods).execute();

		verify(this.dirContextMock).modifyAttributes(DEFAULT_BASE, mods);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testModifyAttributes_NamingException() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		willThrow(ne).given(this.dirContextMock).modifyAttributes(this.nameMock, mods);

		try {
			this.tested.modify(this.nameMock).attributes(mods).execute();
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testBind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		this.tested.bind(this.nameMock).object(expectedObject).attributes(expectedAttributes).execute();

		verify(this.dirContextMock).bind(this.nameMock, expectedObject, expectedAttributes);
		verify(this.dirContextMock).close();

	}

	@Test
	public void testBind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		this.tested.bind(DEFAULT_BASE.toString()).object(expectedObject).attributes(expectedAttributes).execute();

		verify(this.dirContextMock).bind(DEFAULT_BASE, expectedObject, expectedAttributes);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testBind_NamingException() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		willThrow(ne).given(this.dirContextMock).bind(this.nameMock, expectedObject, expectedAttributes);

		try {
			this.tested.bind(this.nameMock).object(expectedObject).attributes(expectedAttributes).execute();
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testBindWithContext() throws Exception {
		expectGetReadWriteContext();

		given(this.dirContextOperationsMock.getDn()).willReturn(this.nameMock);
		given(this.dirContextOperationsMock.isUpdateMode()).willReturn(false);

		this.tested.bind(this.nameMock).object(this.dirContextOperationsMock).execute();

		verify(this.dirContextMock).bind(this.nameMock, this.dirContextOperationsMock, null);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testRebindWithContext() throws Exception {
		expectGetReadWriteContext();

		given(this.dirContextOperationsMock.getDn()).willReturn(this.nameMock);
		given(this.dirContextOperationsMock.isUpdateMode()).willReturn(false);

		this.tested.bind(this.nameMock).object(this.dirContextOperationsMock).replaceExisting(true).execute();

		verify(this.dirContextMock).rebind(this.nameMock, this.dirContextOperationsMock, null);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testRebind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		this.tested.bind(this.nameMock)
			.object(expectedObject)
			.attributes(expectedAttributes)
			.replaceExisting(true)
			.execute();

		verify(this.dirContextMock).rebind(this.nameMock, expectedObject, expectedAttributes);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testRebind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		this.tested.bind(DEFAULT_BASE.toString())
			.object(expectedObject)
			.attributes(expectedAttributes)
			.replaceExisting(true)
			.execute();

		verify(this.dirContextMock).rebind(DEFAULT_BASE, expectedObject, expectedAttributes);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testUnbind() throws Exception {
		expectGetReadWriteContext();

		this.tested.unbind(this.nameMock).execute();

		verify(this.dirContextMock).unbind(this.nameMock);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testUnbind_String() throws Exception {
		expectGetReadWriteContext();

		this.tested.unbind(DEFAULT_BASE.toString()).execute();

		verify(this.dirContextMock).unbind(DEFAULT_BASE);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testUnbindRecursive() throws Exception {
		expectGetReadWriteContext();

		given(this.namingEnumerationMock.hasMore()).willReturn(true, false, false);
		Binding binding = new Binding("cn=Some name", null);
		given(this.namingEnumerationMock.next()).willReturn(binding);

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE);
		given(this.dirContextMock.listBindings(listDn)).willReturn(this.namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		given(this.dirContextMock.listBindings(subListDn)).willReturn(this.namingEnumerationMock);

		this.tested.unbind(new CompositeName(DEFAULT_BASE.toString())).recursive(true).execute();

		verify(this.dirContextMock).unbind(subListDn);
		verify(this.dirContextMock).unbind(listDn);
		verify(this.namingEnumerationMock, times(2)).close();
		verify(this.dirContextMock).close();
	}

	@Test
	public void testUnbindRecursive_String() throws Exception {
		expectGetReadWriteContext();

		given(this.namingEnumerationMock.hasMore()).willReturn(true, false, false);
		Binding binding = new Binding("cn=Some name", null);
		given(this.namingEnumerationMock.next()).willReturn(binding);

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE);
		given(this.dirContextMock.listBindings(listDn)).willReturn(this.namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		given(this.dirContextMock.listBindings(subListDn)).willReturn(this.namingEnumerationMock);

		this.tested.unbind(DEFAULT_BASE.toString()).recursive(true).execute();

		verify(this.dirContextMock).unbind(subListDn);
		verify(this.dirContextMock).unbind(listDn);
		verify(this.namingEnumerationMock, times(2)).close();
		verify(this.dirContextMock).close();
	}

	@Test
	public void testUnbind_NamingException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		willThrow(ne).given(this.dirContextMock).unbind(this.nameMock);

		try {
			this.tested.unbind(this.nameMock).execute();
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_PartialResult_IgnoreNotSet() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException ex = new javax.naming.PartialResultException();
		given(this.dirContextMock.search(eq(this.nameMock), anyString(), any())).willThrow(ex);

		try {
			this.tested.search().name(this.nameMock).toEntryList();
			fail("PartialResultException expected");
		}
		catch (PartialResultException expected) {
			assertThat(true).isTrue();
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testSearch_PartialResult_IgnoreSet() throws Exception {
		LdapClient tested = LdapClient.builder()
			.contextSource(this.contextSourceMock)
			.ignorePartialResultException(true)
			.build();

		expectGetReadOnlyContext();

		given(this.dirContextMock.search(eq(this.nameMock), anyString(), any()))
			.willThrow(javax.naming.PartialResultException.class);

		tested.search().name(this.nameMock).toEntryStream();

		verify(this.dirContextMock).close();
	}

	@Test
	public void testAuthenticateWithSingleUserFoundShouldBeSuccessful() throws Exception {
		AuthenticatedLdapEntryContextMapper<Object> entryContextMapper = mock(
				AuthenticatedLdapEntryContextMapper.class);

		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResult(searchControlsRecursive(), searchResult);

		given(this.contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
			.willReturn(this.authenticatedContextMock);
		given(entryContextMapper.mapWithContext(any(), any())).willReturn(new Object());

		LdapQuery query = LdapQueryBuilder.query().base(this.nameMock).filter("(ou=somevalue)");
		Object result = this.tested.authenticate().query(query).password("password").execute(entryContextMapper);

		verify(this.authenticatedContextMock).close();
		verify(this.dirContextMock).close();
		assertThat(result).isNotNull();
	}

	@Test
	public void testAuthenticateWithTwoUsersFoundShouldThrowException() throws Exception {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult1 = new SearchResult("", expectedObject, new BasicAttributes());
		SearchResult searchResult2 = new SearchResult("", expectedObject, new BasicAttributes());

		setupSearchResults(searchControlsRecursive(), new SearchResult[] { searchResult1, searchResult2 });

		try {
			LdapQuery query = LdapQueryBuilder.query().base(this.nameMock).filter("(ou=somevalue)");
			this.tested.authenticate().query(query).password("password").execute();
			fail("IncorrectResultSizeDataAccessException expected");
		}
		catch (IncorrectResultSizeDataAccessException expected) {
			// expected
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void testAuthenticateWhenNoUserWasFoundShouldFail() throws Exception {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);

		noSearchResults(searchControlsRecursive());

		LdapQuery query = LdapQueryBuilder.query().base(this.nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(EmptyResultDataAccessException.class)
			.isThrownBy(() -> this.tested.authenticate().query(query).password("password").execute());

		verify(this.dirContextMock).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAuthenticateQueryPasswordWhenNoUserWasFoundShouldThrowEmptyResult() throws Exception {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);

		noSearchResults(searchControlsRecursive());

		LdapQuery query = LdapQueryBuilder.query().base(this.nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(EmptyResultDataAccessException.class).isThrownBy(() -> this.tested.authenticate()
			.query(query)
			.password("password")
			.execute((ctx, entry) -> new Object()));

		verify(this.dirContextMock).close();
	}

	@Test
	public void testAuthenticateWithFailedAuthenticationShouldFail() throws Exception {
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		given(this.contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
			.willThrow(new UncategorizedLdapException("Authentication failed"));

		LdapQuery query = LdapQueryBuilder.query().base(this.nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(UncategorizedLdapException.class)
			.isThrownBy(() -> this.tested.authenticate().query(query).password("password").execute());
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperList() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.contextMapperMock.mapFromContext(any())).willReturn("mapped");

		List<Object> results = this.tested.search().name(this.nameMock).map(this.contextMapperMock).list();

		assertThat(results).containsExactly("mapped");
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperStream() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.contextMapperMock.mapFromContext(any())).willReturn("mapped");

		try (Stream<Object> results = this.tested.search().name(this.nameMock).map(this.contextMapperMock).stream()) {
			assertThat(results).containsExactly("mapped");
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperSingle() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.contextMapperMock.mapFromContext(any())).willReturn("mapped");

		Object result = this.tested.search().name(this.nameMock).map(this.contextMapperMock).single();

		assertThat(result).isEqualTo("mapped");
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperSingleWhenEmptyThenException() throws Exception {
		expectGetReadOnlyContext();

		noSearchResultsNameSearch();

		assertThatExceptionOfType(EmptyResultDataAccessException.class)
			.isThrownBy(() -> this.tested.search().name(this.nameMock).map(this.contextMapperMock).single());

		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperOptional() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.contextMapperMock.mapFromContext(any())).willReturn("mapped");

		Optional<Object> result = this.tested.search().name(this.nameMock).map(this.contextMapperMock).optional();

		assertThat(result).contains("mapped");
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperOptionalWhenEmptyThenEmpty() throws Exception {
		expectGetReadOnlyContext();

		noSearchResultsNameSearch();

		Optional<Object> result = this.tested.search().name(this.nameMock).map(this.contextMapperMock).optional();

		assertThat(result).isEmpty();
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapContextMapperSet() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.contextMapperMock.mapFromContext(any())).willReturn("mapped");

		Set<Object> results = this.tested.search().name(this.nameMock).map(this.contextMapperMock).set();

		assertThat(results).containsExactly("mapped");
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapAttributesMapperList() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult("", null, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.attributesMapperMock.mapFromAttributes(any())).willReturn("mapped");

		List<Object> results = this.tested.search().name(this.nameMock).map(this.attributesMapperMock).list();

		assertThat(results).containsExactly("mapped");
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchMapAttributesMapperStream() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult("", null, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		given(this.attributesMapperMock.mapFromAttributes(any())).willReturn("mapped");

		try (Stream<Object> results = this.tested.search()
			.name(this.nameMock)
			.map(this.attributesMapperMock)
			.stream()) {
			assertThat(results).containsExactly("mapped");
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void searchSingle() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		LdapDataEntry result = this.tested.search().name(this.nameMock).single();

		assertThat(result).isSameAs(expectedObject);
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchOptional() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		Optional<LdapDataEntry> result = this.tested.search().name(this.nameMock).optional();

		assertThat(result).containsSame(expectedObject);
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchOptionalWhenEmptyThenEmpty() throws Exception {
		expectGetReadOnlyContext();

		noSearchResultsNameSearch();

		Optional<LdapDataEntry> result = this.tested.search().name(this.nameMock).optional();

		assertThat(result).isEmpty();
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchList() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		List<LdapDataEntry> results = this.tested.search().name(this.nameMock).list();

		assertThat(results).containsExactly((LdapDataEntry) expectedObject);
		verify(this.dirContextMock).close();
	}

	@Test
	public void searchStream() throws Exception {
		expectGetReadOnlyContext();

		LdapDataEntry expectedObject = mock(LdapDataEntry.class);
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResultNameSearch(searchResult);

		try (Stream<LdapDataEntry> results = this.tested.search().name(this.nameMock).stream()) {
			assertThat(results).containsExactly((LdapDataEntry) expectedObject);
		}

		verify(this.dirContextMock).close();
	}

	@Test
	public void createWhenLdapTemplateThenUses() {
		LdapTemplate ldap = mock(LdapTemplate.class);
		given(ldap.getContextSource()).willReturn(this.contextSourceMock);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.dirContextMock);
		LdapClient.create(ldap).search().name("dc=name").toEntryStream();
		verify(ldap).getContextSource();
		verify(ldap).isIgnoreNameNotFoundException();
		verify(ldap).isIgnorePartialResultException();
		verify(ldap).isIgnoreSizeLimitExceededException();
		verify(ldap).getDefaultCountLimit();
		verify(ldap).getDefaultSearchScope();
		verify(ldap).getDefaultTimeLimit();
	}

	private void singleSearchResultNameSearch(SearchResult searchResult) throws Exception {
		given(this.dirContextMock.search(eq(this.nameMock), anyString(), any())).willReturn(this.namingEnumerationMock);
		given(this.namingEnumerationMock.hasMore()).willReturn(true, false);
		given(this.namingEnumerationMock.next()).willReturn(searchResult);
	}

	private void noSearchResultsNameSearch() throws Exception {
		given(this.dirContextMock.search(eq(this.nameMock), anyString(), any())).willReturn(this.namingEnumerationMock);
		given(this.namingEnumerationMock.hasMore()).willReturn(false);
	}

	private void noSearchResults(SearchControls controls) throws Exception {
		given(this.dirContextMock.search(eq(this.nameMock), eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls))))
			.willReturn(this.namingEnumerationMock);

		given(this.namingEnumerationMock.hasMore()).willReturn(false);
	}

	private void singleSearchResult(SearchControls controls, SearchResult searchResult) throws Exception {
		setupSearchResults(controls, new SearchResult[] { searchResult });
	}

	private void setupSearchResults(SearchControls controls, SearchResult... searchResults) throws Exception {
		given(this.dirContextMock.search(eq(this.nameMock), eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls))))
			.willReturn(this.namingEnumerationMock);

		if (searchResults.length == 1) {
			given(this.namingEnumerationMock.hasMore()).willReturn(true, false);
			given(this.namingEnumerationMock.next()).willReturn(searchResults[0]);
		}
		else if (searchResults.length == 2) {
			given(this.namingEnumerationMock.hasMore()).willReturn(true, true, false);
			given(this.namingEnumerationMock.next()).willReturn(searchResults[0], searchResults[1]);
		}
		else {
			throw new IllegalArgumentException("Cannot handle " + searchResults.length + " search results");
		}
	}

	private void singleSearchResultWithStringBase(SearchControls controls, SearchResult searchResult) throws Exception {
		given(this.dirContextMock.search(eq(DEFAULT_BASE), eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls))))
			.willReturn(this.namingEnumerationMock);

		given(this.namingEnumerationMock.hasMore()).willReturn(true, false);
		given(this.namingEnumerationMock.next()).willReturn(searchResult);
	}

	private SearchControls searchControlsRecursive() {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setReturningObjFlag(true);
		return controls;
	}

	private SearchControls searchControlsOneLevel() {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		controls.setReturningObjFlag(true);
		return controls;
	}

	private static class SearchControlsMatcher implements ArgumentMatcher<SearchControls> {

		private final SearchControls controls;

		SearchControlsMatcher(SearchControls controls) {
			this.controls = controls;
		}

		@Override
		public boolean matches(SearchControls item) {
			if (item instanceof SearchControls) {
				SearchControls s1 = item;

				return this.controls.getSearchScope() == s1.getSearchScope()
						&& this.controls.getReturningObjFlag() == s1.getReturningObjFlag()
						&& this.controls.getDerefLinkFlag() == s1.getDerefLinkFlag()
						&& this.controls.getCountLimit() == s1.getCountLimit()
						&& this.controls.getTimeLimit() == s1.getTimeLimit()
						&& this.controls.getReturningAttributes() == s1.getReturningAttributes();
			}
			else {
				throw new IllegalArgumentException();
			}
		}

	}

}
