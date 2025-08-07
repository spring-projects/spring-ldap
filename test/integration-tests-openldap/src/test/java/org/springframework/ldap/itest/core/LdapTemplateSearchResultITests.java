/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.itest.core;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.SearchControls;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.AttributeCheckAttributesMapper;
import org.springframework.ldap.test.AttributeCheckContextMapper;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that LdapTemplate search methods work against OpenLDAP with TLS.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext-tls.xml" })
public class LdapTemplateSearchResultITests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private LdapTemplate tested;

	@Autowired
	private ContextSource contextSource;

	private AttributeCheckAttributesMapper attributesMapper;

	private AttributeCheckContextMapper contextMapper;

	private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description", "telephoneNumber" };

	private static final String[] CN_SN_ATTRS = { "cn", "sn" };

	private static final String[] ABSENT_ATTRIBUTES = { "description", "telephoneNumber" };

	private static final String[] CN_SN_VALUES = { "Some Person2", "Person2" };

	private static final String[] ALL_VALUES = { "Some Person2", "Person2", "Sweden, Company1, Some Person2",
			"+46 555-123458" };

	private static final String BASE_STRING = "";

	private static final String FILTER_STRING = "(&(objectclass=person)(sn=Person2))";

	private static final Name BASE_NAME = new DistinguishedName(BASE_STRING);

	@Before
	public void prepareTestedInstance() throws Exception {
		LdapTestUtils.cleanAndSetup(this.contextSource, LdapUtils.newLdapName("ou=People"),
				new ClassPathResource("/setup_data.ldif"));

		this.attributesMapper = new AttributeCheckAttributesMapper();
		this.contextMapper = new AttributeCheckContextMapper();
	}

	@After
	public void cleanup() throws Exception {
		LdapTestUtils.clearSubContexts(this.contextSource, LdapUtils.newLdapName("ou=People"));
		this.attributesMapper = null;
		this.contextMapper = null;
	}

	@Test
	public void testSearch_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search(BASE_STRING, FILTER_STRING, this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper() {
		this.attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.attributesMapper.setExpectedValues(CN_SN_VALUES);
		this.attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<Object> list = this.tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS,
				this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search(BASE_NAME, FILTER_STRING, this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.attributesMapper.setExpectedValues(ALL_VALUES);
		List<Object> list = this.tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_AttributesMapper_Name() {
		this.attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.attributesMapper.setExpectedValues(CN_SN_VALUES);
		this.attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<Object> list = this.tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS,
				this.attributesMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search(BASE_STRING, FILTER_STRING, this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_ContextMapper() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper() {
		this.contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.contextMapper.setExpectedValues(CN_SN_VALUES);
		this.contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<DirContextAdapter> list = this.tested.search(BASE_STRING, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				CN_SN_ATTRS, this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search(BASE_NAME, FILTER_STRING, this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List<DirContextAdapter> list = this.tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testSearch_SearchScope_LimitedAttrs_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(CN_SN_ATTRS);
		this.contextMapper.setExpectedValues(CN_SN_VALUES);
		this.contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
		List<DirContextAdapter> list = this.tested.search(BASE_NAME, FILTER_STRING, SearchControls.SUBTREE_SCOPE,
				CN_SN_ATTRS, this.contextMapper);
		assertThat(list).hasSize(1);
	}

}
