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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests to verify that not setting a base suffix on the ContextSource (as defined in
 * ldapTemplateNoBaseSuffixTestContext.xml) works as expected.
 *
 * NOTE: This test will not work under Java 1.4.1 or earlier.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateNoBaseSuffixTestContext.xml" })
public class LdapTemplateNoBaseSuffixITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	@Override
	protected Name getRoot() {
		return LdapUtils.newLdapName(base);
	}

	/**
	 * This method depends on a DirObjectFactory
	 * ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory}) being set
	 * in the ContextSource.
	 */
	@Test
	public void testLookup_Plain() {
		String expectedDn = "cn=Some Person2, ou=company1, ou=Sweden," + base;
		DirContextAdapter result = (DirContextAdapter) tested.lookup(expectedDn);

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person2");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
		assertThat(result.getStringAttribute("description")).isEqualTo("Sweden, Company1, Some Person2");

		LdapName expectedName = LdapUtils.newLdapName(expectedDn);
		assertThat(result.getDn()).isEqualTo(expectedName);
		assertThat(result.getNameInNamespace()).isEqualTo(expectedDn);
	}

	@Test
	public void testSearch_Plain() {
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();

		tested.search(base, "(objectclass=person)", handler);
		assertThat(handler.getNoOfRows()).isEqualTo(5);
	}

	@Test
	public void testBindAndUnbind_Plain() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");
		tested.bind("cn=Some Person4, ou=company1, ou=Sweden," + base, adapter, null);

		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person4, ou=company1, ou=Sweden," + base);

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person4");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person4");
		assertThat(result.getDn()).isEqualTo(LdapUtils.newLdapName("cn=Some Person4,ou=company1,ou=Sweden," + base));

		tested.unbind("cn=Some Person4,ou=company1,ou=Sweden," + base);
		try {
			tested.lookup("cn=Some Person4, ou=company1, ou=Sweden," + base);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
	}

}
