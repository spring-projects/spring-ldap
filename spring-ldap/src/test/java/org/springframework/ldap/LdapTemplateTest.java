/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap;

import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.springframework.ldap.support.DistinguishedName;

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

    private MockControl exceptionTranslatorControl;

    private NamingExceptionTranslator exceptionTranslatorMock;

    private MockControl contextExecutorControl;

    private ContextExecutor contextExecutorMock;

    private MockControl searchExecutorControl;

    private SearchExecutor searchExecutorMock;

    private LdapTemplate tested;

    private MockControl dirContextProcessorControl;

    private DirContextProcessor dirContextProcessorMock;

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

        exceptionTranslatorControl = MockControl
                .createControl(NamingExceptionTranslator.class);
        exceptionTranslatorMock = (NamingExceptionTranslator) exceptionTranslatorControl
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

        tested = new LdapTemplate(contextSourceMock);
        tested.setExceptionTranslator(exceptionTranslatorMock);
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

        exceptionTranslatorControl = null;
        exceptionTranslatorMock = null;

        contextExecutorControl = null;
        contextExecutorMock = null;

        searchExecutorControl = null;
        searchExecutorMock = null;

        dirContextProcessorControl = null;
        dirContextProcessorMock = null;
    }

    protected void replay() {
        contextSourceControl.replay();
        dirContextControl.replay();
        namingEnumerationControl.replay();
        nameControl.replay();
        handlerControl.replay();
        contextMapperControl.replay();
        attributesMapperControl.replay();
        exceptionTranslatorControl.replay();
        contextExecutorControl.replay();
        searchExecutorControl.replay();
        dirContextProcessorControl.replay();
    }

    protected void verify() {
        contextSourceControl.verify();
        dirContextControl.verify();
        namingEnumerationControl.verify();
        nameControl.verify();
        handlerControl.verify();
        contextMapperControl.verify();
        attributesMapperControl.verify();
        exceptionTranslatorControl.verify();
        contextExecutorControl.verify();
        searchExecutorControl.verify();
        dirContextProcessorControl.verify();
    }

    private void expectGetReadWriteContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), dirContextMock);
    }

    private void expectGetReadOnlyContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), dirContextMock);
    }

    public void testSearch_CallbackHandler() throws NamingException {
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

    public void testSearch_StringBase_CallbackHandler() throws NamingException {
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
            SearchResult searchResult) throws NamingException {
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

    public void testSearch_CallbackHandler_Defaults() throws NamingException {
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

    public void testSearch_String_CallbackHandler_Defaults()
            throws NamingException {
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
            SearchResult searchResult) throws NamingException {
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

    public void testSearch_NameNotFoundException() throws NamingException {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        dirContextControl.expectAndThrow(dirContextMock.search(nameMock,
                "(ou=somevalue)", controls), new NameNotFoundException());

        dirContextMock.close();

        replay();

        tested.search(nameMock, "(ou=somevalue)", handlerMock);

        verify();
    }

    public void testSearch_NamingException() throws NamingException {
        expectGetReadOnlyContext();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(false);

        dirContextControl.setDefaultMatcher(new SearchControlsMatcher());
        NamingException ne = new NamingException();
        dirContextControl.expectAndThrow(dirContextMock.search(nameMock,
                "(ou=somevalue)", controls), ne);

        dirContextMock.close();

        EntryNotFoundException expectedException = new EntryNotFoundException(
                "dummy");
        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), expectedException);

        replay();

        try {
            tested.search(nameMock, "(ou=somevalue)", handlerMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertSame(expectedException, expected);
        }

        verify();
    }

    public void testSearch_CallbackHandler_DirContextProcessor()
            throws NamingException {
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
            throws NamingException {
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
            throws NamingException {
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
            throws NamingException {
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

        List list = tested.search(nameMock, "(ou=somevalue)",
                controls, contextMapperMock, dirContextProcessorMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(expectedResult, list.get(0));
    }

    public void testSearch_AttributesMapper_ReturningAttrs()
            throws NamingException {
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
            throws NamingException {
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

    public void testSearch_AttributesMapper() throws NamingException {
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

    public void testSearch_String_AttributesMapper() throws NamingException {
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

    public void testSearch_AttributesMapper_Default() throws NamingException {
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

    public void testSearch_String_AttributesMapper_Default()
            throws NamingException {
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

    public void testSearch_ContextMapper() throws NamingException {
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

    public void testSearch_ContextMapper_ReturningAttrs()
            throws NamingException {
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
            throws NamingException {
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

    public void testSearch_String_ContextMapper() throws NamingException {
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

    public void testSearch_ContextMapper_Default() throws NamingException {
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

    public void testSearch_String_ContextMapper_Default()
            throws NamingException {
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
            throws NamingException {
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
            throws NamingException {
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
        NamingException ne = new NamingException();
        dirContextControl.setThrowable(ne);

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        dirContextMock.close();

        replay();

        try {
            tested.modifyAttributes(nameMock, mods);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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
        NamingException ne = new NamingException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.bind(nameMock, expectedObject, expectedAttributes);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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

    public void testRebind() throws NamingException {
        expectGetReadWriteContext();

        Object expectedObject = new Object();
        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextMock.rebind(nameMock, expectedObject, expectedAttributes);

        dirContextMock.close();

        replay();

        tested.rebind(nameMock, expectedObject, expectedAttributes);

        verify();
    }

    public void testRebind_String() throws NamingException {
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
        NamingException ne = new NamingException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.unbind(nameMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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

        NamingException ne = new NamingException();
        contextExecutorControl.expectAndThrow(contextExecutorMock
                .executeWithContext(dirContextMock), ne);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.executeReadOnly(contextExecutorMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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

        NamingException ne = new NamingException();
        contextExecutorControl.expectAndThrow(contextExecutorMock
                .executeWithContext(dirContextMock), ne);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.executeReadWrite(contextExecutorMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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

        NamingException ne = new NamingException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ne);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock,
                    dirContextProcessorMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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

        NamingException ne = new NamingException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ne);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch_NamingException_NamingEnumeration()
            throws Exception {
        expectGetReadOnlyContext();

        searchExecutorControl.expectAndReturn(searchExecutorMock
                .executeSearch(dirContextMock), namingEnumerationMock);

        NamingException ne = new NamingException();
        namingEnumerationControl.expectAndThrow(
                namingEnumerationMock.hasMore(), ne);
        namingEnumerationMock.close();

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testDoSearch_NameNotFoundException() throws Exception {
        expectGetReadOnlyContext();

        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), new NameNotFoundException());
        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock);

        verify();
    }

    public void testSearch_PartialResult_IgnoreNotSet() throws NamingException {
        expectGetReadOnlyContext();

        PartialResultException ex = new PartialResultException();
        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), ex);
        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ex), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.search(searchExecutorMock, handlerMock);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testSearch_PartialResult_IgnoreSet() throws NamingException {
        tested.setIgnorePartialResultException(true);

        expectGetReadOnlyContext();

        searchExecutorControl.expectAndThrow(searchExecutorMock
                .executeSearch(dirContextMock), new PartialResultException());
        dirContextMock.close();

        replay();

        tested.search(searchExecutorMock, handlerMock);

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
