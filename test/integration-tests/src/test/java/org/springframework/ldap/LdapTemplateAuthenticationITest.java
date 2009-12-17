/*
 * Copyright 2005-2008 the original author or authors.
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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CollectingAuthenticationErrorCallback;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the authenticate methods of LdapTemplate.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateAuthenticationITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testAuthenticate() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		assertTrue(tested.authenticate("", filter.toString(), "password"));
	}

	@Test
	public void testAuthenticateWithInvalidPassword() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		assertFalse(tested.authenticate("", filter.toString(), "invalidpassword"));
	}

	@Test
	public void testAuthenticateWithLookupOperationPerformedOnAuthenticatedContext() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		AuthenticatedLdapEntryContextCallback contextCallback = new AuthenticatedLdapEntryContextCallback() {
			public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
				try {
					DirContextAdapter adapter = (DirContextAdapter) ctx.lookup(ldapEntryIdentification.getRelativeDn());
					assertEquals("Some Person3", adapter.getStringAttribute("cn"));
				}
				catch (NamingException e) {
					throw new RuntimeException("Failed to lookup " + ldapEntryIdentification.getRelativeDn(), e);
				}
			}
		};
		assertTrue(tested.authenticate("", filter.toString(), "password", contextCallback));
	}

	@Test
	public void testAuthenticateWithInvalidPasswordAndCollectedException() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		final CollectingAuthenticationErrorCallback errorCallback = new CollectingAuthenticationErrorCallback();
		assertFalse(tested.authenticate("", filter.toString(), "invalidpassword", errorCallback));
		final Exception error = errorCallback.getError();
		assertNotNull("collected error should not be null", error);
		assertTrue("expected org.springframework.ldap.AuthenticationException", error instanceof AuthenticationException);
		assertTrue("expected javax.naming.AuthenticationException", error.getCause() instanceof javax.naming.AuthenticationException);
	}

	@Test
	public void testAuthenticateWithFilterThatDoesNotMatchAnything() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(
				new EqualsFilter("uid", "some.person.that.isnt.there"));
		assertFalse(tested.authenticate("", filter.toString(), "password"));
	}

	@Test
	public void testAuthenticateWithFilterThatMatchesSeveralEntries() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new WhitespaceWildcardsFilter("uid", "some.person"));
		assertFalse(tested.authenticate("", filter.toString(), "password"));
	}
}
