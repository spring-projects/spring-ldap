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

package org.springframework.ldap;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.ldap.LdapName;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Tests to verify that not setting a base suffix on the ContextSource (as
 * defined in ldapTemplateNoBaseSuffixTestContext.xml) works as expected.
 * 
 * NOTE: This test will not work under Java 1.4.1 or earlier.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateNoBaseSuffixTestContext.xml" })
public class LdapTemplateNoBaseSuffixITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Override
	protected DistinguishedName getRoot() {
		return new DistinguishedName("dc=jayway,dc=se");
	}

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	@Test
	public void testLookup_Plain() {
        String expectedDn = "cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se";
        DirContextAdapter result = (DirContextAdapter) tested.lookup(expectedDn);

		assertEquals("Some Person2", result.getStringAttribute("cn"));
		assertEquals("Person2", result.getStringAttribute("sn"));
		assertEquals("Sweden, Company1, Some Person2", result.getStringAttribute("description"));

        LdapName expectedName = LdapUtils.newLdapName(expectedDn);
        assertEquals(expectedName, result.getDn());
		assertEquals(expectedDn, result.getNameInNamespace());
	}

	@Test
	public void testSearch_Plain() {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();

		tested.search("dc=jayway, dc=se", "(objectclass=person)", handler);
		assertEquals(5, handler.getNoOfRows());
	}

	@Test
	public void testBindAndUnbind_Plain() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");
		tested.bind("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se", adapter, null);

		DirContextAdapter result = (DirContextAdapter) tested
				.lookup("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se");

		assertEquals("Some Person4", result.getStringAttribute("cn"));
		assertEquals("Person4", result.getStringAttribute("sn"));
		assertEquals(LdapUtils.newLdapName("cn=Some Person4,ou=company1,c=Sweden,dc=jayway,dc=se"), result.getDn());

		tested.unbind("cn=Some Person4,ou=company1,c=Sweden,dc=jayway,dc=se");
		try {
			tested.lookup("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se");
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}
	}
}
