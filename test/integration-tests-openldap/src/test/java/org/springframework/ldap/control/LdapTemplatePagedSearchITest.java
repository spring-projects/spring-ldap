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
package org.springframework.ldap.control;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.util.List;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Tests the paged search result capability of LdapTemplate.
 * <p>
 * Note: Currently, ApacheDS does not support paged results controls, so this
 * test must be run under another directory server, for example OpenLdap. This
 * test will not run under ApacheDS, and the other integration tests assume
 * ApacheDS and will probably not run under OpenLdap.
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplatePagedSearchITest extends AbstractJUnit4SpringContextTests {

	private static final Name BASE = DistinguishedName.EMPTY_PATH;

	private static final String FILTER_STRING = "(&(objectclass=person))";

	@Autowired
	private LdapTemplate tested;

	private CollectingNameClassPairCallbackHandler callbackHandler;

	private SearchControls searchControls;

	@Before
	public void onSetUp() throws Exception {
		PersonContextMapper mapper = new PersonContextMapper();
		callbackHandler = new ContextMapperCallbackHandler(mapper);
		searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setReturningObjFlag(true);
	}

	@After
	public void onTearDown() throws Exception {
		callbackHandler = null;
		tested = null;
		searchControls = null;
	}

	@Test
	public void testSearch_PagedResult() {
		SearchExecutor searchExecutor = new SearchExecutor() {
			public NamingEnumeration executeSearch(DirContext ctx) throws NamingException {
				return ctx.search(BASE, FILTER_STRING, searchControls);
			}
		};
		Person person;
		List list;
		PagedResultsCookie cookie;
		PagedResultsRequestControl requestControl;

		// Prepare for first search
		requestControl = new PagedResultsRequestControl(3);
		tested.search(searchExecutor, callbackHandler, requestControl);
		cookie = requestControl.getCookie();
		assertNotNull("Cookie should not be null yet", cookie.getCookie());
		list = callbackHandler.getList();
		assertEquals(3, list.size());
		person = (Person) list.get(0);
		assertEquals("Some Person", person.getFullName());
		assertEquals("+46 555-123456", person.getPhone());
		person = (Person) list.get(1);
		assertEquals("Some Person2", person.getFullName());
		assertEquals("+46 555-654321", person.getPhone());
		person = (Person) list.get(2);
		assertEquals("Some Person3", person.getFullName());
		assertEquals("+46 555-123654", person.getPhone());

		// Prepare for second and last search
		requestControl = new PagedResultsRequestControl(3, cookie);
		tested.search(searchExecutor, callbackHandler, requestControl);
		cookie = requestControl.getCookie();
		assertNull("Cookie should be null now", cookie.getCookie());
		assertEquals(5, list.size());
		person = (Person) list.get(3);
		assertEquals("Some Person", person.getFullName());
		assertEquals("+46 555-456321", person.getPhone());
		person = (Person) list.get(4);
		assertEquals("Some Person", person.getFullName());
		assertEquals("+45 555-654123", person.getPhone());
	}

	public void testSearch_PagedResult_ConvenienceMethod() {
		Person person;
		List list;
		PagedResultsCookie cookie;
		PagedResultsRequestControl requestControl;

		// Prepare for first search
		requestControl = new PagedResultsRequestControl(3);
		tested.search(BASE, FILTER_STRING, searchControls, callbackHandler, requestControl);
		cookie = requestControl.getCookie();
		assertNotNull("Cookie should not be null yet", cookie.getCookie());
		list = callbackHandler.getList();
		assertEquals(3, list.size());
		person = (Person) list.get(0);
		assertEquals("Some Person", person.getFullName());
		person = (Person) list.get(1);
		assertEquals("Some Person2", person.getFullName());
		person = (Person) list.get(2);
		assertEquals("Some Person3", person.getFullName());

		// Prepare for second and last search
		requestControl = new PagedResultsRequestControl(3, cookie);
		tested.search(BASE, FILTER_STRING, searchControls, callbackHandler, requestControl);
		cookie = requestControl.getCookie();
		assertNull("Cookie should be null now", cookie.getCookie());
		assertEquals(5, list.size());
		person = (Person) list.get(3);
		assertEquals("Some Person", person.getFullName());
		person = (Person) list.get(4);
		assertEquals("Some Person", person.getFullName());
	}

	private static class PersonContextMapper implements ContextMapper {

		public Object mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter) ctx;
			DistinguishedName dn = new DistinguishedName(context.getDn());
			Person person = new Person();
			person.setCountry(dn.getLdapRdn(0).getComponent().getValue());
			person.setCompany(dn.getLdapRdn(1).getComponent().getValue());
			person.setFullName(context.getStringAttribute("cn"));
			person.setLastName(context.getStringAttribute("sn"));
			person.setDescription(context.getStringAttribute("description"));
			person.setPhone(context.getStringAttribute("telephoneNumber"));

			return person;
		}
	}

	public void setTested(LdapTemplate tested) {
		this.tested = tested;
	}
}
