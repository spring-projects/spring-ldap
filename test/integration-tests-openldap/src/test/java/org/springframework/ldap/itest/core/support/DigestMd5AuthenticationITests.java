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

package org.springframework.ldap.itest.core.support;

import javax.naming.directory.DirContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify DIGEST-MD5 authentication support.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateDigestMd5TestContext.xml" })
public class DigestMd5AuthenticationITests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	@Qualifier("populateContextSource")
	private ContextSource contextSource;

	@Before
	public void prepareTestedInstance() throws Exception {
		LdapTestUtils.cleanAndSetup(this.contextSource, LdapUtils.newLdapName("ou=People"),
				new ClassPathResource("/setup_data.ldif"));
	}

	@After
	public void cleanup() throws Exception {
		LdapTestUtils.clearSubContexts(this.contextSource, LdapUtils.newLdapName("ou=People"));
	}

	@Test
	public void testAuthenticate() {
		DirContext ctxt = this.ldapTemplate.getContextSource().getContext("some.person1", "password");
		assertThat(ctxt).isNotNull();
	}

}
