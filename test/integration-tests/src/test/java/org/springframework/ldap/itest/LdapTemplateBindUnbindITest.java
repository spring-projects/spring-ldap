/*
 * Copyright 2005-2016 the original author or authors.
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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the bind and unbind methods of LdapTemplate. The test methods in this
 * class tests a little too much, but we need to clean up after binding, so the
 * most efficient way to test is to do it all in one test method. Also, the
 * methods in this class relies on that the lookup method works as it should -
 * that should be ok, since that is verified in a separate test class.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateBindUnbindITest extends
		AbstractLdapTemplateIntegrationTest {
	@Autowired
	private LdapTemplate tested;

	private static String DN = "cn=Some Person4,ou=company1,ou=Sweden";

	@Test
	public void testBindAndUnbindWithAttributes() {
		Attributes attributes = setupAttributes();
		tested.bind(DN, null, attributes);
		verifyBoundCorrectData();
		tested.unbind(DN);
		verifyCleanup();
	}

    @Test
    public void testBindGroupOfUniqueNamesWithNameValues() {
        DirContextAdapter ctx = new DirContextAdapter(LdapUtils.newLdapName("cn=TEST,ou=groups"));
        ctx.addAttributeValue("cn", "TEST");
        ctx.addAttributeValue("objectclass", "top");
        ctx.addAttributeValue("objectclass", "groupOfUniqueNames");
        ctx.addAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base));
        tested.bind(ctx);
    }

	@Test
	public void testBindAndUnbindWithAttributesUsingLdapName() {
		Attributes attributes = setupAttributes();
		tested.bind(LdapUtils.newLdapName(DN), null, attributes);
		verifyBoundCorrectData();
		tested.unbind(LdapUtils.newLdapName(DN));
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindWithDirContextAdapter() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top",
				"person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		tested.bind(DN, adapter, null);
		verifyBoundCorrectData();
		tested.unbind(DN);
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindWithDirContextAdapterUsingLdapName() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top",
				"person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		tested.bind(LdapUtils.newLdapName(DN), adapter, null);
		verifyBoundCorrectData();
		tested.unbind(LdapUtils.newLdapName(DN));
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindWithDirContextAdapterOnly() {
		DirContextAdapter adapter = new DirContextAdapter(LdapUtils.newLdapName(DN));
		adapter.setAttributeValues("objectclass", new String[] { "top",
				"person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		tested.bind(adapter);
		verifyBoundCorrectData();
		tested.unbind(DN);
		verifyCleanup();
	}

	@Test
	public void testBindAndRebindWithDirContextAdapterOnly() {
		DirContextAdapter adapter = new DirContextAdapter(LdapUtils.newLdapName(DN));
		adapter.setAttributeValues("objectclass", new String[] { "top",
				"person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		tested.bind(adapter);
		verifyBoundCorrectData();
		adapter.setAttributeValue("sn", "Person4.Changed");
		tested.rebind(adapter);
		verifyReboundCorrectData();
		tested.unbind(DN);
		verifyCleanup();
	}

	private Attributes setupAttributes() {
		Attributes attributes = new BasicAttributes();
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("top");
		ocattr.add("person");
		attributes.put(ocattr);
		attributes.put("cn", "Some Person4");
		attributes.put("sn", "Person4");
		return attributes;
	}

	private void verifyBoundCorrectData() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person4");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person4");
	}

	private void verifyReboundCorrectData() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person4");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person4.Changed");
	}

	private void verifyCleanup() {
		try {
			tested.lookup(DN);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
	}
}
