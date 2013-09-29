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

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        contextSourceMock = mock(ContextSource.class);

        // Setup LdapContext mock
        dirContextMock = mock(LdapContext.class);

        // Setup Name mock
        nameMock = mock(Name.class);
        contextMapperMock = mock(ContextMapper.class);
        attributesMapperMock = mock(AttributesMapper.class);
        odmMock = mock(ObjectDirectoryMapper.class);

        tested = new LdapTemplate(contextSourceMock);
        tested.setObjectDirectoryMapper(odmMock);
    }

    private void expectGetReadOnlyContext() {
        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock);
    }

    // Tests for lookup(name)

    @Test
    public void testLookup() throws Exception {
        expectGetReadOnlyContext();

        Object expected = new Object();
        when(dirContextMock.lookup(nameMock)).thenReturn(expected);

        Object actual = tested.lookup(nameMock);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    @Test
    public void testLookup_String() throws Exception {
        expectGetReadOnlyContext();

        Object expected = new Object();
        when(dirContextMock.lookup(DEFAULT_BASE_STRING)).thenReturn(expected);

        Object actual = tested.lookup(DEFAULT_BASE_STRING);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    @Test
    public void testLookup_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        when(dirContextMock.lookup(nameMock)).thenThrow(ne);

        try {
            tested.lookup(nameMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify(dirContextMock).close();
    }

    // Tests for lookup(name, AttributesMapper)

    @Test
    public void testLookup_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        BasicAttributes expectedAttributes = new BasicAttributes();
        when(dirContextMock.getAttributes(nameMock)).thenReturn(expectedAttributes);

        Object expected = new Object();
        when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

        Object actual = tested.lookup(nameMock, attributesMapperMock);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    @Test
    public void testLookup_String_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        BasicAttributes expectedAttributes = new BasicAttributes();
        when(dirContextMock.getAttributes(DEFAULT_BASE_STRING)).thenReturn(expectedAttributes);

        Object expected = new Object();
        when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

        Object actual = tested
                .lookup(DEFAULT_BASE_STRING, attributesMapperMock);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    @Test
    public void testLookup_AttributesMapper_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        when(dirContextMock.getAttributes(nameMock)).thenThrow(ne);

        try {
            tested.lookup(nameMock, attributesMapperMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify(dirContextMock).close();
    }

    // Tests for lookup(name, ContextMapper)

    @Test
    public void testLookup_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        Object transformed = new Object();
        Object expected = new Object();
        when(dirContextMock.lookup(nameMock)).thenReturn(expected);

        when(contextMapperMock.mapFromContext(expected)).thenReturn(transformed);

        Object actual = tested.lookup(nameMock, contextMapperMock);

        verify(dirContextMock).close();

        assertSame(transformed, actual);
    }

    @Test
    public void testFindByDn() throws NamingException {
        expectGetReadOnlyContext();

        Object transformed = new Object();
        Class<Object> expectedClass = Object.class;

        DirContextAdapter expectedContext = new DirContextAdapter();
        when(dirContextMock.lookup(nameMock)).thenReturn(expectedContext);
        when(odmMock.mapFromLdapDataEntry(expectedContext, expectedClass)).thenReturn(transformed);

        // Perform test
        Object result = tested.findByDn(nameMock, expectedClass);
        assertSame(transformed, result);

        verify(odmMock).manageClass(expectedClass);
    }



    @Test
    public void testLookup_String_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        Object transformed = new Object();
        Object expected = new Object();
        when(dirContextMock.lookup(DEFAULT_BASE_STRING)).thenReturn(expected);

        when(contextMapperMock.mapFromContext(expected)).thenReturn(transformed);

        Object actual = tested.lookup(DEFAULT_BASE_STRING, contextMapperMock);

        verify(dirContextMock).close();

        assertSame(transformed, actual);
    }

    @Test
    public void testLookup_ContextMapper_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        when(dirContextMock.lookup(nameMock)).thenThrow(ne);

        try {
            tested.lookup(nameMock, contextMapperMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify(dirContextMock).close();
    }

    // Tests for lookup(name, attributes, AttributesMapper)

    @Test
    public void testLookup_ReturnAttributes_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        when(dirContextMock.getAttributes(nameMock, attributeNames)).thenReturn(expectedAttributes);

        Object expected = new Object();
        when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

        Object actual = tested.lookup(nameMock, attributeNames,
                attributesMapperMock);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    @Test
    public void testLookup_String_ReturnAttributes_AttributesMapper()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        when(dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).thenReturn(expectedAttributes);

        Object expected = new Object();
        when(attributesMapperMock.mapFromAttributes(expectedAttributes)).thenReturn(expected);

        Object actual = tested.lookup(DEFAULT_BASE_STRING, attributeNames,
                attributesMapperMock);

        verify(dirContextMock).close();

        assertSame(expected, actual);
    }

    // Tests for lookup(name, attributes, ContextMapper)

    @Test
    public void testLookup_ReturnAttributes_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        LdapName name = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
        DirContextAdapter adapter = new DirContextAdapter(expectedAttributes,
                name);

        when(dirContextMock.getAttributes(name,attributeNames)).thenReturn(expectedAttributes);

        Object transformed = new Object();
        when(contextMapperMock.mapFromContext(adapter)).thenReturn(transformed);

        Object actual = tested.lookup(name, attributeNames, contextMapperMock);

        verify(dirContextMock).close();

        assertSame(transformed, actual);
    }

    @Test
    public void testLookup_String_ReturnAttributes_ContextMapper()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        when(dirContextMock.getAttributes(DEFAULT_BASE_STRING, attributeNames)).thenReturn(expectedAttributes);

        LdapName name = LdapUtils.newLdapName(DEFAULT_BASE_STRING);
        DirContextAdapter adapter = new DirContextAdapter(expectedAttributes,
                name);

        Object transformed = new Object();
        when(contextMapperMock.mapFromContext(adapter)).thenReturn(transformed);

        Object actual = tested.lookup(DEFAULT_BASE_STRING, attributeNames,
                contextMapperMock);

        verify(dirContextMock).close();

        assertSame(transformed, actual);
    }
}
