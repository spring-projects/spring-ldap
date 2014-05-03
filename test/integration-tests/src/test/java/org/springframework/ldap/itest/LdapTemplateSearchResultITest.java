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
package org.springframework.ldap.itest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.AttributeCheckAttributesMapper;
import org.springframework.ldap.test.AttributeCheckContextMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Tests for LdapTemplate's search methods. This test class tests all the
 * different versions of the search methods except the generic ones covered in
 * other tests.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class LdapTemplateSearchResultITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private AttributeCheckAttributesMapper attributesMapper;

	private AttributeCheckContextMapper contextMapper;

	private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description", "telephoneNumber" };

	private static final String[] CN_SN_ATTRS = { "cn", "sn" };

	private static final String[] ABSENT_ATTRIBUTES = { "description", "telephoneNumber" };

	private static final String[] CN_SN_VALUES = { "Some Person2", "Person2" };

	private static final String[] ALL_VALUES = { "Some Person2", "Person2", "Sweden, Company1, Some Person2",
			"+46 555-654321" };

	private static final String BASE_STRING = "";

	private static final String FILTER_STRING = "(&(objectclass=person)(sn=Person2))";

	private static final Name BASE_NAME = LdapUtils.newLdapName(BASE_STRING);

	@Before
	public void prepareTestedInstance() throws Exception {
		attributesMapper = new AttributeCheckAttributesMapper();
		contextMapper = new AttributeCheckContextMapper();
	}

	@After
	public void cleanup() throws Exception {
		attributesMapper = null;
		contextMapper = null;
	}

    @Test
	public void testSearch_AttributesMapper() {
		attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		attributesMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_STRING, FILTER_STRING, attributesMapper);
		assertEquals(1, list.size());
	}

    @Test
    public void testSearch_LdapQuery_AttributesMapper() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);

        List<Object> list = tested.search(query()
                .base(BASE_STRING)
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_LdapQuery_AttributesMapper_FewerAttributes() {
        attributesMapper.setExpectedAttributes(new String[] {"cn"});
        attributesMapper.setExpectedValues(new String[]{"Some Person2"});

        List<Object> list = tested.search(query()
                .base(BASE_STRING)
                .attributes("cn")
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_LdapQuery_AttributesMapper_SearchScope() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);

        List<Object> list = tested.search(query()
                .base(BASE_STRING)
                .searchScope(SearchScope.ONELEVEL)
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(0, list.size());
    }

    @Test
    public void testSearch_LdapQuery_AttributesMapper_SearchScope_CorrectBase() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);

        List<Object> list = tested.search(query()
                .base("ou=company1,ou=Sweden")
                .searchScope(SearchScope.ONELEVEL)
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_LdapQuery_AttributesMapper_NoBase() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);

        List<Object> list = tested.search(query()
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_LdapQuery_AttributesMapper_DifferentBase() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);

        List<Object> list = tested.search(query()
                .base("ou=Norway")
                .where("objectclass").is("person").and("sn").is("Person2"),
                attributesMapper);
        assertEquals(0, list.size());
    }

    @Test
	public void testSearch_SearchScope_AttributesMapper() {
		attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		attributesMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE, attributesMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper() {
		attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		attributesMapper.setExpectedValues(CN_SN_VALUES);
		attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List list = tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS,
				attributesMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_AttributesMapper_Name() {
		attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		attributesMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_NAME, FILTER_STRING, attributesMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_SearchScope_AttributesMapper_Name() {
		attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		attributesMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE, attributesMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper_Name() {
		attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		attributesMapper.setExpectedValues(CN_SN_VALUES);
		attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List list = tested
				.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, attributesMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_ContextMapper() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_STRING, FILTER_STRING, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearchForObject() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		DirContextAdapter result = (DirContextAdapter) tested
				.searchForObject(BASE_STRING, FILTER_STRING, contextMapper);
		assertNotNull(result);
	}

	@Test(expected = IncorrectResultSizeDataAccessException.class)
	public void testSearchForObjectWithMultipleHits() {
		tested.searchForObject(BASE_STRING, "(&(objectclass=person)(sn=*))", new AbstractContextMapper() {
			@Override
			protected Object doMapFromContext(DirContextOperations ctx) {
				return ctx;
			}
		});
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testSearchForObjectNoHits() {
		tested.searchForObject(BASE_STRING, "(&(objectclass=person)(sn=Person does not exist))", new AbstractContextMapper() {
			@Override
			protected Object doMapFromContext(DirContextOperations ctx) {
				return ctx;
			}
		});
	}

	@Test
	public void testSearch_SearchScope_ContextMapper() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper() {
		contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		contextMapper.setExpectedValues(CN_SN_VALUES);
		contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List list = tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_ContextMapper_Name() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_NAME, FILTER_STRING, contextMapper);
		assertEquals(1, list.size());
	}

    @Test
    public void testSearch_ContextMapper_LdapQuery() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List<DirContextAdapter> list = tested.search(query()
                .base(BASE_NAME)
                .where("objectclass").is("person").and("sn").is("Person2"),
                 contextMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_ContextMapper_LdapQuery_NoBase() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List<DirContextAdapter> list = tested.search(query()
                .where("objectclass").is("person").and("sn").is("Person2"),
                contextMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearch_ContextMapper_LdapQuery_SearchScope() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List<DirContextAdapter> list = tested.search(query()
                .base(BASE_NAME)
                .searchScope(SearchScope.ONELEVEL)
                .where("objectclass").is("person").and("sn").is("Person2"),
                contextMapper);
        assertEquals(0, list.size());
    }

    @Test
    public void testSearch_ContextMapper_LdapQuery_SearchScope_CorrectBase() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List<DirContextAdapter> list = tested.search(query()
                .base("ou=company1,ou=Sweden")
                .searchScope(SearchScope.ONELEVEL)
                .where("objectclass").is("person").and("sn").is("Person2"),
                contextMapper);
        assertEquals(1, list.size());
    }

    @Test
    public void testSearchForContext_LdapQuery() {
        DirContextOperations result = tested.searchForContext(query()
                .where("objectclass").is("person").and("sn").is("Person2"));

        assertNotNull(result);
        assertEquals("Person2", result.getStringAttribute("sn"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testSearchForContext_LdapQuery_SearchScopeNotFound() {
        tested.searchForContext(query()
                .searchScope(SearchScope.ONELEVEL)
                .where("objectclass").is("person").and("sn").is("Person2"));
    }

    @Test
    public void testSearchForContext_LdapQuery_SearchScope_CorrectBase() {
        DirContextOperations result =
                tested.searchForContext(query()
                .searchScope(SearchScope.ONELEVEL)
                .base("ou=company1,ou=Sweden")
                .where("objectclass").is("person").and("sn").is("Person2"));

        assertNotNull(result);
        assertEquals("Person2", result.getStringAttribute("sn"));
    }

    @Test
	public void testSearch_SearchScope_ContextMapper_Name() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		List list = tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper_Name() {
		contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		contextMapper.setExpectedValues(CN_SN_VALUES);
		contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List list = tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testSearchWithInvalidSearchBaseShouldByDefaultThrowException() {
		try {
			tested.search(BASE_NAME + "ou=unknown", FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS,
					contextMapper);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}
	}

	@Test
	public void testSearchWithInvalidSearchBaseCanBeConfiguredToSwallowException() {
		tested.setIgnoreNameNotFoundException(true);
		contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		contextMapper.setExpectedValues(CN_SN_VALUES);
		contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List list = tested.search(BASE_NAME + "ou=unknown", FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS,
				contextMapper);
		assertEquals(0, list.size());
	}

    @Test
    public void verifyThatSearchWithCountLimitReturnsTheEntriesFoundSoFar() {
        List<Object> result = tested.search(query()
                .countLimit(3)
                .where("objectclass").is("person"), new ContextMapper<Object>() {
            @Override
            public Object mapFromContext(Object ctx) throws NamingException {
                return new Object();
            }
        });

        assertEquals(3, result.size());
    }

    @Test(expected = SizeLimitExceededException.class)
    public void verifyThatSearchWithCountLimitWithFlagToFalseThrowsException() {
        tested.setIgnoreSizeLimitExceededException(false);
        tested.search(query()
                .countLimit(3)
                .where("objectclass").is("person"), new ContextMapper<Object>() {
            @Override
            public Object mapFromContext(Object ctx) throws NamingException {
                return new Object();
            }
        });
    }
}
