/*
 * Copyright 2005-2016 the original author or authors.
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
package org.springframework.ldap.pool.validation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.pool.DirContextType;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        assertThat(tested.getSearchControls().getSearchScope()).as("ONELEVEL_SCOPE, ").isEqualTo(SearchControls.ONELEVEL_SCOPE);
	}
    
    // LDAP-189
    @Test
    public void testSearchScopeSubTreeScopeSetInConstructorIsUsed() throws Exception {
    	DefaultDirContextValidator tested = new DefaultDirContextValidator(SearchControls.SUBTREE_SCOPE);
    	assertThat(tested.getSearchControls().getSearchScope()).as("SUBTREE_SCOPE, ").isEqualTo(SearchControls.SUBTREE_SCOPE);
    }

    // LDAP-189
    @Test
    public void testSearchScopeObjectScopeSetInConstructorIsUsed() throws Exception {
        DefaultDirContextValidator tested = new DefaultDirContextValidator(SearchControls.OBJECT_SCOPE);
        assertThat(tested.getSearchControls().getSearchScope()).as("OBJECT_SCOPE, ").isEqualTo(SearchControls.OBJECT_SCOPE);
	}

    @Test
    public void testProperties() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        dirContextValidator.setBase("baseName");
        final String baseName = dirContextValidator.getBase();
        assertThat(baseName).isEqualTo("baseName");

        try {
            dirContextValidator.setFilter(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }
        dirContextValidator.setFilter("filter");
        final String filter = dirContextValidator.getFilter();
        assertThat(filter).isEqualTo("filter");

        try {
            dirContextValidator.setSearchControls(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }
        final SearchControls sc = new SearchControls();
        dirContextValidator.setSearchControls(sc);
        final SearchControls sc2 = dirContextValidator.getSearchControls();
        assertThat(sc2).isEqualTo(sc);
    }

    @Test
    public void testValidateDirContextAssertions() throws Exception {
        final DefaultDirContextValidator dirContextValidator = new DefaultDirContextValidator();

        try {
            dirContextValidator.validateDirContext(DirContextType.READ_ONLY,
                    null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }

        try {
            dirContextValidator.validateDirContext(null, dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
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
        assertThat(valid).isTrue();
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

        assertThat(valid).isFalse();
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

        assertThat(valid).isFalse();
    }
}
