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

package org.springframework.ldap.itest.core.support;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTests;
import org.springframework.ldap.itest.NoAdTests;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Integration tests for LdapContextSource.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapContextSourceIntegrationTests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	@Qualifier("contextSource")
	private ContextSource tested;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Test
	public void testGetReadOnlyContext() throws NamingException {
		DirContext ctx = null;

		try {
			ctx = this.tested.getReadOnlyContext();
			assertThat(ctx).isNotNull();
			Hashtable environment = ctx.getEnvironment();
			assertThat(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isFalse();
			assertThat(environment.containsKey(Context.SECURITY_PRINCIPAL)).isTrue();
			assertThat(environment.containsKey(Context.SECURITY_CREDENTIALS)).isTrue();
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception ex) {
					// Never mind this
				}
			}
		}
	}

	@Test
	public void testGetReadWriteContext() throws NamingException {
		DirContext ctx = null;

		try {
			ctx = this.tested.getReadWriteContext();
			assertThat(ctx).isNotNull();
			// Double check to see that we are authenticated.
			Hashtable environment = ctx.getEnvironment();
			assertThat(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isFalse();
			assertThat(environment.containsKey(Context.SECURITY_PRINCIPAL)).isTrue();
			assertThat(environment.containsKey(Context.SECURITY_CREDENTIALS)).isTrue();
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception ex) {
					// Never mind this
				}
			}
		}
	}

	@Test
	@Category(NoAdTests.class)
	public void testGetContext() throws NamingException {
		DirContext ctx = null;
		try {
			String expectedPrincipal = "cn=Some Person,ou=company1,ou=Sweden," + base;
			String expectedCredentials = "password";
			ctx = this.tested.getContext(expectedPrincipal, expectedCredentials);
			assertThat(ctx).isNotNull();
			// Double check to see that we are authenticated, and that we did not receive
			// a connection eligible for connection pooling.
			Hashtable environment = ctx.getEnvironment();
			assertThat(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isFalse();
			assertThat(environment.get(Context.SECURITY_PRINCIPAL)).isEqualTo(expectedPrincipal);
			assertThat(environment.get(Context.SECURITY_CREDENTIALS)).isEqualTo(expectedCredentials);
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception ex) {
					// Never mind this
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	@Category(NoAdTests.class)
	public void verifyAuthenticate() {
		EqualsFilter filter = new EqualsFilter("cn", "Some Person2");
		List<String> results = this.ldapTemplate.search("", filter.toString(), new DnContextMapper());
		if (results.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}

		DirContext ctx = null;
		try {
			ctx = this.tested.getContext(results.get(0), "password");
			assertThat(true).isTrue();
		}
		catch (Exception ex) {
			fail("Authentication failed");
		}
		finally {
			LdapUtils.closeContext(ctx);
		}
	}

	private final static class DnContextMapper extends AbstractContextMapper<String> {

		@Override
		protected String doMapFromContext(DirContextOperations ctx) {
			return ctx.getNameInNamespace();
		}

	}

}
