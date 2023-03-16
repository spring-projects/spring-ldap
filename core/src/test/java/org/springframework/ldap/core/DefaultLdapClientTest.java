/*
 * Copyright 2005-2023 the original author or authors.
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

import java.util.function.Supplier;

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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LdapClient}
 * 
 * @author Josh Cummings
 */
public class DefaultLdapClientTest {

	private static final Name DEFAULT_BASE = LdapUtils.newLdapName("o=example.com");

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private AttributesMapper attributesMapperMock;

	private NamingEnumeration namingEnumerationMock;

	private Name nameMock;

	private NameClassPairCallbackHandler handlerMock;

	private ContextMapper contextMapperMock;

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

	@Before
	public void setUp() throws Exception {

		// Setup ContextSource mock
		contextSourceMock = mock(ContextSource.class);
		// Setup LdapContext mock
		dirContextMock = mock(LdapContext.class);
		// Setup NamingEnumeration mock
		namingEnumerationMock = mock(NamingEnumeration.class);
		// Setup Name mock
		nameMock = LdapUtils.emptyLdapName();
		// Setup Handler mock
		handlerMock = mock(NameClassPairCallbackHandler.class);
		contextMapperMock = mock(ContextMapper.class);
		attributesMapperMock = mock(AttributesMapper.class);
		contextExecutorMock = mock(ContextExecutor.class);
		searchExecutorMock = mock(SearchExecutor.class);
		dirContextProcessorMock = mock(DirContextProcessor.class);
		dirContextOperationsMock = mock(DirContextOperations.class);
		authenticatedContextMock = mock(DirContext.class);
		entryContextCallbackMock = mock(AuthenticatedLdapEntryContextCallback.class);
		odmMock = mock(ObjectDirectoryMapper.class);
		query = LdapQueryBuilder.query().base("ou=spring").filter("ou=user");
		authContextMapperMock = mock(AuthenticatedLdapEntryContextMapper.class);

		tested = LdapClient.create(contextSourceMock);
	}

	private void expectGetReadWriteContext() {
		when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
	}

	private void expectGetReadOnlyContext() {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
	}

	@Test
	public void testSearchContextMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(searchControlsOneLevel(), searchResult);

		tested.search().query((builder) -> builder.base(nameMock)
				.searchScope(SearchScope.ONELEVEL)
				.filter("(ou=somevalue)")).toObject(contextMapperMock);

		verify(contextMapperMock).mapFromContext(any());
		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_StringBase_CallbackHandler() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		tested.search().query((builder) -> builder.base(DEFAULT_BASE.toString())
				.searchScope(SearchScope.ONELEVEL)
				.filter("(ou=somevalue)")).toObject(contextMapperMock);

		verify(contextMapperMock).mapFromContext(any());
		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_AttributeMapper_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(controls, searchResult);

		tested.search().query((builder) -> builder.base(nameMock)
				.searchScope(SearchScope.SUBTREE)
				.filter("(ou=somevalue)")).toObject(attributesMapperMock);

		verify(attributesMapperMock).mapFromAttributes(any());
		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_String_AttributeMapper_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		tested.search().query((builder) -> builder.base(DEFAULT_BASE.toString())
				.searchScope(SearchScope.SUBTREE)
				.filter("(ou=somevalue)")).toObject(attributesMapperMock);

		verify(attributesMapperMock).mapFromAttributes(any());
		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_NameNotFoundException() throws Exception {
		expectGetReadOnlyContext();

		final SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException("some text");
		when(dirContextMock.search(
				eq(nameMock),
				eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls)))).thenThrow(ne);

		try {
			tested.search().query((builder) -> builder.base(nameMock)
					.searchScope(SearchScope.SUBTREE)
					.filter("(ou=somevalue)")).toObject(attributesMapperMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_NamingException() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		when(dirContextMock.search(
				eq(nameMock),
				eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls)))).thenThrow(ne);

		try {
			tested.search().query((builder) -> builder.base(nameMock)
					.filter("(ou=somevalue)")).toObject(attributesMapperMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			// expected
		}

		verify(dirContextMock).close();
	}

	@Test
	public void verifyThatDefaultSearchControlParametersAreAutomaticallyAppliedInSearch() throws Exception {
		Supplier<SearchControls> defaults = mock(Supplier.class);
		when(defaults.get()).thenReturn(new SearchControls());
		LdapClient tested = LdapClient.builder()
				.contextSource(contextSourceMock)
				.defaultSearchControls(defaults).build();

		expectGetReadOnlyContext();

		when(dirContextMock.search(eq(nameMock), anyString(), any())).thenReturn(namingEnumerationMock);
		tested.search().name(nameMock).toEntry();

		verify(defaults).get();
		verify(namingEnumerationMock).close();
		verify(dirContextMock).close();
	}

	@Test
	public void testModifyAttributes() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		tested.modify(nameMock).attributes(mods).execute();

		verify(dirContextMock).modifyAttributes(nameMock, mods);
		verify(dirContextMock).close();
	}

	@Test
	public void testModifyAttributes_String() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		tested.modify(DEFAULT_BASE.toString()).attributes(mods).execute();

		verify(dirContextMock).modifyAttributes(DEFAULT_BASE, mods);
		verify(dirContextMock).close();
	}

	@Test
	public void testModifyAttributes_NamingException() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[1];

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		doThrow(ne).when(dirContextMock).modifyAttributes(nameMock, mods);

		try {
			tested.modify(nameMock).attributes(mods).execute();
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testBind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(nameMock).object(expectedObject).attributes(expectedAttributes).execute();

		verify(dirContextMock).bind(nameMock, expectedObject, expectedAttributes);
		verify(dirContextMock).close();

	}

	@Test
	public void testBind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(DEFAULT_BASE.toString()).object(expectedObject).attributes(expectedAttributes).execute();

		verify(dirContextMock).bind(DEFAULT_BASE, expectedObject, expectedAttributes);
		verify(dirContextMock).close();
	}

	@Test
	public void testBind_NamingException() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		doThrow(ne).when(dirContextMock).bind(nameMock, expectedObject, expectedAttributes);

		try {
			tested.bind(nameMock).object(expectedObject).attributes(expectedAttributes).execute();
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testBindWithContext() throws Exception {
		expectGetReadWriteContext();

		when(dirContextOperationsMock.getDn()).thenReturn(nameMock);
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(false);

		tested.bind(nameMock).object(dirContextOperationsMock).execute();

		verify(dirContextMock).bind(nameMock, dirContextOperationsMock, null);
		verify(dirContextMock).close();
	}


	@Test
	public void testRebindWithContext() throws Exception {
		expectGetReadWriteContext();

		when(dirContextOperationsMock.getDn()).thenReturn(nameMock);
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(false);

		tested.bind(nameMock).object(dirContextOperationsMock).replaceExisting(true).execute();

		verify(dirContextMock).rebind(nameMock, dirContextOperationsMock, null);
		verify(dirContextMock).close();
	}

	@Test
	public void testRebind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(nameMock).object(expectedObject).attributes(expectedAttributes)
				.replaceExisting(true).execute();

		verify(dirContextMock).rebind(nameMock, expectedObject, expectedAttributes);
		verify(dirContextMock).close();
	}

	@Test
	public void testRebind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(DEFAULT_BASE.toString()).object(expectedObject).attributes(expectedAttributes)
				.replaceExisting(true).execute();

		verify(dirContextMock).rebind(DEFAULT_BASE, expectedObject, expectedAttributes);
		verify(dirContextMock).close();
	}

	@Test
	public void testUnbind() throws Exception {
		expectGetReadWriteContext();

		tested.unbind(nameMock).execute();

		verify(dirContextMock).unbind(nameMock);
		verify(dirContextMock).close();
	}

	@Test
	public void testUnbind_String() throws Exception {
		expectGetReadWriteContext();

		tested.unbind(DEFAULT_BASE.toString()).execute();

		verify(dirContextMock).unbind(DEFAULT_BASE);
		verify(dirContextMock).close();
	}

	@Test
	public void testUnbindRecursive() throws Exception {
		expectGetReadWriteContext();

		when(namingEnumerationMock.hasMore()).thenReturn(true, false, false);
		Binding binding = new Binding("cn=Some name", null);
		when(namingEnumerationMock.next()).thenReturn(binding);

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE);
		when(dirContextMock.listBindings(listDn)).thenReturn(namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		when(dirContextMock.listBindings(subListDn)).thenReturn(namingEnumerationMock);

		tested.unbind(new CompositeName(DEFAULT_BASE.toString())).recursive(true).execute();

		verify(dirContextMock).unbind(subListDn);
		verify(dirContextMock).unbind(listDn);
		verify(namingEnumerationMock, times(2)).close();
		verify(dirContextMock).close();
	}

	@Test
	public void testUnbindRecursive_String() throws Exception {
		expectGetReadWriteContext();

		when(namingEnumerationMock.hasMore()).thenReturn(true, false, false);
		Binding binding = new Binding("cn=Some name", null);
		when(namingEnumerationMock.next()).thenReturn(binding);

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE);
		when(dirContextMock.listBindings(listDn)).thenReturn(namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		when(dirContextMock.listBindings(subListDn)).thenReturn(namingEnumerationMock);

		tested.unbind(DEFAULT_BASE.toString()).recursive(true).execute();

		verify(dirContextMock).unbind(subListDn);
		verify(dirContextMock).unbind(listDn);
		verify(namingEnumerationMock, times(2)).close();
		verify(dirContextMock).close();
	}

	@Test
	public void testUnbind_NamingException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		doThrow(ne).when(dirContextMock).unbind(nameMock);

		try {
			tested.unbind(nameMock).execute();
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_PartialResult_IgnoreNotSet() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException ex = new javax.naming.PartialResultException();
		when(dirContextMock.search(eq(nameMock), anyString(), any())).thenThrow(ex);

		try {
			tested.search().name(nameMock).toEntryList();
			fail("PartialResultException expected");
		}
		catch (PartialResultException expected) {
			assertThat(true).isTrue();
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testSearch_PartialResult_IgnoreSet() throws Exception {
		LdapClient tested = LdapClient.builder()
				.contextSource(contextSourceMock)
				.ignorePartialResultException(true).build();

		expectGetReadOnlyContext();

		when(dirContextMock.search(eq(nameMock), anyString(), any())).thenThrow(javax.naming.PartialResultException.class);

		tested.search().name(nameMock).toEntryStream();

		verify(dirContextMock).close();
	}

	@Test
	public void testAuthenticateWithSingleUserFoundShouldBeSuccessful() throws Exception {
		AuthenticatedLdapEntryContextMapper<Object> entryContextMapper = mock(AuthenticatedLdapEntryContextMapper.class);

		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResult(searchControlsRecursive(), searchResult);

		when(contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
				.thenReturn(authenticatedContextMock);
		when(entryContextMapper.mapWithContext(any(), any())).thenReturn(new Object());

		LdapQuery query = LdapQueryBuilder.query().base(nameMock).filter("(ou=somevalue)");
		Object result = tested.authenticate().query(query).password("password").execute(entryContextMapper);

		verify(authenticatedContextMock).close();
		verify(dirContextMock).close();
		assertThat(result).isNotNull();
	}

	@Test
	public void testAuthenticateWithTwoUsersFoundShouldThrowException() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult1 = new SearchResult("", expectedObject, new BasicAttributes());
		SearchResult searchResult2 = new SearchResult("", expectedObject, new BasicAttributes());

		setupSearchResults(searchControlsRecursive(), new SearchResult[] { searchResult1, searchResult2 });

		try {
			LdapQuery query = LdapQueryBuilder.query().base(nameMock).filter("(ou=somevalue)");
			tested.authenticate().query(query).password("password").execute();
			fail("IncorrectResultSizeDataAccessException expected");
		}
		catch (IncorrectResultSizeDataAccessException expected) {
			// expected
		}

		verify(dirContextMock).close();
	}

	@Test
	public void testAuthenticateWhenNoUserWasFoundShouldFail() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		noSearchResults(searchControlsRecursive());

		LdapQuery query = LdapQueryBuilder.query().base(nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(EmptyResultDataAccessException.class).isThrownBy(() ->
				tested.authenticate().query(query).password("password").execute());

		verify(dirContextMock).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAuthenticateQueryPasswordWhenNoUserWasFoundShouldThrowEmptyResult() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		noSearchResults(searchControlsRecursive());

		LdapQuery query = LdapQueryBuilder.query().base(nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(EmptyResultDataAccessException.class).isThrownBy(() ->
				tested.authenticate().query(query).password("password").execute((ctx, entry) -> new Object()));

		verify(dirContextMock).close();
	}

	@Test
	public void testAuthenticateWithFailedAuthenticationShouldFail() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		when(contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
				.thenThrow(new UncategorizedLdapException("Authentication failed"));

		LdapQuery query = LdapQueryBuilder.query().base(nameMock).filter("(ou=somevalue)");
		assertThatExceptionOfType(UncategorizedLdapException.class).isThrownBy(() ->
				tested.authenticate().query(query).password("password").execute());
		verify(dirContextMock).close();
	}

	private void noSearchResults(SearchControls controls) throws Exception {
		when(dirContextMock.search(
				eq(nameMock),
				eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls)))).thenReturn(namingEnumerationMock);

		when(namingEnumerationMock.hasMore()).thenReturn(false);
	}

	private void singleSearchResult(SearchControls controls, SearchResult searchResult) throws Exception {
		setupSearchResults(controls, new SearchResult[] { searchResult });
	}

	private void setupSearchResults(SearchControls controls, SearchResult... searchResults) throws Exception {
		when(dirContextMock.search(
				eq(nameMock),
				eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls)))).thenReturn(namingEnumerationMock);

		if(searchResults.length == 1) {
			when(namingEnumerationMock.hasMore()).thenReturn(true, false);
			when(namingEnumerationMock.next()).thenReturn(searchResults[0]);
		} else if(searchResults.length ==2) {
			when(namingEnumerationMock.hasMore()).thenReturn(true, true, false);
			when(namingEnumerationMock.next()).thenReturn(searchResults[0], searchResults[1]);
		} else {
			throw new IllegalArgumentException("Cannot handle " + searchResults.length + " search results");
		}
	}

	private void singleSearchResultWithStringBase(SearchControls controls, SearchResult searchResult)
			throws Exception {
		when(dirContextMock.search(
				eq(DEFAULT_BASE),
				eq("(ou=somevalue)"),
				argThat(new SearchControlsMatcher(controls)))).thenReturn(namingEnumerationMock);

		when(namingEnumerationMock.hasMore()).thenReturn(true, false);
		when(namingEnumerationMock.next()).thenReturn(searchResult);
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

		public SearchControlsMatcher(SearchControls controls) {
			this.controls = controls;
		}

		@Override
		public boolean matches(SearchControls item) {
			if (item instanceof SearchControls) {
				SearchControls s1 = item;

				return controls.getSearchScope() == s1.getSearchScope()
						&& controls.getReturningObjFlag() == s1.getReturningObjFlag()
						&& controls.getDerefLinkFlag() == s1.getDerefLinkFlag()
						&& controls.getCountLimit() == s1.getCountLimit()
						&& controls.getTimeLimit() == s1.getTimeLimit()
						&& controls.getReturningAttributes() == s1.getReturningAttributes();
			}
			else {
				throw new IllegalArgumentException();
			}
		}
	}
}
