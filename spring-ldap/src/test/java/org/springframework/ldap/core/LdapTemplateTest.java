/*
 * Copyright 2005-2007 the original author or authors.
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

import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.PartialResultException;

public class LdapTemplateTest extends TestCase {

    private static final String DEFAULT_BASE_STRING = "o=example.com";

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl attributesMapperControl;

    private AttributesMapper attributesMapperMock;

    private MockControl namingEnumerationControl;

    private NamingEnumeration namingEnumerationMock;

    private MockControl nameControl;

    private Name nameMock;

    private MockControl handlerControl;

    private NameClassPairCallbackHandler handlerMock;

    private MockControl contextMapperControl;

    private ContextMapper contextMapperMock;

    private MockControl contextExecutorControl;

    private ContextExecutor contextExecutorMock;

    private MockControl searchExecutorControl;

    private SearchExecutor searchExecutorMock;

    private LdapTemplate tested;

    private MockControl dirContextProcessorControl;

    private DirContextProcessor dirContextProcessorMock;

    private MockControl dirContextOperationsConrol;

    private DirContextOperations dirContextOperationsMock;

    protected void setUp() throws Exception {
        super.setUp();

        // Setup ContextSource mock
        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        // Setup LdapContext mock
        dirContextControl = MockControl.createControl(LdapContext.class);
        dirContextMock = (LdapContext) dirContextControl.getMock();

        // Setup NamingEnumeration mock
        namingEnumerationControl = MockControl
                .createControl(NamingEnumeration.class);
        namingEnumerationMock = (NamingEnumeration) namingEnumerationControl
                .getMock();

        // Setup Name mock
        nameControl = MockControl.createControl(Name.class);
        nameMock = (Name) nameControl.getMock();

        // Setup Handler mock
        handlerControl = MockControl
                .createControl(NameClassPairCallbackHandler.class);
        handlerMock = (NameClassPairCallbackHandler) handlerControl.getMock();

        contextMapperControl = MockControl.createControl(ContextMapper.class);
        contextMapperMock = (ContextMapper) contextMapperControl.getMock();

        attributesMapperControl = MockControl
                .createControl(AttributesMapper.class);
        attributesMapperMock = (AttributesMapper) attributesMapperControl
                .getMock();

        contextExecutorControl = MockControl
                .createControl(ContextExecutor.class);
        contextExecutorMock = (ContextExecutor) contextExecutorControl
                .getMock();

        searchExecutorControl = MockControl.createControl(SearchExecutor.class);
        searchExecutorMock = (SearchExecutor) searchExecutorControl.getMock();

        dirContextProcessorControl = MockControl
                .createControl(DirContextProcessor.class);
        dirContextProcessorMock = (DirContextProcessor) dirContextProcessorControl
                .getMock();

        dirContextOperationsConrol = MockControl
                .createControl(DirContextOperations.class);
        dirContextOperationsMock = (DirContextOperations) dirContextOperationsConrol
                .getMock();

        tested = new LdapTemplate(contextSourceMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextSourceControl = null;
        contextSourceMock = null;

        dirContextControl = null;
        dirContextMock = null;

        namingEnumerationControl = null;
        namingEnumerationMock = null;

        nameControl = null;
        nameMock = null;

        handlerControl = null;
        handlerMock = null;

        contextMapperControl = null;
        contextMapperMock = null;

        attributesMapperControl = null;
        attributesMapperMock = null;

        contextExecutorControl = null;
        contextExecutorMock = null;

        searchExecutorControl = null;
        searchExecutorMock = null;

        dirContextProcessorControl = null;
        dirContextProcessorMock = null;

        dirContextOperationsConrol = null;
        dirContextOperationsMock = null;

    }

    protected void replay() {
        contextSourceControl.replay();
        dirContextControl.replay();
        namingEnumerationControl.replay();
        nameControl.replay();
        handlerControl.replay();
        contextMapperControl.replay();
        attributesMapperControl.replay();
        contextExecutorControl.replay();
        searchExecutorControl.replay();
        dirContextProcessorControl.replay();
        dirContextOperationsConrol.replay();
    }

    protected void verify() {
        contextSourceControl.verify();
        dirContextControl.verify();
        namingEnumerationControl.verify();
        nameControl.verify();
        handlerControl.verify();
        contextMapperControl.verify();
        attributesMapperControl.verify();
        contextExecutorControl.verify();
        searchExecutorControl.verify();
        dirContextProcessorControl.verify();
        dirContextOperationsConrol.verify();
    }

    private void expectGetReadWriteContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), dirContextMock);
    }

    private void expectGetReadOnlyContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), dirContextMock);
    }

    public void testSearch_CallbackHandler() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextMock.close();

        replay();

        tested.search(nameMock, "(ou=somevalue)", 1, true, handlerMock);

        verify();
    }

    public void testSearch_StringBase_CallbackHandler() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextMock.close();

        replay();

        tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1, true,
                handlerMock);

        verify();
    }

    private void setupStringSearchAndNamingEnumeration(SearchControls controls,
            SearchResult searchResult) throws Exception {
        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        dirContextControl.expectAndReturn(dirContextMock.search(
                DEFAULT_BASE_STRING, "(ou=somevalue)", controls),
                namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                searchResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();
    }

    public void testSearch_CallbackHandler_Defaults() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextMock.close();

        replay();

        tested.search(nameMock, "(ou=somevalue)", handlerMock);

        verify();
    }

    public void testSearch_String_CallbackHandler_Defaults() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextMock.close();

        replay();

        tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", handlerMock);

        verify();
    }

    private void setupSearchAndNamingEnumeration(SearchControls controls,
            SearchResult searchResult) throws Exception {
        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        dirContextControl.expectAndReturn(dirContextMock.search(nameMock,
                "(ou=somevalue)", controls), namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                searchResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();
    }

    public void testSearch_NameNotFoundException() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.expectAndThrow(dirContextMock.search(nameMock,
                "(ou=somevalue)", controls), ne);

        dirContextMock.close();

        replay();

        tested.search(nameMock, "(ou=somevalue)", handlerMock);

        verify();
    }

    public void testSearch_NamingException() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        dirContextControl.expectAndThrow(dirContextMock.search(nameMock,
                "(ou=somevalue)", controls), ne);

        dirContextMock.close();

        replay();

        try {
            tested.search(nameMock, "(ou=somevalue)", handlerMock);
            fail("LimitExceededException expected");
        } catch (LimitExceededException expected) {
            // expected
        }

        verify();
    }

    public void testSearch_CallbackHandler_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        dirContextProcessorMock.preProcess(dirContextMock);

        setupSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextProcessorMock.postProcess(dirContextMock);

        dirContextMock.close();

        replay();

        tested.search(nameMock, "(ou=somevalue)", controls, handlerMock,
                dirContextProcessorMock);

        verify();
    }

    public void testSearch_String_CallbackHandler_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        SearchResult searchResult = new SearchResult("", new Object(),
                new BasicAttributes());

        dirContextProcessorMock.preProcess(dirContextMock);

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        handlerMock.handleNameClassPair(searchResult);

        dirContextProcessorMock.postProcess(dirContextMock);

        dirContextMock.close();

        replay();

        tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", controls,
                handlerMock, dirContextProcessorMock);

        verify();
    }

    public void testSearch_String_AttributesMapper_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        dirContextProcessorMock.preProcess(dirContextMock);
        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                controls, attributesMapperMock, dirContextProcessorMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_Name_AttributesMapper_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        dirContextProcessorMock.preProcess(dirContextMock);
        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", controls,
                attributesMapperMock, dirContextProcessorMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_SearchControls_ContextMapper_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        dirContextProcessorMock.preProcess(dirContextMock);
        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                controls, contextMapperMock, dirContextProcessorMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_Name_SearchControls_ContextMapper_DirContextProcessor()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        dirContextProcessorMock.preProcess(dirContextMock);
        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", controls,
                contextMapperMock, dirContextProcessorMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_AttributesMapper_ReturningAttrs() throws Exception {
        expectGetReadOnlyContext();

        String[] attrs = new String[0];
        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);
        controls.setReturningAttributes(attrs);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", 1, attrs,
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_AttributesMapper_ReturningAttrs()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attrs = new String[0];
        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);
        controls.setReturningAttributes(attrs);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1,
                attrs, attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", 1,
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1,
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_AttributesMapper_Default() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)",
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_AttributesMapper_Default() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", 1,
                contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_ContextMapper_ReturningAttrs() throws Exception {
        expectGetReadOnlyContext();

        String[] attrs = new String[0];

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);
        controls.setReturningAttributes(attrs);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", 1, attrs,
                contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_ContextMapper_ReturningAttrs()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attrs = new String[0];

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);
        controls.setReturningAttributes(attrs);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1,
                attrs, contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)", 1,
                contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_ContextMapper_Default() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested
                .search(nameMock, "(ou=somevalue)", contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_ContextMapper_Default() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_SearchControls_ContextMapper()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                controls, contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_SearchControls_ContextMapper_ReturningObjFlagNotSet()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        SearchControls expectedControls = new SearchControls();
        expectedControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        expectedControls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupStringSearchAndNamingEnumeration(expectedControls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                controls, contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_Name_SearchControls_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        Object expectedObject = new Object();
        SearchResult searchResult = new SearchResult("", expectedObject,
                new BasicAttributes());

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = expectedObject;
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expectedObject), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", controls,
                contextMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_String_SearchControls_AttributesMapper()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupStringSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(DEFAULT_BASE_STRING, "(ou=somevalue)",
                controls, attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_Name_SearchControls_AttributesMapper()
            throws Exception {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(1);
        controls.setReturningObjFlag(false);

        BasicAttributes expectedAttributes = new BasicAttributes();
        SearchResult searchResult = new SearchResult("", null,
                expectedAttributes);

        setupSearchAndNamingEnumeration(controls, searchResult);

        Object expectedResult = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expectedResult);

        dirContextMock.close();

        replay();

        List list = tested.search(nameMock, "(ou=somevalue)", controls,
                attributesMapperMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testModifyAttributes() throws Exception {
        expectGetReadWriteContext();

        ModificationItem[] mods = new ModificationItem[0];
        dirContextMock.modifyAttributes(nameMock, mods);

        dirContextMock.close();

        replay();

        tested.modifyAttributes(nameMock, mods);

        verify();
    }

    public void testModifyAttributes_String() throws Exception {
        expectGetReadWriteContext();

        ModificationItem[] mods = new ModificationItem[0];
        dirContextMock.modifyAttributes(DEFAULT_BASE_STRING, mods);

        dirContextMock.close();

        replay();

        tested.modifyAttributes(DEFAULT_BASE_STRING, mods);

        verify();
    }

    public void testModifyAttributes_NamingException() throws Exception {
        expectGetReadWriteContext();

        ModificationItem[] mods = new ModificationItem[0];
        dirContextMock.modifyAttributes(nameMock, mods);
        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        dirContextControl.setThrowable(ne);

        dirContextMock.close();

        replay();

        try {
            tested.modifyAttributes(nameMock, mods);
            fail("LimitExceededException expected");
        } catch (LimitExceededException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testBind() throws Exception {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.bind(nameMock, expectedObject, expectedAttributes);
        dirContextMock.close();

        replay();

        tested.bind(nameMock, expectedObject, expectedAttributes);

        verify();

    }

    public void testBind_String() throws Exception {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.bind(DEFAULT_BASE_STRING, expectedObject,
                expectedAttributes);
        dirContextMock.close();

        replay();

        tested.bind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);

        verify();

    }

    public void testBind_NamingException() throws Exception {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.bind(nameMock, expectedObject, expectedAttributes);
        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        replay();

        try {
            tested.bind(nameMock, expectedObject, expectedAttributes);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();

    }

    public void testUnbind() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.unbind(nameMock);
        dirContextMock.close();
        replay();

        tested.unbind(nameMock);

        verify();
    }

    public void testUnbind_String() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.unbind(DEFAULT_BASE_STRING);
        dirContextMock.close();
        replay();

        tested.unbind(DEFAULT_BASE_STRING);

        verify();
    }

    public void testUnbindRecursive() throws Exception {
        expectGetReadWriteContext();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        Binding binding = new Binding("cn=Some name", null);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                binding);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        DistinguishedName listDn = new DistinguishedName(DEFAULT_BASE_STRING);
        dirContextMock.listBindings(listDn);
        dirContextControl.setReturnValue(namingEnumerationMock);
        DistinguishedName subListDn = new DistinguishedName(
                "cn=Some name, o=example.com");
        dirContextMock.listBindings(subListDn);
        dirContextControl.setReturnValue(namingEnumerationMock);

        dirContextMock.unbind(subListDn);
        dirContextMock.unbind(listDn);
        dirContextMock.close();

        // Caused by creating a DistinguishedName from a Name
        nameControl.expectAndReturn(nameMock.size(), 1, 2);
        nameControl.expectAndReturn(nameMock.get(0), "o=example.com");

        replay();

        tested.unbind(nameMock, true);

        verify();
    }

    public void testUnbindRecursive_String() throws Exception {
        expectGetReadWriteContext();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        Binding binding = new Binding("cn=Some name", null);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                binding);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        DistinguishedName listDn = new DistinguishedName(DEFAULT_BASE_STRING);
        dirContextMock.listBindings(listDn);
        dirContextControl.setReturnValue(namingEnumerationMock);
        DistinguishedName subListDn = new DistinguishedName(
                "cn=Some name, o=example.com");
        dirContextMock.listBindings(subListDn);
        dirContextControl.setReturnValue(namingEnumerationMock);

        dirContextMock.unbind(subListDn);
        dirContextMock.unbind(listDn);
        dirContextMock.close();
        replay();

        tested.unbind(DEFAULT_BASE_STRING, true);

        verify();
    }

    public void testRebind() throws Exception {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.rebind(nameMock, expectedObject, expectedAttributes);

        dirContextMock.close();

        replay();

        tested.rebind(nameMock, expectedObject, expectedAttributes);

        verify();
    }

    public void testRebind_String() throws Exception {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.rebind(DEFAULT_BASE_STRING, expectedObject,
                expectedAttributes);

        dirContextMock.close();

        replay();

        tested.rebind(DEFAULT_BASE_STRING, expectedObject, expectedAttributes);

        verify();
    }

    public void testUnbind_NamingException() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.unbind(nameMock);
        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        replay();

        try {
            tested.unbind(nameMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testExecuteReadOnly() throws Exception {
        expectGetReadOnlyContext();

        Object object = new Object();
        contextExecutorControl.expectAndReturn(contextExecutorMock
                .executeWithContext(dirContextMock), object);

        dirContextMock.close();

        replay();

        Object result = tested.executeReadOnly(contextExecutorMock);

        verify();

        assertSame(object, result);
    }

    public void testExecuteReadOnly_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        contextExecutorControl.expectAndThrow(contextExecutorMock
                .executeWithContext(dirContextMock), ne);

        dirContextMock.close();

        replay();

        try {
            tested.executeReadOnly(contextExecutorMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testExecuteReadWrite() throws Exception {
        expectGetReadWriteContext();

        Object object = new Object();
        contextExecutorControl.expectAndReturn(contextExecutorMock
                .executeWithContext(dirContextMock), object);

        dirContextMock.close();

        replay();

        Object result = tested.executeReadWrite(contextExecutorMock);

        verify();

        assertSame(object, result);
    }

    public void testExecuteReadWrite_NamingException() throws Exception {
        expectGetReadWriteContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        contextExecutorControl.expectAndThrow(contextExecutorMock
                .executeWithContext(dirContextMock), ne);

        dirContextMock.close();

        replay();

        try {
            tested.executeReadWrite(contextExecutorMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch_DirContextProcessor() throws Exception {
        expectGetReadOnlyContext();

        SearchResult searchResult = new SearchResult(null, null, null);

        dirContextProcessorMock.preProcess(dirContextMock);

        searchExecutorControl.expectAndReturn(searchExecutorMock
                .executeSearch(dirContextMock), namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                searchResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        handlerMock.handleNameClassPair(searchResult);

        dirContextProcessorMock.postProcess(dirContextMock);

        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);

        verify();
    }

    public void testDoSearch_DirContextProcessor_NamingException()
            throws Exception {
        expectGetReadOnlyContext();

        dirContextProcessorMock.preProcess(dirContextMock);

        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ne);

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock,
                    dirContextProcessorMock);
            fail("LimitExceededException expected");
        } catch (LimitExceededException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch() throws Exception {
        expectGetReadOnlyContext();

        SearchResult searchResult = new SearchResult(null, null, null);

        searchExecutorControl.expectAndReturn(searchExecutorMock
                .executeSearch(dirContextMock), namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                searchResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();

        handlerMock.handleNameClassPair(searchResult);

        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock);

        verify();
    }

    public void testDoSearch_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ne);

        dirContextMock.close();

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock);
            fail("LimitExceededException expected");
        } catch (LimitExceededException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch_NamingException_NamingEnumeration()
            throws Exception {
        expectGetReadOnlyContext();

        searchExecutorControl.expectAndReturn(searchExecutorMock
                .executeSearch(dirContextMock), namingEnumerationMock);

        javax.naming.LimitExceededException ne = new javax.naming.LimitExceededException();
        namingEnumerationControl.expectAndThrow(
                namingEnumerationMock.hasMore(), ne);
        namingEnumerationMock.close();

        dirContextMock.close();

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock);
            fail("LimitExceededException expected");
        } catch (LimitExceededException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch_NameNotFoundException() throws Exception {
        expectGetReadOnlyContext();

        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock),
                new javax.naming.NameNotFoundException());
        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock);

        verify();
    }

    public void testSearch_PartialResult_IgnoreNotSet() throws Exception {
        expectGetReadOnlyContext();

        dirContextProcessorMock.preProcess(dirContextMock);

        javax.naming.PartialResultException ex = new javax.naming.PartialResultException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ex);
        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock,
                    dirContextProcessorMock);
            fail("PartialResultException expected");
        } catch (PartialResultException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testSearch_PartialResult_IgnoreSet() throws Exception {
        tested.setIgnorePartialResultException(true);

        expectGetReadOnlyContext();

        dirContextProcessorMock.preProcess(dirContextMock);

        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock),
                new javax.naming.PartialResultException());

        dirContextProcessorMock.postProcess(dirContextMock);
        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock, dirContextProcessorMock);

        verify();
    }

    public void testLookupContextWithName() {
        final DirContextAdapter expectedResult = new DirContextAdapter();

        LdapTemplate tested = new LdapTemplate() {
            public Object lookup(Name dn) {
                assertSame(DistinguishedName.EMPTY_PATH, dn);
                return expectedResult;
            }
        };

        DirContextOperations result = tested
                .lookupContext(DistinguishedName.EMPTY_PATH);
        assertSame(expectedResult, result);

    }

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

    public void testModifyAttributesWithDirContextOperations() throws Exception {
        final ModificationItem[] expectedModifications = new ModificationItem[0];

        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .getDn(), DistinguishedName.EMPTY_PATH);
        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .isUpdateMode(), true);
        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .getModificationItems(), expectedModifications);

        LdapTemplate tested = new LdapTemplate() {
            public void modifyAttributes(Name dn, ModificationItem[] mods) {
                assertSame(DistinguishedName.EMPTY_PATH, dn);
                assertSame(expectedModifications, mods);
            }
        };

        replay();

        tested.modifyAttributes(dirContextOperationsMock);

        verify();
    }

    public void testModifyAttributesWithDirContextOperationsNotInitializedDn()
            throws Exception {

        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .getDn(), DistinguishedName.EMPTY_PATH);
        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .isUpdateMode(), false);

        LdapTemplate tested = new LdapTemplate() {
            public void modifyAttributes(Name dn, ModificationItem[] mods) {
                fail("The call to the base modifyAttributes should not have occured.");
            }
        };

        replay();

        try {
            tested.modifyAttributes(dirContextOperationsMock);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }
        verify();
    }

    public void testModifyAttributesWithDirContextOperationsNotInitializedInUpdateMode()
            throws Exception {

        dirContextOperationsConrol.expectAndReturn(dirContextOperationsMock
                .getDn(), null);

        LdapTemplate tested = new LdapTemplate() {
            public void modifyAttributes(Name dn, ModificationItem[] mods) {
                fail("The call to the base modifyAttributes should not have occured.");
            }
        };

        replay();

        try {
            tested.modifyAttributes(dirContextOperationsMock);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }
        verify();
    }

    /**
     * Needed to verify search control values.
     * 
     * @author Mattias Arthursson
     */
    private static class SearchControlsMatcher extends AbstractMatcher {
        protected boolean argumentMatches(Object arg0, Object arg1) {
            if (arg0 instanceof SearchControls
                    && arg1 instanceof SearchControls) {
                SearchControls s0 = (SearchControls) arg0;
                SearchControls s1 = (SearchControls) arg1;

                return s0.getSearchScope() == s1.getSearchScope()
                        && s0.getReturningObjFlag() == s1.getReturningObjFlag()
                        && s0.getDerefLinkFlag() == s1.getDerefLinkFlag()
                        && s0.getCountLimit() == s1.getCountLimit()
                        && s0.getTimeLimit() == s1.getTimeLimit()
                        && s0.getReturningAttributes() == s1
                                .getReturningAttributes();
            } else {
                return super.argumentMatches(arg0, arg1);
            }
        }
    }
}
