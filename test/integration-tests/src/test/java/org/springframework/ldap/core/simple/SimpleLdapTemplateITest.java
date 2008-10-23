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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "/conf/simpleLdapTemplateTestContext.xml" })
public class SimpleLdapTemplateITest extends AbstractLdapTemplateIntegrationTest {
	private static String DN_STRING = "cn=Some Person4,ou=company1,c=Sweden";

	private static DistinguishedName DN = new DistinguishedName("cn=Some Person4,ou=company1,c=Sweden");

	@Autowired
	private SimpleLdapTemplate ldapTemplate;

	@Test
	public void testLookup() {
		String result = ldapTemplate.lookup("cn=Some Person,ou=company1,c=Sweden", new CnContextMapper());
		assertEquals("Some Person", result);
	}

	@Test
	public void testLookupName() {
		String result = ldapTemplate.lookup(new DistinguishedName("cn=Some Person,ou=company1,c=Sweden"),
				new CnContextMapper());
		assertEquals("Some Person", result);
	}

	@Test
	public void testSearch() {
		List<String> cns = ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", new CnContextMapper());

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
	}

	@Test
	public void testSearchProcessor() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		DummyDirContextProcessor processor = new DummyDirContextProcessor();

		List<String> cns = ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", searchControls,
				new CnContextMapper(), processor);

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
		assertTrue(processor.isPreProcessCalled());
		assertTrue(processor.isPostProcessCalled());
	}

	@Test
	public void testSearchProcessorName() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		DummyDirContextProcessor processor = new DummyDirContextProcessor();

		List<String> cns = ldapTemplate.search(DistinguishedName.EMPTY_PATH, "(&(objectclass=person)(sn=Person3))",
				searchControls, new CnContextMapper(), processor);

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
		assertTrue(processor.isPreProcessCalled());
		assertTrue(processor.isPostProcessCalled());
	}

	@Test
	public void testSearchName() {
		List<String> cns = ldapTemplate.search(DistinguishedName.EMPTY_PATH, "(&(objectclass=person)(sn=Person3))",
				new CnContextMapper());

		assertEquals(1, cns.size());
		assertEquals("Some Person3", cns.get(0));
	}

	@Test
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

	@Test
	public void testModifyAttributesName() {
		DirContextOperations ctx = ldapTemplate.lookupContext(new DistinguishedName(
				"cn=Some Person,ou=company1,c=Sweden"));

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

	@Test
	public void testBindAndUnbind() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		ldapTemplate.bind(DN_STRING, adapter, null);
		verifyBoundCorrectData();
		ldapTemplate.unbind(DN_STRING);
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindName() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		ldapTemplate.bind(DN, adapter, null);
		verifyBoundCorrectData();
		ldapTemplate.unbind(DN);
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindWithDirContextAdapter() {
		DirContextAdapter adapter = new DirContextAdapter(DN);
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		ldapTemplate.bind(adapter);
		verifyBoundCorrectData();
		ldapTemplate.unbind(DN);
		verifyCleanup();
	}

	private void verifyBoundCorrectData() {
		DirContextOperations result = ldapTemplate.lookupContext(DN_STRING);
		assertEquals("Some Person4", result.getStringAttribute("cn"));
		assertEquals("Person4", result.getStringAttribute("sn"));
	}

	private void verifyCleanup() {
		try {
			ldapTemplate.lookupContext(DN_STRING);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}
	}

	private static final class CnContextMapper implements ParameterizedContextMapper<String> {

		public String mapFromContext(Object ctx) {
			DirContextAdapter adapter = (DirContextAdapter) ctx;

			return adapter.getStringAttribute("cn");
		}
	}

	private static final class DummyDirContextProcessor implements DirContextProcessor {

		private boolean preProcessCalled;

		private boolean postProcessCalled;

		public boolean isPreProcessCalled() {
			return preProcessCalled;
		}

		public boolean isPostProcessCalled() {
			return postProcessCalled;
		}

		public void postProcess(DirContext ctx) throws NamingException {
			preProcessCalled = true;
		}

		public void preProcess(DirContext ctx) throws NamingException {
			postProcessCalled = true;
		}

	}
}
