/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.itest.core.simple;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTests;
import org.springframework.ldap.itest.NoAdTests;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ContextConfiguration(locations = { "/conf/simpleLdapTemplateTestContext.xml" })
public class SimpleLdapTemplateITests extends AbstractLdapTemplateIntegrationTests {

	private static String DN_STRING = "cn=Some Person4,ou=company1,ou=Sweden";

	private static LdapName DN = LdapUtils.newLdapName("cn=Some Person4,ou=company1,ou=Sweden");

	@Autowired
	private LdapTemplate ldapTemplate;

	@Test
	public void testLookup() {
		String result = this.ldapTemplate.lookup("cn=Some Person,ou=company1,ou=Sweden", new CnContextMapper());
		assertThat(result).isEqualTo("Some Person");
	}

	@Test
	public void testLookupName() {
		String result = this.ldapTemplate.lookup(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden"),
				new CnContextMapper());
		assertThat(result).isEqualTo("Some Person");
	}

	@Test
	public void testSearch() {
		List<String> cns = this.ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", new CnContextMapper());

		assertThat(cns).hasSize(1);
		assertThat(cns.get(0)).isEqualTo("Some Person3");
	}

	@Test
	public void testSearchForObject() {
		String cn = this.ldapTemplate.searchForObject("", "(&(objectclass=person)(sn=Person3))", new CnContextMapper());
		assertThat(cn).isEqualTo("Some Person3");
	}

	@Test
	public void testSearchProcessor() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		DummyDirContextProcessor processor = new DummyDirContextProcessor();

		List<String> cns = this.ldapTemplate.search("", "(&(objectclass=person)(sn=Person3))", searchControls,
				new CnContextMapper(), processor);

		assertThat(cns).hasSize(1);
		assertThat(cns.get(0)).isEqualTo("Some Person3");
		assertThat(processor.isPreProcessCalled()).isTrue();
		assertThat(processor.isPostProcessCalled()).isTrue();
	}

	@Test
	public void testSearchProcessorName() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		DummyDirContextProcessor processor = new DummyDirContextProcessor();

		List<String> cns = this.ldapTemplate.search(LdapUtils.emptyLdapName(), "(&(objectclass=person)(sn=Person3))",
				searchControls, new CnContextMapper(), processor);

		assertThat(cns).hasSize(1);
		assertThat(cns.get(0)).isEqualTo("Some Person3");
		assertThat(processor.isPreProcessCalled()).isTrue();
		assertThat(processor.isPostProcessCalled()).isTrue();
	}

	@Test
	public void testSearchName() {
		List<String> cns = this.ldapTemplate.search(LdapUtils.emptyLdapName(), "(&(objectclass=person)(sn=Person3))",
				new CnContextMapper());

		assertThat(cns).hasSize(1);
		assertThat(cns.get(0)).isEqualTo("Some Person3");
	}

	@Test
	public void testModifyAttributes() {
		DirContextOperations ctx = this.ldapTemplate.lookupContext("cn=Some Person,ou=company1,ou=Sweden");

		ctx.setAttributeValue("description", "updated description");
		ctx.setAttributeValue("telephoneNumber", "0000001");

		this.ldapTemplate.modifyAttributes(ctx);

		// verify that the data was properly updated.
		this.ldapTemplate.lookup("cn=Some Person,ou=company1,ou=Sweden", new ContextMapper<>() {
			public Object mapFromContext(Object ctx) {
				DirContextAdapter adapter = (DirContextAdapter) ctx;
				assertThat(adapter.getStringAttribute("description")).isEqualTo("updated description");
				assertThat(adapter.getStringAttribute("telephoneNumber")).isEqualTo("0000001");
				return null;
			}
		});
	}

	@Test
	public void testModifyAttributesName() {
		DirContextOperations ctx = this.ldapTemplate
			.lookupContext(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden"));

		ctx.setAttributeValue("description", "updated description");
		ctx.setAttributeValue("telephoneNumber", "0000001");

		this.ldapTemplate.modifyAttributes(ctx);

		// verify that the data was properly updated.
		this.ldapTemplate.lookup("cn=Some Person,ou=company1,ou=Sweden", new ContextMapper<>() {
			public Object mapFromContext(Object ctx) {
				DirContextAdapter adapter = (DirContextAdapter) ctx;
				assertThat(adapter.getStringAttribute("description")).isEqualTo("updated description");
				assertThat(adapter.getStringAttribute("telephoneNumber")).isEqualTo("0000001");
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

		this.ldapTemplate.bind(DN_STRING, adapter, null);
		verifyBoundCorrectData();
		this.ldapTemplate.unbind(DN_STRING);
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindName() {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		this.ldapTemplate.bind(DN, adapter, null);
		verifyBoundCorrectData();
		this.ldapTemplate.unbind(DN);
		verifyCleanup();
	}

	@Test
	public void testBindAndUnbindWithDirContextAdapter() {
		DirContextAdapter adapter = new DirContextAdapter(DN);
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");

		this.ldapTemplate.bind(adapter);
		verifyBoundCorrectData();
		this.ldapTemplate.unbind(DN);
		verifyCleanup();
	}

	@Test
	@Category(NoAdTests.class)
	public void testAuthenticate() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		assertThat(this.ldapTemplate.authenticate("", filter.toString(), "password")).isTrue();
	}

	private void verifyBoundCorrectData() {
		DirContextOperations result = this.ldapTemplate.lookupContext(DN_STRING);
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person4");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person4");
	}

	private void verifyCleanup() {
		try {
			this.ldapTemplate.lookupContext(DN_STRING);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}
	}

	private static final class CnContextMapper implements ContextMapper<String> {

		@Override
		public String mapFromContext(Object ctx) {
			DirContextAdapter adapter = (DirContextAdapter) ctx;

			return adapter.getStringAttribute("cn");
		}

	}

	private static final class DummyDirContextProcessor implements DirContextProcessor {

		private boolean preProcessCalled;

		private boolean postProcessCalled;

		boolean isPreProcessCalled() {
			return this.preProcessCalled;
		}

		boolean isPostProcessCalled() {
			return this.postProcessCalled;
		}

		@Override
		public void postProcess(DirContext ctx) throws NamingException {
			this.preProcessCalled = true;
		}

		@Override
		public void preProcess(DirContext ctx) throws NamingException {
			this.postProcessCalled = true;
		}

	}

}
