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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * This test only works against in-process Apache DS server, regardless of configured
 * profile.
 */
@ContextConfiguration(locations = { "/conf/ldapTemplatePooledTestContext.xml" })
public class LdapTemplatePooledITests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private LdapTemplate tested;

	@Autowired
	private ContextSource contextSource;

	@Value("${base}")
	protected String base;

	@After
	public void cleanup() throws Exception {
		LdapTestUtils.shutdownEmbeddedServer();
	}

	/**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory}) being set in
	 * the ContextSource.
	 */
	@Test
	public void verifyThatInvalidConnectionIsAutomaticallyPurged() throws Exception {
		LdapTestUtils.startEmbeddedServer(1888, "dc=261consulting,dc=com", "jayway");
		LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.emptyLdapName(),
				new ClassPathResource("/setup_data.ldif"));

		DirContextOperations result = tested.lookupContext("cn=Some Person2, ou=company1,ou=Sweden");
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person2");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
		assertThat(result.getStringAttribute("description")).isEqualTo("Sweden, Company1, Some Person2");

		// Shutdown server and kill all existing connections
		LdapTestUtils.shutdownEmbeddedServer();
		LdapTestUtils.startEmbeddedServer(1888, "dc=261consulting,dc=com", "jayway");

		try {
			tested.lookup("cn=Some Person2, ou=company1,ou=Sweden");
			fail("Exception expected");
		}
		catch (Exception expected) {
			// This should fail because the target connection was closed
			assertThat(true).isTrue();
		}

		LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.emptyLdapName(),
				new ClassPathResource("/setup_data.ldif"));
		// But this should be OK, because the dirty connection should have been
		// automatically purged.
		tested.lookup("cn=Some Person2, ou=company1,ou=Sweden");
	}

}
