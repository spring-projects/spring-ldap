/*
 * Copyright 2005-2008 the original author or authors.
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.test.AttributeCheckContextMapper;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for LdapTemplate's list methods.
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateListITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private AttributeCheckContextMapper contextMapper;

	private static final String BASE_STRING = "";

	private static final DistinguishedName BASE_NAME = new DistinguishedName(BASE_STRING);

	private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description", "telephoneNumber" };

	private static final String[] ALL_VALUES = { "Some Person", "Person", "Sweden, Company2, Some Person",
			"+46 555-456321" };

	@Before
	public void prepareTestedInstance() throws Exception {
		contextMapper = new AttributeCheckContextMapper();
	}

	@After
	public void tearDown() throws Exception {
		contextMapper = null;
	}

	@Test
	public void testListBindings_ContextMapper() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		List list = tested.listBindings("ou=company2,c=Sweden" + BASE_STRING, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testListBindings_ContextMapper_Name() {
		contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
		contextMapper.setExpectedValues(ALL_VALUES);
		DistinguishedName dn = new DistinguishedName("ou=company2,c=Sweden");
		dn.append(BASE_NAME);
		List list = tested.listBindings(dn, contextMapper);
		assertEquals(1, list.size());
	}

	@Test
	public void testListBindings_ContextMapper_MapToPersons() {
		DistinguishedName dn = new DistinguishedName("ou=company1,c=Sweden");
		dn.append(BASE_NAME);
		List list = tested.listBindings(dn, new PersonContextMapper());
		assertEquals(3, list.size());
		String personClass = "org.springframework.ldap.Person";
		assertEquals(personClass, list.get(0).getClass().getName());
		assertEquals(personClass, list.get(1).getClass().getName());
		assertEquals(personClass, list.get(2).getClass().getName());
	}

	@Test
	public void testList() {
		List list = tested.list(BASE_STRING);
		assertEquals(3, list.size());
		assertTrue(list.contains("ou=groups"));
		assertTrue(list.contains("c=Norway"));
		assertTrue(list.contains("c=Sweden"));
	}

	@Test
	public void testList_Name() {
		List list = tested.list(BASE_NAME);
		assertEquals(3, list.size());
		assertTrue(list.contains("ou=groups"));
		assertTrue(list.contains("c=Norway"));
		assertTrue(list.contains("c=Sweden"));
	}

	@Test
	public void testList_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		tested.list(BASE_STRING, handler);
		assertEquals(3, handler.getNoOfRows());
	}

	@Test
	public void testList_Name_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		tested.list(BASE_NAME, handler);
		assertEquals(3, handler.getNoOfRows());
	}

	@Test
	public void testListBindings() {
		List list = tested.listBindings(BASE_STRING);
		assertEquals(3, list.size());
		assertTrue(list.contains("ou=groups"));
		assertTrue(list.contains("c=Norway"));
		assertTrue(list.contains("c=Sweden"));
	}

	@Test
	public void testListBindings_Name() {
		List list = tested.listBindings(BASE_NAME);
		assertEquals(3, list.size());
	}

	@Test
	public void testListBindings_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		tested.listBindings(BASE_STRING, handler);
		assertEquals(3, handler.getNoOfRows());
	}

	@Test
	public void testListBindings_Name_Handler() throws Exception {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		tested.listBindings(BASE_NAME, handler);
		assertEquals(3, handler.getNoOfRows());
	}
}
