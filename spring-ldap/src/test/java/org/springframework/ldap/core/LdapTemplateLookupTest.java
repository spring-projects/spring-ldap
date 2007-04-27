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

import javax.naming.Name;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.NameNotFoundException;

public class LdapTemplateLookupTest extends TestCase {

    private static final String DEFAULT_BASE_STRING = "o=example.com";

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl attributesMapperControl;

    private AttributesMapper attributesMapperMock;

    private MockControl nameControl;

    private Name nameMock;

    private MockControl contextMapperControl;

    private ContextMapper contextMapperMock;

    private LdapTemplate tested;

    protected void setUp() throws Exception {
        super.setUp();

        // Setup ContextSource mock
        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        // Setup LdapContext mock
        dirContextControl = MockControl.createControl(LdapContext.class);
        dirContextMock = (LdapContext) dirContextControl.getMock();

        // Setup Name mock
        nameControl = MockControl.createControl(Name.class);
        nameMock = (Name) nameControl.getMock();

        contextMapperControl = MockControl.createControl(ContextMapper.class);
        contextMapperMock = (ContextMapper) contextMapperControl.getMock();

        attributesMapperControl = MockControl
                .createControl(AttributesMapper.class);
        attributesMapperMock = (AttributesMapper) attributesMapperControl
                .getMock();

        tested = new LdapTemplate(contextSourceMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextSourceControl = null;
        contextSourceMock = null;

        dirContextControl = null;
        dirContextMock = null;

        nameControl = null;
        nameMock = null;

        contextMapperControl = null;
        contextMapperMock = null;

        attributesMapperControl = null;
        attributesMapperMock = null;
    }

    protected void replay() {
        contextSourceControl.replay();
        dirContextControl.replay();
        nameControl.replay();
        contextMapperControl.replay();
        attributesMapperControl.replay();
    }

    protected void verify() {
        contextSourceControl.verify();
        dirContextControl.verify();
        nameControl.verify();
        contextMapperControl.verify();
        attributesMapperControl.verify();
    }

    private void expectGetReadOnlyContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), dirContextMock);
    }

    // Tests for lookup(name)

    public void testLookup() throws Exception {
        expectGetReadOnlyContext();

        Object expected = new Object();
        dirContextControl.expectAndReturn(dirContextMock.lookup(nameMock),
                expected);

        dirContextMock.close();

        replay();

        Object actual = tested.lookup(nameMock);

        verify();

        assertSame(expected, actual);
    }

    public void testLookup_String() throws Exception {
        expectGetReadOnlyContext();

        Object expected = new Object();
        dirContextControl.expectAndReturn(dirContextMock
                .lookup(DEFAULT_BASE_STRING), expected);

        dirContextMock.close();

        replay();

        Object actual = tested.lookup(DEFAULT_BASE_STRING);

        verify();

        assertSame(expected, actual);
    }

    public void testLookup_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.expectAndThrow(dirContextMock.lookup(nameMock), ne);

        dirContextMock.close();

        replay();

        try {
            tested.lookup(nameMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    // Tests for lookup(name, AttributesMapper)

    public void testLookup_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextControl.expectAndReturn(dirContextMock
                .getAttributes(nameMock), expectedAttributes);
        dirContextMock.close();

        Object expected = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expected);

        replay();

        Object actual = tested.lookup(nameMock, attributesMapperMock);

        verify();

        assertSame(expected, actual);
    }

    public void testLookup_String_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        BasicAttributes expectedAttributes = new BasicAttributes();
        dirContextControl.expectAndReturn(dirContextMock
                .getAttributes(DEFAULT_BASE_STRING), expectedAttributes);
        dirContextMock.close();

        Object expected = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expected);

        replay();

        Object actual = tested
                .lookup(DEFAULT_BASE_STRING, attributesMapperMock);

        verify();

        assertSame(expected, actual);
    }

    public void testLookup_AttributesMapper_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.expectAndThrow(
                dirContextMock.getAttributes(nameMock), ne);
        dirContextMock.close();

        replay();

        try {
            tested.lookup(nameMock, attributesMapperMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    // Tests for lookup(name, ContextMapper)

    public void testLookup_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        Object transformed = new Object();
        Object expected = new Object();
        dirContextControl.expectAndReturn(dirContextMock.lookup(nameMock),
                expected);

        dirContextMock.close();

        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expected), transformed);

        replay();

        Object actual = tested.lookup(nameMock, contextMapperMock);

        verify();

        assertSame(transformed, actual);
    }

    public void testLookup_String_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        Object transformed = new Object();
        Object expected = new Object();
        dirContextControl.expectAndReturn(dirContextMock
                .lookup(DEFAULT_BASE_STRING), expected);

        dirContextMock.close();

        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(expected), transformed);

        replay();

        Object actual = tested.lookup(DEFAULT_BASE_STRING, contextMapperMock);

        verify();

        assertSame(transformed, actual);
    }

    public void testLookup_ContextMapper_NamingException() throws Exception {
        expectGetReadOnlyContext();

        javax.naming.NameNotFoundException ne = new javax.naming.NameNotFoundException();
        dirContextControl.expectAndThrow(dirContextMock.lookup(nameMock), ne);

        dirContextMock.close();

        replay();

        try {
            tested.lookup(nameMock, contextMapperMock);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    // Tests for lookup(name, attributes, AttributesMapper)

    public void testLookup_ReturnAttributes_AttributesMapper() throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        dirContextControl.expectAndReturn(dirContextMock.getAttributes(
                nameMock, attributeNames), expectedAttributes);
        dirContextMock.close();

        Object expected = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expected);

        replay();

        Object actual = tested.lookup(nameMock, attributeNames,
                attributesMapperMock);

        verify();

        assertSame(expected, actual);
    }

    public void testLookup_String_ReturnAttributes_AttributesMapper()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        dirContextControl.expectAndReturn(dirContextMock.getAttributes(
                DEFAULT_BASE_STRING, attributeNames), expectedAttributes);
        dirContextMock.close();

        Object expected = new Object();
        attributesMapperControl.expectAndReturn(attributesMapperMock
                .mapFromAttributes(expectedAttributes), expected);

        replay();

        Object actual = tested.lookup(DEFAULT_BASE_STRING, attributeNames,
                attributesMapperMock);

        verify();

        assertSame(expected, actual);
    }

    // Tests for lookup(name, attributes, ContextMapper)

    public void testLookup_ReturnAttributes_ContextMapper() throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        DistinguishedName name = new DistinguishedName(DEFAULT_BASE_STRING);
        DirContextAdapter adapter = new DirContextAdapter(expectedAttributes,
                name);

        dirContextControl.expectAndReturn(dirContextMock.getAttributes(name,
                attributeNames), expectedAttributes);
        dirContextMock.close();

        Object transformed = new Object();
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(adapter), transformed);

        replay();

        Object actual = tested.lookup(name, attributeNames, contextMapperMock);

        verify();

        assertSame(transformed, actual);
    }

    public void testLookup_String_ReturnAttributes_ContextMapper()
            throws Exception {
        expectGetReadOnlyContext();

        String[] attributeNames = new String[] { "cn" };

        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("cn", "Some Name");

        dirContextControl.expectAndReturn(dirContextMock.getAttributes(
                DEFAULT_BASE_STRING, attributeNames), expectedAttributes);
        dirContextMock.close();

        DistinguishedName name = new DistinguishedName(DEFAULT_BASE_STRING);
        DirContextAdapter adapter = new DirContextAdapter(expectedAttributes,
                name);

        Object transformed = new Object();
        contextMapperControl.expectAndReturn(contextMapperMock
                .mapFromContext(adapter), transformed);

        replay();

        Object actual = tested.lookup(DEFAULT_BASE_STRING, attributeNames,
                contextMapperMock);

        verify();

        assertSame(transformed, actual);
    }
}
