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

package org.springframework.ldap.itest;

import java.util.LinkedList;
import java.util.List;

import javax.naming.ldap.LdapName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.AttributeCheckContextMapper;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for LdapTemplate's list methods.
 *
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateListITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	private AttributeCheckContextMapper contextMapper;

	private static final String BASE_STRING = "";

	private static final LdapName BASE_NAME = LdapUtils.newLdapName(BASE_STRING);

	private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description", "telephoneNumber" };

	private static final String[] ALL_VALUES = { "Some Person", "Person", "Sweden, Company2, Some Person",
			"+46 555-456321" };

	@Before
	public void prepareTestedInstance() throws Exception {
		this.contextMapper = new AttributeCheckContextMapper();
	}

	@After
	public void tearDown() throws Exception {
		this.contextMapper = null;
	}

	@Test
	public void testListBindings_ContextMapper() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		List list = this.tested.listBindings("ou=company2,ou=Sweden" + BASE_STRING, this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testListBindings_ContextMapper_Name() {
		this.contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		this.contextMapper.setExpectedValues(ALL_VALUES);
		LdapName dn = LdapUtils.newLdapName("ou=company2,ou=Sweden");
		List list = this.tested.listBindings(dn, this.contextMapper);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testListBindings_ContextMapper_MapToPersons() {
		LdapName dn = LdapUtils.newLdapName("ou=company1,ou=Sweden");
		List list = this.tested.listBindings(dn, new PersonContextMapper());
		assertThat(list).hasSize(3);
		String personClass = "org.springframework.ldap.itest.Person";
		assertThat(list.get(0).getClass().getName()).isEqualTo(personClass);
		assertThat(list.get(1).getClass().getName()).isEqualTo(personClass);
		assertThat(list.get(2).getClass().getName()).isEqualTo(personClass);
	}

	@Test
	public void testList() {
		List<String> list = this.tested.list(BASE_STRING);
		assertThat(list).hasSize(3);
		verifyBindings(list);
	}

	private void verifyBindings(List<String> list) {
		LinkedList<LdapName> transformed = new LinkedList<LdapName>();

		for (String s : list) {
			transformed.add(LdapUtils.newLdapName(s));
		}

		assertThat(transformed.contains(LdapUtils.newLdapName("ou=groups"))).isTrue();
		assertThat(transformed.contains(LdapUtils.newLdapName("ou=Norway"))).isTrue();
		assertThat(transformed.contains(LdapUtils.newLdapName("ou=Sweden"))).isTrue();
	}

	@Test
	public void testList_Name() {
		List<String> list = this.tested.list(BASE_NAME);
		assertThat(list).hasSize(3);
		verifyBindings(list);
	}

	@Test
	public void testList_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		this.tested.list(BASE_STRING, handler);
		assertThat(handler.getNoOfRows()).isEqualTo(3);
	}

	@Test
	public void testList_Name_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		this.tested.list(BASE_NAME, handler);
		assertThat(handler.getNoOfRows()).isEqualTo(3);
	}

	@Test
	public void testListBindings() {
		List<String> list = this.tested.listBindings(BASE_STRING);
		assertThat(list).hasSize(3);
		verifyBindings(list);
	}

	@Test
	public void testListBindings_Name() {
		List list = this.tested.listBindings(BASE_NAME);
		assertThat(list).hasSize(3);
	}

	@Test
	public void testListBindings_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		this.tested.listBindings(BASE_STRING, handler);
		assertThat(handler.getNoOfRows()).isEqualTo(3);
	}

	@Test
	public void testListBindings_Name_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		this.tested.listBindings(BASE_NAME, handler);
		assertThat(handler.getNoOfRows()).isEqualTo(3);
	}

}
