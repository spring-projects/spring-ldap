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

package org.springframework.ldap;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the rename methods of LdapTemplate.
 * 
 * We rely on that the bind, unbind and lookup methods work as they should -
 * that should be ok, since that is verified in a separate test class. *
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class InvalidBackslashITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private static DistinguishedName DN = new DistinguishedName("cn=Some\\\\Person6,ou=company1,c=Sweden");

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
	 * E.g. the distinguished name "cn=Some\\Person6,ou=company1,c=Sweden"
	 * (indicating that the cn value is 'Some\Person'), will be represented by a
	 * <code>CompositeName</code> with the string representation
	 * "cn=Some\\\Person6,ou=company1,c=Sweden", which is in fact an invalid DN.
	 * This will be supplied to <code>DistinguishedName</code> for parsing,
	 * causing it to fail. This test makes sure that Spring LDAP properly works
	 * around this bug.
	 * </p>
	 * <p>
	 * What happens under the covers is (in the Java LDAP Provider code):
	 * 
	 * <pre>
	 * LdapName ldapname = new LdapName(&quot;cn=Some\\\\Person6,ou=company1,c=Sweden&quot;);
	 * compositeName compositeName = new CompositeName();
	 * compositeName.add(ldapname.get(ldapname.size() - 1)); // for some odd reason
	 * </pre>
	 * <code>CompositeName#add()</code> cannot handle this and the result is
	 * the spoiled DN.
	 * </p>
	 * @throws InvalidNameException
	 */
	@Test
	public void testSearchForDnSpoiledByCompositeName() throws InvalidNameException {
		List result = tested.search("", "(sn=Person6)", new AbstractContextMapper() {
			@Override
			protected Object doMapFromContext(DirContextOperations ctx) {
				DistinguishedName dn = (DistinguishedName) ctx.getDn();
				assertEquals("cn=Some\\\\Person6, ou=company1, c=Sweden", dn.toString());
				assertEquals("Some\\Person6", dn.getLdapRdn("cn").getValue());
				return new Object();
			}
		});

		assertEquals(1, result.size());
	}
}
