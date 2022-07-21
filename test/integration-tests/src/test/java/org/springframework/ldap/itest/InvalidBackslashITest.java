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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for verifying that issues LDAP-50 and LDAP-109 are solved.
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class InvalidBackslashITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private static LdapName DN = LdapUtils.newLdapName("cn=Some\\\\Person6,ou=company1,ou=Sweden");

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some\\Person6");
		adapter.setAttributeValue("sn", "Person6");
		adapter.setAttributeValue("description", "Some description");

		tested.unbind(DN);
		tested.bind(DN, adapter, null);
	}

	@After
	public void cleanup() throws Exception {
		tested.unbind(DN);
	}

	/**
	 * Test for LDAP-109, LDAP-50. When an entry has a distinguished name
	 * including a backslach ('\') the Name supplied to DefaultDirObjectFactory
	 * will be invalid.
	 * <p>
	 * E.g. the distinguished name "cn=Some\\Person6,ou=company1,ou=Sweden"
	 * (indicating that the cn value is 'Some\Person'), will be represented by a
	 * <code>CompositeName</code> with the string representation
	 * "cn=Some\\\Person6,ou=company1,ou=Sweden", which is in fact an invalid DN.
	 * This will be supplied to <code>DistinguishedName</code> for parsing,
	 * causing it to fail. This test makes sure that Spring LDAP properly works
	 * around this bug.
	 * </p>
	 * <p>
	 * What happens under the covers is (in the Java LDAP Provider code):
	 * 
	 * <pre>
	 * LdapName ldapname = new LdapName(&quot;cn=Some\\\\Person6,ou=company1,ou=Sweden&quot;);
	 * CompositeName compositeName = new CompositeName();
	 * compositeName.add(ldapname.get(ldapname.size() - 1)); // for some odd reason
	 * </pre>
	 * <code>CompositeName#add()</code> cannot handle this and the result is
	 * the spoiled DN.
	 * </p>
	 * @throws InvalidNameException
	 */
	@Test
	@Category(NoAdTest.class)
	public void testSearchForDnSpoiledByCompositeName() throws InvalidNameException {
		List result = tested.search("", "(sn=Person6)", new AbstractContextMapper() {
			@Override
			protected Object doMapFromContext(DirContextOperations ctx) {
				LdapName dn = (LdapName) ctx.getDn();
				Rdn rdn = LdapUtils.getRdn(dn, "cn");
				assertThat(dn.toString()).isEqualTo("cn=Some\\\\Person6,ou=company1,ou=Sweden");
				assertThat(rdn.getValue()).isEqualTo("Some\\Person6");
				return new Object();
			}
		});

		assertThat(result).hasSize(1);
	}
}
