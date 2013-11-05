/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.itest.core.support;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.NoAdTest;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Integration tests for LdapContextSource.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapContextSourceIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
    @Qualifier("contextSource")
	private ContextSource tested;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Test
	public void testGetReadOnlyContext() throws NamingException {
		DirContext ctx = null;

		try {
			ctx = tested.getReadOnlyContext();
			assertNotNull(ctx);
			Hashtable environment = ctx.getEnvironment();
			assertFalse(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG));
			assertTrue(environment.containsKey(Context.SECURITY_PRINCIPAL));
			assertTrue(environment.containsKey(Context.SECURITY_CREDENTIALS));
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception e) {
					// Never mind this
				}
			}
		}
	}

	@Test
	public void testGetReadWriteContext() throws NamingException {
		DirContext ctx = null;

		try {
			ctx = tested.getReadWriteContext();
			assertNotNull(ctx);
			// Double check to see that we are authenticated.
			Hashtable environment = ctx.getEnvironment();
            assertFalse(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG));
			assertTrue(environment.containsKey(Context.SECURITY_PRINCIPAL));
			assertTrue(environment.containsKey(Context.SECURITY_CREDENTIALS));
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception e) {
					// Never mind this
				}
			}
		}
	}

	@Test
    @Category(NoAdTest.class)
	public void testGetContext() throws NamingException {
		DirContext ctx = null;
		try {
			String expectedPrincipal = "cn=Some Person,ou=company1,ou=Sweden," + base;
			String expectedCredentials = "password";
			ctx = tested.getContext(expectedPrincipal, expectedCredentials);
			assertNotNull(ctx);
			// Double check to see that we are authenticated, and that we did not receive
			// a connection eligible for connection pooling.
			Hashtable environment = ctx.getEnvironment();
            assertFalse(environment.containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG));
			assertEquals(expectedPrincipal, environment.get(Context.SECURITY_PRINCIPAL));
			assertEquals(expectedCredentials, environment.get(Context.SECURITY_CREDENTIALS));
		}
		finally {
			// Always clean up.
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (Exception e) {
					// Never mind this
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
    @Category(NoAdTest.class)
	public void verifyAuthenticate() {
		EqualsFilter filter = new EqualsFilter("cn", "Some Person2");
		List<String> results = ldapTemplate.search("", filter.toString(), new DnContextMapper());
		if (results.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}

		DirContext ctx = null;
		try {
			ctx = tested.getContext(results.get(0), "password");
			assertTrue(true);
		}
		catch (Exception e) {
			fail("Authentication failed");
		}
		finally {
			LdapUtils.closeContext(ctx);
		}
	}

	private final static class DnContextMapper extends AbstractParameterizedContextMapper<String> {
		@Override
		protected String doMapFromContext(DirContextOperations ctx) {
			return ctx.getNameInNamespace();
		}
	}
}
