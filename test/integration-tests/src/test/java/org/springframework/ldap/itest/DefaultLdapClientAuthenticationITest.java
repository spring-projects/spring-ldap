/*
 * Copyright 2005-2023 the original author or authors.
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

import javax.naming.NamingException;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.support.LookupAttemptingCallback;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link LdapClient}'s authenticate methods.
 *
 * @author Josh Cummings
 */
@ContextConfiguration(locations = { "/conf/ldapClientTestContext.xml" })
public class DefaultLdapClientAuthenticationITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapClient tested;

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticate() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().base(LdapUtils.emptyLdapName()).filter(filter);
		tested.authenticate().query(query).password("password").execute();
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithLdapQuery() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().where("objectclass").is("person").and("uid").is("some.person3");
		tested.authenticate().query(query).password("password").execute();
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithInvalidPassword() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		assertThatExceptionOfType(AuthenticationException.class)
				.isThrownBy(() -> tested.authenticate().query(query).password("invalidpassword").execute());
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithLdapQueryAndInvalidPassword() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().where("objectclass").is("person").and("uid").is("some.person3");
		assertThatExceptionOfType(AuthenticationException.class)
				.isThrownBy(() -> tested.authenticate().query(query).password("invalidpassword").execute());
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithLookupOperationPerformedOnAuthenticatedContext() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		AuthenticatedLdapEntryContextCallback contextCallback = (ctx, entry) -> {
			try {
				DirContextAdapter adapter = (DirContextAdapter) ctx.lookup(entry.getRelativeDn());
				assertThat(adapter.getStringAttribute("cn")).isEqualTo("Some Person3");
			}
			catch (NamingException e) {
				throw new RuntimeException("Failed to lookup " + entry.getRelativeDn(), e);
			}
		};
		tested.authenticate().query(query).password("password").execute((ctx, entry) -> {
			contextCallback.executeWithContext(ctx, entry);
			return null;
		});
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithLdapQueryAndMapper() {
		LdapQuery query = LdapQueryBuilder.query().where("objectclass").is("person").and("uid").is("some.person3");
		DirContextOperations ctx = tested.authenticate().query(query).password("password")
				.execute(new LookupAttemptingCallback());

		assertThat(ctx).isNotNull();
		assertThat(ctx.getStringAttribute("uid")).isEqualTo("some.person3");
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithLdapQueryAndMapperAndInvalidPassword() {
		LdapQuery query = LdapQueryBuilder.query().where("objectclass").is("person").and("uid").is("some.person3");
		assertThatExceptionOfType(AuthenticationException.class).isThrownBy(() -> tested.authenticate().query(query)
				.password("invalidpassword").execute(new LookupAttemptingCallback()));
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithInvalidPasswordAndCollectedException() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		assertThatExceptionOfType(AuthenticationException.class)
				.isThrownBy(() -> tested.authenticate().query(query).password("invalidpassword").execute());
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithFilterThatDoesNotMatchAnything() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"))
				.and(new EqualsFilter("uid", "some.person.that.isnt.there"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		assertThatExceptionOfType(EmptyResultDataAccessException.class)
				.isThrownBy(() -> tested.authenticate().query(query).password("password").execute());
	}

	@Test
	@Category(NoAdTest.class)
	public void testAuthenticateWithFilterThatMatchesSeveralEntries() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("cn", "Some Person"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class)
				.isThrownBy(() -> tested.authenticate().query(query).password("password").execute());
	}

	@Test
	@Category(NoAdTest.class)
	public void testLookupAttemptingCallback() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", "some.person3"));
		LdapQuery query = LdapQueryBuilder.query().filter(filter);
		LookupAttemptingCallback callback = new LookupAttemptingCallback();
		tested.authenticate().query(query).password("password").execute(callback);
	}

}
