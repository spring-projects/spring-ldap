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
package org.springframework.ldap.pool.validation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.pool.DirContextType;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist <a
 *         href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DefaultDirContextValidatorTest {

    private NamingEnumeration namingEnumerationMock;

    private DirContext dirContextMock;

    @Before
    public void setUp() throws Exception {
        namingEnumerationMock = mock(NamingEnumeration.class);
        dirContextMock = mock(DirContext.class);
    }

    // LDAP-189
    @Test
    public void testSearchScopeOneLevelScopeSetInConstructorIsUsed() throws Exception {
        DefaultDirContextValidator tested = new DefaultDirContextValidator(SearchControls.ONELEVEL_SCOPE);
        assertEquals("ONELEVEL_SCOPE, ", SearchControls.ONELEVEL_SCOPE, tested.getSearchControls().getSearchScope());
	}
    
    // LDAP-189
    @Test
    public void testSearchScopeSubTreeScopeSetInConstructorIsUsed() throws Exception {
    	DefaultDirContextValidator tested = new DefaultDirContextValidator(SearchControls.SUBTREE_SCOPE);
    	assertEquals("SUBTREE_SCOPE, ", SearchControls.SUBTREE_SCOPE, tested.getSearchControls().getSearchScope());
    }

    // LDAP-189
    @Test
    public void testSearchScopeObjectScopeSetInConstructorIsUsed() throws Exception {
        DefaultDirContextValidator tested = new DefaultDirContextValidator(SearchControls.OBJECT_SCOPE);
        assertEquals("OBJECT_SCOPE, ", SearchControls.OBJECT_SCOPE, tested.getSearchControls().getSearchScope());
	}

    @Test
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

    @Test
    public void testValidateDirContextAssertions() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        try {
            dirContextValidator.validateDirContext(DirContextType.READ_ONLY,
                    null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            dirContextValidator.validateDirContext(null, dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testValidateDirContextHasResult() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        when(namingEnumerationMock.hasMore()).thenReturn(true);
        when(dirContextMock.search(baseName, filter, searchControls))
                .thenReturn(namingEnumerationMock);

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);
        assertTrue(valid);
    }

    @Test
    public void testValidateDirContextNoResult() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        when(namingEnumerationMock.hasMore()).thenReturn(false);
        when(dirContextMock.search(baseName, filter, searchControls))
                .thenReturn(namingEnumerationMock);

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);

        assertFalse(valid);
    }

    @Test
    public void testValidateDirContextException() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        final String baseName = dirContextValidator.getBase();
        final String filter = dirContextValidator.getFilter();
        final SearchControls searchControls = dirContextValidator
                .getSearchControls();

        when(dirContextMock.search(baseName, filter, searchControls))
                .thenThrow(new NamingException("Failed to search"));

        final boolean valid = dirContextValidator.validateDirContext(
                DirContextType.READ_ONLY, dirContextMock);

        assertFalse(valid);
    }
}
