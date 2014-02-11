/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.LdapDataEntry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.PartialResultException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Unit tests for the LdapTemplate class.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplateTest {

	private static final String DEFAULT_BASE_STRING = "o=example.com";

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	private AttributesMapper attributesMapperMock;

	private NamingEnumeration namingEnumerationMock;

	private Name nameMock;

	private NameClassPairCallbackHandler handlerMock;

	private ContextMapper contextMapperMock;

	private ContextExecutor contextExecutorMock;

	private SearchExecutor searchExecutorMock;

	private LdapTemplate tested;

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

        tested = new LdapTemplate(contextSourceMock);
        tested.setObjectDirectoryMapper(odmMock);
	}

	private void expectGetReadWriteContext() {
		when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
	}

	private void expectGetReadOnlyContext() {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
	}

    @Test
	public void testSearch_CallbackHandler() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(searchControlsOneLevel(), searchResult);

		tested.search(nameMock, "(ou=somevalue)", 1, true, handlerMock);

        verify(handlerMock).handleNameClassPair(searchResult);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_StringBase_CallbackHandler() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, true, handlerMock);

        verify(handlerMock).handleNameClassPair(searchResult);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_CallbackHandler_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(controls, searchResult);

		tested.search(nameMock, "(ou=somevalue)", handlerMock);

        verify(handlerMock).handleNameClassPair(searchResult);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_String_CallbackHandler_Defaults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", handlerMock);

        verify(handlerMock).handleNameClassPair(searchResult);
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
			tested.search(nameMock, "(ou=somevalue)", handlerMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
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
			tested.search(nameMock, "(ou=somevalue)", handlerMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			// expected
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_CallbackHandler_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResult(controls, searchResult);

		tested.search(nameMock, "(ou=somevalue)", controls, handlerMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(handlerMock).handleNameClassPair(searchResult);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_String_CallbackHandler_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		SearchResult searchResult = new SearchResult("", new Object(), new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, handlerMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(handlerMock).handleNameClassPair(searchResult);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_String_AttributesMapper_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, attributesMapperMock,
				dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_Name_AttributesMapper_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResult(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", controls, attributesMapperMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_SearchControls_ContextMapper_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, contextMapperMock,
				dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_Name_SearchControls_ContextMapper_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", controls, contextMapperMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_AttributesMapper_ReturningAttrs() throws Exception {
		expectGetReadOnlyContext();

		String[] attrs = new String[0];
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		controls.setReturningObjFlag(false);
		controls.setReturningAttributes(attrs);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResult(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", 1, attrs, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_AttributesMapper_ReturningAttrs() throws Exception {
		expectGetReadOnlyContext();

		String[] attrs = new String[0];
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		controls.setReturningObjFlag(false);
		controls.setReturningAttributes(attrs);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, attrs, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
    public void verifyThatDefaultSearchControlParametersAreAutomaticallyAppliedInSearch() throws Exception {
        tested.setDefaultSearchScope(SearchControls.ONELEVEL_SCOPE);
        tested.setDefaultCountLimit(5000);
        tested.setDefaultTimeLimit(500);

        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setReturningObjFlag(false);
        controls.setCountLimit(5000);
        controls.setTimeLimit(500);
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null, expectedAttributes);

        singleSearchResult(controls, searchResult);

        Object expectedResult = new Object();
        when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

        List list = tested.search(nameMock, "(ou=somevalue)", attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    @Test
	public void testSearch_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResult(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", 1, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_AttributesMapper_Default() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResult(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_AttributesMapper_Default() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
		singleSearchResult(searchControlsOneLevel(), searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", 1, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
    public void testFindOne() throws Exception {
        Class<Object> expectedClass = Object.class;

        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
        when(odmMock.filterFor(expectedClass,
                new EqualsFilter("ou", "somevalue"))).thenReturn(new EqualsFilter("ou", "somevalue"));

        DirContextAdapter expectedObject = new DirContextAdapter();
        SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());
        singleSearchResult(searchControlsRecursive(), searchResult);

        Object expectedResult = expectedObject;
        when(odmMock.mapFromLdapDataEntry(expectedObject, expectedClass)).thenReturn(expectedResult);

        Object result = tested.findOne(query()
                .where("ou").is("somevalue"), expectedClass);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertSame(expectedResult, result);
    }

    @Test
    public void verifyThatFindOneThrowsEmptyResultIfNoResult() throws Exception {
        Class<Object> expectedClass = Object.class;

        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
        when(odmMock.filterFor(expectedClass,
                new EqualsFilter("ou", "somevalue"))).thenReturn(new EqualsFilter("ou", "somevalue"));

        noSearchResults(searchControlsRecursive());

        try {
            tested.findOne(query().where("ou").is("somevalue"), expectedClass);
            fail("EmptyResultDataAccessException expected");
        } catch (EmptyResultDataAccessException expected) {
            assertTrue(true);
        }

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
        verify(odmMock, never()).mapFromLdapDataEntry(any(LdapDataEntry.class), any(Class.class));
    }

    @Test
    public void verifyThatFindOneThrowsIncorrectResultSizeDataAccessExceptionWhenMoreResults() throws Exception {
        Class<Object> expectedClass = Object.class;

        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
        when(odmMock.filterFor(expectedClass,
                new EqualsFilter("ou", "somevalue"))).thenReturn(new EqualsFilter("ou", "somevalue"));

        DirContextAdapter expectedObject = new DirContextAdapter();
        SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

        setupSearchResults(searchControlsRecursive(), new SearchResult[]{searchResult, searchResult});

        Object expectedResult = expectedObject;
        when(odmMock.mapFromLdapDataEntry(expectedObject, expectedClass)).thenReturn(expectedResult, expectedResult);

        try {
            tested.findOne(query().where("ou").is("somevalue"), expectedClass);
            fail("EmptyResultDataAccessException expected");
        } catch (IncorrectResultSizeDataAccessException expected) {
            assertTrue(true);
        }

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
    }

    @Test
	public void testSearch_ContextMapper_ReturningAttrs() throws Exception {
		expectGetReadOnlyContext();

		String[] attrs = new String[0];

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningAttributes(attrs);

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", 1, attrs, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_ContextMapper_ReturningAttrs() throws Exception {
		expectGetReadOnlyContext();

		String[] attrs = new String[0];

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningAttributes(attrs);

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, attrs, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);


		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_ContextMapper_Default() throws Exception {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_ContextMapper_Default() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_SearchControls_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_SearchControls_ContextMapper_ReturningObjFlagNotSet() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		SearchControls expectedControls = new SearchControls();
		expectedControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		expectedControls.setReturningObjFlag(true);

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResultWithStringBase(expectedControls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_Name_SearchControls_ContextMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(controls, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", controls, contextMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_String_SearchControls_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResultWithStringBase(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testSearch_Name_SearchControls_AttributesMapper() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsOneLevel();
		controls.setReturningObjFlag(false);

		BasicAttributes expectedAttributes = new BasicAttributes();
		SearchResult searchResult = new SearchResult("", null, expectedAttributes);

		singleSearchResult(controls, searchResult);

		Object expectedResult = new Object();
		when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expectedResult);

		List list = tested.search(nameMock, "(ou=somevalue)", controls, attributesMapperMock);

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();

        assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(expectedResult, list.get(0));
	}

    @Test
	public void testModifyAttributes() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[0];

		tested.modifyAttributes(nameMock, mods);

        verify(dirContextMock).modifyAttributes(nameMock, mods);
        verify(dirContextMock).close();
	}

    @Test
	public void testModifyAttributes_String() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[0];

		tested.modifyAttributes(DEFAULT_BASE_STRING, mods);

        verify(dirContextMock).modifyAttributes(DEFAULT_BASE_STRING, mods);
        verify(dirContextMock).close();
	}

    @Test
	public void testModifyAttributes_NamingException() throws Exception {
		expectGetReadWriteContext();

		ModificationItem[] mods = new ModificationItem[0];

        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        doThrow(ne).when(dirContextMock).modifyAttributes(nameMock, mods);

		try {
			tested.modifyAttributes(nameMock, mods);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testBind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(nameMock, expectedObject, expectedAttributes);

        verify(dirContextMock).bind(nameMock, expectedObject, expectedAttributes);
        verify(dirContextMock).close();

    }

    @Test
	public void testBind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.bind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);

        verify(dirContextMock).bind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);
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
			tested.bind(nameMock, expectedObject, expectedAttributes);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
    }

    @Test
    public void testBindWithContext() throws Exception {
		expectGetReadWriteContext();

		when(dirContextOperationsMock.getDn()).thenReturn(nameMock);
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(false);

		tested.bind(dirContextOperationsMock);

        verify(dirContextMock).bind(nameMock, dirContextOperationsMock, null);
        verify(dirContextMock).close();
	}

    @Test
    public void testCreateWithIdSpecified() throws NamingException {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        LdapName expectedName = LdapUtils.newLdapName("ou=someOu");
        when(odmMock.getId(expectedObject)).thenReturn(expectedName);

        ArgumentCaptor<DirContextAdapter> ctxCaptor = ArgumentCaptor.forClass(DirContextAdapter.class);
        doNothing().when(odmMock).mapToLdapDataEntry(eq(expectedObject), ctxCaptor.capture());

        tested.create(expectedObject);

        verify(odmMock, never()).setId(expectedObject, expectedName);
        verify(dirContextMock).bind(expectedName, ctxCaptor.getValue(), null);
        verify(dirContextMock).close();
    }

    @Test
    public void testCreateWithCalculatedId() throws NamingException {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        LdapName expectedName = LdapUtils.newLdapName("ou=someOu");
        when(odmMock.getId(expectedObject)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedObject)).thenReturn(expectedName);

        ArgumentCaptor<DirContextAdapter> ctxCaptor = ArgumentCaptor.forClass(DirContextAdapter.class);
        doNothing().when(odmMock).mapToLdapDataEntry(eq(expectedObject), ctxCaptor.capture());

        tested.create(expectedObject);

        verify(odmMock).setId(expectedObject, expectedName);
        verify(dirContextMock).bind(expectedName, ctxCaptor.getValue(), null);
        verify(dirContextMock).close();
    }

    @Test
    public void testCreateWithNoIdAvailableThrows() throws NamingException {
        Object expectedObject = new Object();
        when(odmMock.getId(expectedObject)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedObject)).thenReturn(null);

        try {
            tested.create(expectedObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testUpdateWithIdSpecified() throws NamingException {
        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
        LdapName expectedName = LdapUtils.newLdapName("ou=someOu");

        ModificationItem[] expectedModificationItems = new ModificationItem[0];
        DirContextOperations ctxMock = mock(DirContextOperations.class);
        when(ctxMock.getDn()).thenReturn(expectedName);
        when(ctxMock.isUpdateMode()).thenReturn(true);
        when(ctxMock.getModificationItems()).thenReturn(expectedModificationItems);

        Object expectedObject = new Object();
        when(odmMock.getId(expectedObject)).thenReturn(expectedName);
        when(odmMock.getCalculatedId(expectedObject)).thenReturn(null);

        when(dirContextMock.lookup(expectedName)).thenReturn(ctxMock);

        tested.update(expectedObject);

        verify(odmMock, never()).setId(expectedObject, expectedName);
        verify(odmMock).mapToLdapDataEntry(expectedObject, ctxMock);
        verify(dirContextMock).modifyAttributes(expectedName, expectedModificationItems);

        verify(dirContextMock, times(2)).close();
    }

    @Test
    public void testUpdateWithIdCalculated() throws NamingException {
        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
        LdapName expectedName = LdapUtils.newLdapName("ou=someOu");

        ModificationItem[] expectedModificationItems = new ModificationItem[0];
        DirContextOperations ctxMock = mock(DirContextOperations.class);
        when(ctxMock.getDn()).thenReturn(expectedName);
        when(ctxMock.isUpdateMode()).thenReturn(true);
        when(ctxMock.getModificationItems()).thenReturn(expectedModificationItems);

        Object expectedObject = new Object();
        when(odmMock.getId(expectedObject)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedObject)).thenReturn(expectedName);

        when(dirContextMock.lookup(expectedName)).thenReturn(ctxMock);

        tested.update(expectedObject);

        verify(odmMock).setId(expectedObject, expectedName);
        verify(odmMock).mapToLdapDataEntry(expectedObject, ctxMock);
        verify(dirContextMock).modifyAttributes(expectedName, expectedModificationItems);

        verify(dirContextMock, times(2)).close();
    }

    @Test
    public void testUpdateWithIdChanged() throws NamingException {
        Object expectedObject = new Object();

        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock, dirContextMock);
        LdapName expectedOriginalName = LdapUtils.newLdapName("ou=someOu");
        LdapName expectedNewName = LdapUtils.newLdapName("ou=someOtherOu");

        ArgumentCaptor<DirContextAdapter> ctxCaptor = ArgumentCaptor.forClass(DirContextAdapter.class);
        doNothing().when(odmMock).mapToLdapDataEntry(eq(expectedObject), ctxCaptor.capture());

        when(odmMock.getId(expectedObject)).thenReturn(expectedOriginalName);
        when(odmMock.getCalculatedId(expectedObject)).thenReturn(expectedNewName);

        tested.update(expectedObject);

        verify(odmMock).setId(expectedObject, expectedNewName);
        verify(dirContextMock).unbind(expectedOriginalName);
        verify(dirContextMock).bind(expectedNewName, ctxCaptor.getValue(), null);
        verify(dirContextMock, times(2)).close();
    }

    @Test
	public void testUnbind() throws Exception {
		expectGetReadWriteContext();

		tested.unbind(nameMock);

        verify(dirContextMock).unbind(nameMock);
        verify(dirContextMock).close();
	}

    @Test
	public void testUnbind_String() throws Exception {
		expectGetReadWriteContext();

		tested.unbind(DEFAULT_BASE_STRING);

        verify(dirContextMock).unbind(DEFAULT_BASE_STRING);
        verify(dirContextMock).close();
	}

    @Test
    public void testRebindWithContext() throws Exception {
		expectGetReadWriteContext();

		when(dirContextOperationsMock.getDn()).thenReturn(nameMock);
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(false);

		tested.rebind(dirContextOperationsMock);

        verify(dirContextMock).rebind(nameMock, dirContextOperationsMock, null);
        verify(dirContextMock).close();
	}

    @Test
	public void testUnbindRecursive() throws Exception {
		expectGetReadWriteContext();

		when(namingEnumerationMock.hasMore()).thenReturn(true, false, false);
		Binding binding = new Binding("cn=Some name", null);
		when(namingEnumerationMock.next()).thenReturn(binding);

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
		when(dirContextMock.listBindings(listDn)).thenReturn(namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		when(dirContextMock.listBindings(subListDn)).thenReturn(namingEnumerationMock);

		tested.unbind(new CompositeName(DEFAULT_BASE_STRING), true);

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

		LdapName listDn = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
		when(dirContextMock.listBindings(listDn)).thenReturn(namingEnumerationMock);
		LdapName subListDn = LdapUtils.newLdapName("cn=Some name, o=example.com");
		when(dirContextMock.listBindings(subListDn)).thenReturn(namingEnumerationMock);

		tested.unbind(DEFAULT_BASE_STRING, true);

        verify(dirContextMock).unbind(subListDn);
        verify(dirContextMock).unbind(listDn);
        verify(namingEnumerationMock, times(2)).close();
        verify(dirContextMock).close();
	}

    @Test
	public void testRebind() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.rebind(nameMock, expectedObject, expectedAttributes);

        verify(dirContextMock).rebind(nameMock, expectedObject, expectedAttributes);
        verify(dirContextMock).close();
	}

    @Test
	public void testRebind_String() throws Exception {
		expectGetReadWriteContext();

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();

		tested.rebind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);

        verify(dirContextMock).rebind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);
        verify(dirContextMock).close();
	}

    @Test
	public void testUnbind_NamingException() throws Exception {
		expectGetReadWriteContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        doThrow(ne).when(dirContextMock).unbind(nameMock);

		try {
			tested.unbind(nameMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testExecuteReadOnly() throws Exception {
		expectGetReadOnlyContext();

		Object object = new Object();
		when(contextExecutorMock.executeWithContext(dirContextMock)).thenReturn(object);

		Object result = tested.executeReadOnly(contextExecutorMock);

        verify(dirContextMock).close();

		assertSame(object, result);
	}

    @Test
	public void testExecuteReadOnly_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		when(contextExecutorMock.executeWithContext(dirContextMock)).thenThrow(ne);

		try {
			tested.executeReadOnly(contextExecutorMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testExecuteReadWrite() throws Exception {
		expectGetReadWriteContext();

		Object object = new Object();
		when(contextExecutorMock.executeWithContext(dirContextMock)).thenReturn(object);

		Object result = tested.executeReadWrite(contextExecutorMock);

        verify(dirContextMock).close();

        assertSame(object, result);
	}

    @Test
	public void testExecuteReadWrite_NamingException() throws Exception {
		expectGetReadWriteContext();

		javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
		when(contextExecutorMock.executeWithContext(dirContextMock)).thenThrow(ne);

		try {
			tested.executeReadWrite(contextExecutorMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch_DirContextProcessor() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult(null, null, null);

		when(searchExecutorMock.executeSearch(dirContextMock)).thenReturn(namingEnumerationMock);

		when(namingEnumerationMock.hasMore()).thenReturn(true, false);
		when(namingEnumerationMock.next()).thenReturn(searchResult);

		tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(handlerMock).handleNameClassPair(searchResult);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch_DirContextProcessor_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		when(searchExecutorMock.executeSearch(dirContextMock)).thenThrow(ne);

		try {
			tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertTrue(true);
		}

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch() throws Exception {
		expectGetReadOnlyContext();

		SearchResult searchResult = new SearchResult(null, null, null);

		when(searchExecutorMock.executeSearch(dirContextMock)).thenReturn(namingEnumerationMock);

		when(namingEnumerationMock.hasMore()).thenReturn(true, false);
		when(namingEnumerationMock.next()).thenReturn(searchResult);

		tested.search(searchExecutorMock, handlerMock);

        verify(handlerMock).handleNameClassPair(searchResult);
        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch_NamingException() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		when(searchExecutorMock.executeSearch(dirContextMock)).thenThrow(ne);

		try {
			tested.search(searchExecutorMock, handlerMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch_NamingException_NamingEnumeration() throws Exception {
		expectGetReadOnlyContext();

		when(searchExecutorMock.executeSearch(dirContextMock)).thenReturn(namingEnumerationMock);

		javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
		when(namingEnumerationMock.hasMore()).thenThrow(ne);

		try {
			tested.search(searchExecutorMock, handlerMock);
			fail("LimitExceededException expected");
		}
		catch (LimitExceededException expected) {
			assertTrue(true);
		}

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
	}

    @Test
	public void testDoSearch_NameNotFoundException() throws Exception {
		expectGetReadOnlyContext();

		when(searchExecutorMock.executeSearch(dirContextMock)).thenThrow(new javax.naming.NameNotFoundException());

		try {
			tested.search(searchExecutorMock, handlerMock);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_PartialResult_IgnoreNotSet() throws Exception {
		expectGetReadOnlyContext();

		javax.naming.PartialResultException ex = new javax.naming.PartialResultException();
		when(searchExecutorMock.executeSearch(dirContextMock)).thenThrow(ex);

		try {
			tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);
			fail("PartialResultException expected");
		}
		catch (PartialResultException expected) {
			assertTrue(true);
		}

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(dirContextMock).close();
	}

    @Test
	public void testSearch_PartialResult_IgnoreSet() throws Exception {
		tested.setIgnorePartialResultException(true);

		expectGetReadOnlyContext();

		when(searchExecutorMock.executeSearch(dirContextMock)).thenThrow(new javax.naming.PartialResultException());

		tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);

        verify(dirContextProcessorMock).preProcess(dirContextMock);
        verify(dirContextProcessorMock).postProcess(dirContextMock);
        verify(dirContextMock).close();
	}

    @Test
	public void testLookupContextWithName() {
		final DirContextAdapter expectedResult = new DirContextAdapter();

        final LdapName expectedName = LdapUtils.emptyLdapName();
        LdapTemplate tested = new LdapTemplate() {
			public Object lookup(Name dn) {
				assertSame(dn, dn);
				return expectedResult;
			}
		};

		DirContextOperations result = tested.lookupContext(expectedName);
		assertSame(expectedResult, result);

	}

    @Test
	public void testLookupContextWithString() {
		final DirContextAdapter expectedResult = new DirContextAdapter();
		final String expectedName = "cn=John Doe";

		LdapTemplate tested = new LdapTemplate() {
			public Object lookup(String dn) {
				assertSame(expectedName, dn);
				return expectedResult;
			}
		};

		DirContextOperations result = tested.lookupContext(expectedName);
		assertSame(expectedResult, result);
	}

    @Test
	public void testModifyAttributesWithDirContextOperations() throws Exception {
		final ModificationItem[] expectedModifications = new ModificationItem[0];

        final LdapName epectedDn = LdapUtils.emptyLdapName();
        when(dirContextOperationsMock.getDn()).thenReturn(epectedDn);
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(true);
		when(dirContextOperationsMock.getModificationItems()).thenReturn(expectedModifications);

		LdapTemplate tested = new LdapTemplate() {
			public void modifyAttributes(Name dn, ModificationItem[] mods) {
				assertSame(epectedDn, dn);
				assertSame(expectedModifications, mods);
			}
		};

		tested.modifyAttributes(dirContextOperationsMock);
	}

    @Test
	public void testModifyAttributesWithDirContextOperationsNotInitializedDn() throws Exception {

		when(dirContextOperationsMock.getDn()).thenReturn(LdapUtils.emptyLdapName());
		when(dirContextOperationsMock.isUpdateMode()).thenReturn(false);

		LdapTemplate tested = new LdapTemplate() {
			public void modifyAttributes(Name dn, ModificationItem[] mods) {
				fail("The call to the base modifyAttributes should not have occured.");
			}
		};

		try {
			tested.modifyAttributes(dirContextOperationsMock);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertTrue(true);
		}
	}

    @Test
	public void testModifyAttributesWithDirContextOperationsNotInitializedInUpdateMode() throws Exception {
		when(dirContextOperationsMock.getDn()).thenReturn(null);

		LdapTemplate tested = new LdapTemplate() {
			public void modifyAttributes(Name dn, ModificationItem[] mods) {
				fail("The call to the base modifyAttributes should not have occured.");
			}
		};

		try {
			tested.modifyAttributes(dirContextOperationsMock);
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertTrue(true);
		}
	}

    @Test
	public void testSearchForObject() throws Exception {
		expectGetReadOnlyContext();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		Object result = tested.searchForObject(nameMock, "(ou=somevalue)", contextMapperMock);

        verify(dirContextMock).close();

        assertNotNull(result);
		assertSame(expectedResult, result);
	}

    @Test
	public void testSearchForObjectWithMultipleResults() throws Exception {
		expectGetReadOnlyContext();

		SearchControls controls = searchControlsRecursive();

		Object expectedObject = new Object();
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		when(dirContextMock.search(
                eq(nameMock),
                eq("(ou=somevalue)"),
                argThat(new SearchControlsMatcher(controls)))).thenReturn(namingEnumerationMock);

		when(namingEnumerationMock.hasMore()).thenReturn(true, true, false);
		when(namingEnumerationMock.next()).thenReturn(searchResult, searchResult);

		Object expectedResult = expectedObject;
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);
		when(contextMapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

		try {
			tested.searchForObject(nameMock, "(ou=somevalue)", contextMapperMock);
			fail("IncorrectResultSizeDataAccessException expected");
		}
		catch (IncorrectResultSizeDataAccessException expected) {
			assertTrue(true);
		}

        verify(namingEnumerationMock).close();
        verify(dirContextMock).close();
	}

    @Test
	public void testSearchForObjectWithNoResults() throws Exception {
		expectGetReadOnlyContext();

		noSearchResults(searchControlsRecursive());

		try {
			tested.searchForObject(nameMock, "(ou=somevalue)", contextMapperMock);
			fail("EmptyResultDataAccessException expected");
		}
		catch (EmptyResultDataAccessException expected) {
			assertTrue(true);
		}

        verify(dirContextMock).close();
    }

    @Test
	public void testAuthenticateWithSingleUserFoundShouldBeSuccessful() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
                LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		when(contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
                .thenReturn(authenticatedContextMock);
		entryContextCallbackMock.executeWithContext(authenticatedContextMock, new LdapEntryIdentification(
                LdapUtils.newLdapName("cn=john doe,dc=jayway,dc=se"), LdapUtils.newLdapName("cn=john doe")));

		boolean result = tested.authenticate(nameMock, "(ou=somevalue)", "password", entryContextCallbackMock);

        verify(authenticatedContextMock).close();
        verify(dirContextMock).close();

		assertTrue(result);
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
			tested.authenticate(nameMock, "(ou=somevalue)", "password", entryContextCallbackMock);
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

		boolean result = tested.authenticate(nameMock, "(ou=somevalue)", "password", entryContextCallbackMock);

        verify(dirContextMock).close();

        assertFalse(result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAuthenticateQueryPasswordMapperWhenNoUserWasFoundShouldThrowEmptyResult() throws Exception {

		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		 when(dirContextMock.search(
					any(Name.class),
					any(String.class),
					any(SearchControls.class))).thenReturn(namingEnumerationMock);

			when(namingEnumerationMock.hasMore()).thenReturn(false);

		try {
			tested.authenticate(query, "", authContextMapperMock);
			fail("Expected Exception");
		}catch(EmptyResultDataAccessException success) {}
		verify(dirContextMock).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAuthenticateQueryPasswordWhenNoUserWasFoundShouldThrowEmptyResult() throws Exception {

		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		 when(dirContextMock.search(
					any(Name.class),
					any(String.class),
					any(SearchControls.class))).thenReturn(namingEnumerationMock);

			when(namingEnumerationMock.hasMore()).thenReturn(false);

		try {
			tested.authenticate(query, "");
			fail("Expected Exception");
		}catch(EmptyResultDataAccessException success) {}
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

		boolean result = tested.authenticate(nameMock, "(ou=somevalue)", "password", entryContextCallbackMock);

        verify(dirContextMock).close();

        assertFalse(result);
	}

    @Test
	public void testAuthenticateWithErrorInCallbackShouldFail() throws Exception {
		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);

		Object expectedObject = new DirContextAdapter(new BasicAttributes(), LdapUtils.newLdapName("cn=john doe"),
				LdapUtils.newLdapName("dc=jayway, dc=se"));
		SearchResult searchResult = new SearchResult("", expectedObject, new BasicAttributes());

		singleSearchResult(searchControlsRecursive(), searchResult);

		when(contextSourceMock.getContext("cn=john doe,dc=jayway,dc=se", "password"))
                .thenReturn(authenticatedContextMock);
        doThrow(new UncategorizedLdapException("Authentication failed")).when(entryContextCallbackMock)
                .executeWithContext(authenticatedContextMock,
                        new LdapEntryIdentification(
                                LdapUtils.newLdapName("cn=john doe,dc=jayway,dc=se"), LdapUtils.newLdapName("cn=john doe")));

		boolean result = tested.authenticate(nameMock, "(ou=somevalue)", "password", entryContextCallbackMock);

        verify(authenticatedContextMock).close();
        verify(dirContextMock).close();

        assertFalse(result);
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

	private void setupSearchResults(SearchControls controls, SearchResult[] searchResults) throws Exception {
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
                eq(DEFAULT_BASE_STRING),
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

    private static class SearchControlsMatcher extends BaseMatcher<SearchControls> {
        private final SearchControls controls;

        public SearchControlsMatcher(SearchControls controls) {
            this.controls = controls;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof SearchControls) {
                SearchControls s1 = (SearchControls) item;

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

        @Override
        public void describeTo(Description description) {
            description.appendText("SearchControls matches");
        }
    }
}
