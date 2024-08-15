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

package org.springframework.ldap.itest;

import java.util.List;
import java.util.stream.Collectors;

import javax.naming.Name;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.AttributeCheckAttributesMapper;
import org.springframework.ldap.test.AttributeCheckContextMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link LdapClient}'s search methods.
 *
 * @author Josh Cummings
 */
@ContextConfiguration(locations = { "/conf/ldapClientTestContext.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DefaultLdapClientSearchResultITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapClient tested;

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
		this.attributesMapper = new AttributeCheckAttributesMapper();
		this.contextMapper = new AttributeCheckContextMapper();
	}

	@After
	public void cleanup() throws Exception {
		this.attributesMapper = null;
		this.contextMapper = null;
	}

	@Test
	public void testSearch_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		LdapQuery query = LdapQueryBuilder.query().base(BASE_STRING).filter(FILTER_STRING);
		List<Object> list = this.tested.search().query(query).toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).where("objectclass").is("person").and("sn").is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).where("objectclass").is("person").and("sn").is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper_FewerAttributes() {
		this.attributesMapper.setExpectedAttributes(new String[] { "cn" });
		this.attributesMapper.setExpectedValues(new String[] { "Some Person2" });

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.attributes("cn")
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper_FewerAttributes() {
		this.attributesMapper.setExpectedAttributes(new String[] { "cn" });
		this.attributesMapper.setExpectedValues(new String[] { "Some Person2" });

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.attributes("cn")
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper_SearchScope() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper_SearchScope() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper_SearchScope_CorrectBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base("ou=company1,ou=Sweden")
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper_SearchScope_CorrectBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base("ou=company1,ou=Sweden")
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper_NoBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().where("objectclass").is("person").and("sn").is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper_NoBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().where("objectclass").is("person").and("sn").is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_LdapQuery_AttributesMapper_DifferentBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base("ou=Norway").where("objectclass").is("person").and("sn").is("Person2"))
			.toList(this.attributesMapper);
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearchForStream_LdapQuery_AttributesMapper_DifferentBase() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);

		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base("ou=Norway").where("objectclass").is("person").and("sn").is("Person2"))
			.toStream(this.attributesMapper)
			.collect(Collectors.toList());
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearch_SearchScope_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).searchScope(SearchScope.SUBTREE).filter(FILTER_STRING))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.attributesMapper.setExpectedValues(CN_SN_VALUES);
		this.attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.searchScope(SearchScope.SUBTREE)
				.attributes(CN_SN_ATTRS)
				.filter(FILTER_STRING))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).filter(FILTER_STRING))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).searchScope(SearchScope.SUBTREE).filter(FILTER_STRING))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.attributesMapper.setExpectedValues(CN_SN_VALUES);
		this.attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<Object> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_NAME)
				.searchScope(SearchScope.SUBTREE)
				.attributes(CN_SN_ATTRS)
				.filter(FILTER_STRING))
			.toList(this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForObject() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		DirContextAdapter result = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).filter(FILTER_STRING))
			.toObject(this.contextMapper);
		assertThat(result).isNotNull();
	}

	@Test(expected = IncorrectResultSizeDataAccessException.class)
	public void testSearchForObjectWithMultipleHits() {
		this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).filter("(&(objectclass=person)(sn=*))"))
			.toObject((Object ctx) -> ctx);
	}

	@Test // (expected = EmptyResultDataAccessException.class)
	public void testSearchForObjectNoHits() {
		Object result = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.filter("(&(objectclass=person)(sn=Person does not exist))"))
			.toObject((Object ctx) -> ctx);
		assertThat(result).isNull();
	}

	@Test
	public void testSearch_SearchScope_ContextMapper() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_STRING).searchScope(SearchScope.SUBTREE).filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper() {
		this.contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.contextMapper.setExpectedValues(CN_SN_VALUES);
		this.contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_STRING)
				.searchScope(SearchScope.SUBTREE)
				.attributes(CN_SN_ATTRS)
				.filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper_LdapQuery() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).where("objectclass").is("person").and("sn").is("Person2"))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_ContextMapper_LdapQuery() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).where("objectclass").is("person").and("sn").is("Person2"))
			.toStream(this.contextMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper_LdapQuery_NoBase() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().where("objectclass").is("person").and("sn").is("Person2"))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_ContextMapper_LdapQuery_NoBase() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().where("objectclass").is("person").and("sn").is("Person2"))
			.toStream(this.contextMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper_LdapQuery_SearchScope() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_NAME)
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toList(this.contextMapper);
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearchForStream_ContextMapper_LdapQuery_SearchScope() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_NAME)
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toStream(this.contextMapper)
			.collect(Collectors.toList());
		assertThat(list).isEmpty();
	}

	@Test
	public void testSearch_ContextMapper_LdapQuery_SearchScope_CorrectBase() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base("ou=company1,ou=Sweden")
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForStream_ContextMapper_LdapQuery_SearchScope_CorrectBase() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base("ou=company1,ou=Sweden")
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toStream(this.contextMapper)
			.collect(Collectors.toList());
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchForContext_LdapQuery() {
		ContextMapper<DirContextOperations> mapper = (result) -> (DirContextOperations) result;
		DirContextOperations result = this.tested.search()
			.query(LdapQueryBuilder.query().where("objectclass").is("person").and("sn").is("Person2"))
			.toObject(mapper);
		assertThat(result).isNotNull();
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
	}

	@Test // (expected = EmptyResultDataAccessException.class)
	public void testSearchForContext_LdapQuery_SearchScopeNotFound() {
		Object result = this.tested.search()
			.query(LdapQueryBuilder.query()
				.searchScope(SearchScope.ONELEVEL)
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toObject(this.attributesMapper);
		assertThat(result).isNull();
	}

	@Test
	public void testSearchForContext_LdapQuery_SearchScope_CorrectBase() {
		ContextMapper<DirContextOperations> mapper = (result) -> (DirContextOperations) result;
		DirContextOperations result = this.tested.search()
			.query(LdapQueryBuilder.query()
				.searchScope(SearchScope.ONELEVEL)
				.base("ou=company1,ou=Sweden")
				.where("objectclass")
				.is("person")
				.and("sn")
				.is("Person2"))
			.toObject(mapper);

		assertThat(result).isNotNull();
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
	}

	@Test
	public void testSearch_SearchScope_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query().base(BASE_NAME).searchScope(SearchScope.SUBTREE).filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.contextMapper.setExpectedValues(CN_SN_VALUES);
		this.contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_NAME)
				.searchScope(SearchScope.SUBTREE)
				.attributes(CN_SN_ATTRS)
				.filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearchWithInvalidSearchBaseShouldByDefaultThrowException() {
		try {
			this.tested.search()
				.query(LdapQueryBuilder.query()
					.base(BASE_NAME + "ou=unknown")
					.searchScope(SearchScope.SUBTREE)
					.attributes(CN_SN_ATTRS)
					.filter(FILTER_STRING))
				.toObject(this.contextMapper);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testSearchWithInvalidSearchBaseCanBeConfiguredToSwallowException() {
		ReflectionTestUtils.setField(this.tested, "ignoreNameNotFoundException", true);
		this.contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.contextMapper.setExpectedValues(CN_SN_VALUES);
		this.contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<DirContextAdapter> list = this.tested.search()
			.query(LdapQueryBuilder.query()
				.base(BASE_NAME + "ou=unknown")
				.searchScope(SearchScope.SUBTREE)
				.attributes(CN_SN_ATTRS)
				.filter(FILTER_STRING))
			.toList(this.contextMapper);
		assertThat(list).isEmpty();
	}

	@Test
	public void verifyThatSearchWithCountLimitReturnsTheEntriesFoundSoFar() {
		List<Object> result = this.tested.search()
			.query(LdapQueryBuilder.query().countLimit(3).where("objectclass").is("person"))
			.toList((Object ctx) -> new Object());

		assertThat(result).hasSize(3);
	}

	@Test(expected = SizeLimitExceededException.class)
	public void verifyThatSearchWithCountLimitWithFlagToFalseThrowsException() {
		ReflectionTestUtils.setField(this.tested, "ignoreSizeLimitExceededException", false);
		this.tested.search()
			.query(LdapQueryBuilder.query().countLimit(3).where("objectclass").is("person"))
			.toList((Object ctx) -> ctx);
	}

}
