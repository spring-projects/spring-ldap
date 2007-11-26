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
package org.springframework.ldap.validation;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;

/**
 * @author Eric Dalquist <a
 *         href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DefaultDirContextValidatorTest extends TestCase {

    private MockControl namingEnumerationControl;

    private NamingEnumeration namingEnumerationMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    protected void setUp() throws Exception {
        super.setUp();
        namingEnumerationControl = MockControl
                .createControl(NamingEnumeration.class);
        namingEnumerationMock = (NamingEnumeration) namingEnumerationControl
                .getMock();

        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        namingEnumerationControl = null;
        namingEnumerationMock = null;

        dirContextControl = null;
        dirContextMock = null;
    }

    protected void replay() {
        namingEnumerationControl.replay();
        dirContextControl.replay();
    }

    protected void verify() {
        namingEnumerationControl.verify();
        dirContextControl.verify();
    }

    public void testProperties() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        dirContextValidator.setBase("baseName");
        final String baseName = dirContextValidator.getBase();
        assertEquals("baseName", baseName);

        try {
            dirContextValidator.setFilter(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        dirContextValidator.setFilter("filter");
        final String filter = dirContextValidator.getFilter();
        assertEquals("filter", filter);

        try {
            dirContextValidator.setSearchControls(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        final SearchControls sc = new SearchControls();
        dirContextValidator.setSearchControls(sc);
        final SearchControls sc2 = dirContextValidator.getSearchControls();
        assertEquals(sc, sc2);
    }

    public void testValidateDirContextAssertions() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        try {
            dirContextValidator.validateDirContext(DirContextType.READ_ONLY,
                    null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        replay();

        try {
            dirContextValidator.validateDirContext(null, dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testValidateDirContextHasResult() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        dirContextControl.expectAndReturn(dirContextMock.search(baseName,
                filter, searchControls), namingEnumerationMock);

        replay();

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);

        verify();
        assertTrue(valid);
    }

    public void testValidateDirContextNoResult() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        dirContextControl.expectAndReturn(dirContextMock.search(baseName,
                filter, searchControls), namingEnumerationMock);

        replay();

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);

        verify();
        assertFalse(valid);
    }

    public void testValidateDirContextException() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        dirContextControl.expectAndThrow(dirContextMock.search(baseName,
                filter, searchControls),
                new NamingException("Failed to search"));

        replay();

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);

        verify();
        assertFalse(valid);
    }
}
