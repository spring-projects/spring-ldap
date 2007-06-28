package org.springframework.ldap.core.simple;

import java.util.List;

import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.core.DirContextAdapter;

public class SimpleLdapTemplateTest extends AbstractLdapTemplateIntegrationTest {

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

	public void testSearch1() {
		List<String> cns = ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", new CnContextMapper());

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
	}

	private static class CnContextMapper implements ParametrizedContextMapper<String> {

		public String mapFromContext(Object ctx) {
			DirContextAdapter adapter = (DirContextAdapter) ctx;

			return adapter.getStringAttribute("cn");
		}
	}

}
