/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.itest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.CollectingAuthenticationErrorCallback;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LookupAttemptingCallback;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Tests the authenticate methods of LdapTemplate.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateAuthenticationITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Test
    @Category(NoAdTest.class)
	public void testAuthenticate() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		assertThat(tested.authenticate("", filter.toString(), "password")).isTrue();
	}

    @Test
    @Category(NoAdTest.class)
    public void testAuthenticateWithLdapQuery() {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
        tested.authenticate(query()
                .where("objectclass").is("person")
                .and("uid").is("some.person3"),
                "password");
    }

    @Test
    @Category(NoAdTest.class)
	public void testAuthenticateWithInvalidPassword() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		assertThat(tested.authenticate("", filter.toString(), "invalidpassword")).isFalse();
	}

    @Test(expected = AuthenticationException.class)
    @Category(NoAdTest.class)
    public void testAuthenticateWithLdapQueryAndInvalidPassword() {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
        tested.authenticate(query()
                .where("objectclass").is("person")
                .and("uid").is("some.person3"),
                "invalidpassword");
    }

    @Test
    @Category(NoAdTest.class)
	public void testAuthenticateWithLookupOperationPerformedOnAuthenticatedContext() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		AuthenticatedLdapEntryContextCallback contextCallback = new AuthenticatedLdapEntryContextCallback() {
			public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
				try {
					DirContextAdapter adapter = (DirContextAdapter) ctx.lookup(ldapEntryIdentification.getRelativeDn());
					assertThat(adapter.getStringAttribute("cn")).isEqualTo("Some Person3");
				}
				catch (NamingException e) {
					throw new RuntimeException("Failed to lookup " + ldapEntryIdentification.getRelativeDn(), e);
				}
			}
		};
		assertThat(tested.authenticate("", filter.toString(), "password", contextCallback)).isTrue();
	}

    @Test
    @Category(NoAdTest.class)
    public void testAuthenticateWithLdapQueryAndMapper() {
        DirContextOperations ctx = tested.authenticate(query()
                .where("objectclass").is("person")
                .and("uid").is("some.person3"),
                "password",
                new LookupAttemptingCallback());

        assertThat(ctx).isNotNull();
        assertThat(ctx.getStringAttribute("uid")).isEqualTo("some.person3");
    }

    @Test(expected = AuthenticationException.class)
    @Category(NoAdTest.class)
    public void testAuthenticateWithLdapQueryAndMapperAndInvalidPassword() {
        DirContextOperations ctx = tested.authenticate(query()
                .where("objectclass").is("person")
                .and("uid").is("some.person3"),
                "invalidpassword",
                new LookupAttemptingCallback());
    }

    @Test
    @Category(NoAdTest.class)
	public void testAuthenticateWithInvalidPasswordAndCollectedException() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		final CollectingAuthenticationErrorCallback errorCallback = new CollectingAuthenticationErrorCallback();
		assertThat(tested.authenticate("", filter.toString(), "invalidpassword", errorCallback)).isFalse();
		final Exception error = errorCallback.getError();
		assertThat(error).as("collected error should not be null").isNotNull();
		assertThat(error instanceof AuthenticationException).as("expected org.springframework.ldap.AuthenticationException").isTrue();
		assertThat(error.getCause() instanceof javax.naming.AuthenticationException).as("expected javax.naming.AuthenticationException").isTrue();
	}

	@Test
    @Category(NoAdTest.class)
	public void testAuthenticateWithFilterThatDoesNotMatchAnything() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(
				new EqualsFilter("uid", "some.person.that.isnt.there"));
		assertThat(tested.authenticate("", filter.toString(), "password")).isFalse();
	}

	@Test(expected=IncorrectResultSizeDataAccessException.class)
    @Category(NoAdTest.class)
	public void testAuthenticateWithFilterThatMatchesSeveralEntries() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("cn", "Some Person"));
		tested.authenticate("", filter.toString(), "password");
	}

	@Test
    @Category(NoAdTest.class)
	public void testLookupAttemptingCallback() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LookupAttemptingCallback callback = new LookupAttemptingCallback();
		assertThat(tested.authenticate("", filter.encode(), "password", callback)).isTrue();
	}
}
