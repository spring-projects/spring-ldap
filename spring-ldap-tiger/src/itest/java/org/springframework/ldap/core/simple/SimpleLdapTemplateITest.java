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
package org.springframework.ldap.core.simple;

import java.util.List;

import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

public class SimpleLdapTemplateITest extends AbstractLdapTemplateIntegrationTest {

	private SimpleLdapTemplate ldapTemplate;

	protected String[] getConfigLocations() {
		return new String[] { "/conf/ldapTemplateTestContext.xml" };
	}

	public void setSimplLdapTemplate(SimpleLdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void testLookup() {
		String result = ldapTemplate.lookup("cn=Some Person,ou=company1,c=Sweden", new CnContextMapper());
		assertEquals("Some Person", result);
	}

	public void testSearch() {
		List<String> cns = ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", new CnContextMapper());

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
	}

	public void testModifyAttributes() {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=Some Person,ou=company1,c=Sweden");

		ctx.setAttributeValue("description", "updated description");
		ctx.setAttributeValue("telephoneNumber", "0000001");

		ldapTemplate.modifyAttributes(ctx);

		// verify that the data was properly updated.
		ldapTemplate.lookup("cn=Some Person,ou=company1,c=Sweden", new ParameterizedContextMapper<Object>() {
			public Object mapFromContext(Object ctx) {
				DirContextAdapter adapter = (DirContextAdapter) ctx;
				assertEquals("updated description", adapter.getStringAttribute("description"));
				assertEquals("0000001", adapter.getStringAttribute("telephoneNumber"));
				return null;
			}
		});
	}

	private static class CnContextMapper implements ParameterizedContextMapper<String> {

		public String mapFromContext(Object ctx) {
			DirContextAdapter adapter = (DirContextAdapter) ctx;

			return adapter.getStringAttribute("cn");
		}
	}

}
