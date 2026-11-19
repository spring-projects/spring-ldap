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

package org.springframework.ldap.itest.openldap;

import javax.naming.directory.DirContext;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies simple bind as an ordinary user, including failed binds, against OpenLDAP.
 *
 * @author Josh Cummings
 */
class SimpleBindITests {

	private static final String LEELA_DN = "cn=Turanga Leela,ou=people," + OpenLdapContainer.BASE;

	private final OpenLdapContainer openLdap = OpenLdapContainer.shared();

	@Test
	void getContextWhenCorrectPasswordThenBinds() throws Exception {
		LdapContextSource contextSource = this.openLdap.contextSource(null);

		DirContext context = contextSource.getContext(LEELA_DN, "leela");

		try {
			assertThat(context).isNotNull();
		}
		finally {
			context.close();
		}
	}

	@Test
	void getContextWhenWrongPasswordThenAuthenticationException() {
		LdapContextSource contextSource = this.openLdap.contextSource(null);

		assertThatExceptionOfType(AuthenticationException.class)
			.isThrownBy(() -> contextSource.getContext(LEELA_DN, "not-leelas-password"));
	}

	// Spring LDAP does not itself reject an empty password; JNDI would turn it into an
	// unauthenticated bind. This test therefore also depends on OpenLDAP refusing
	// unauthenticated binds (its default), so that a user cannot get in with no password.
	@Test
	void getContextWhenEmptyPasswordThenAuthenticationException() {
		LdapContextSource contextSource = this.openLdap.contextSource(null);

		assertThatExceptionOfType(AuthenticationException.class)
			.isThrownBy(() -> contextSource.getContext(LEELA_DN, ""));
	}

	@Test
	void authenticateWhenCorrectPasswordThenTrue() {
		LdapTemplate ldap = new LdapTemplate(this.openLdap.contextSource(null));

		assertThat(ldap.authenticate("ou=people", "(uid=leela)", "leela")).isTrue();
	}

	@Test
	void authenticateWhenWrongPasswordThenFalse() {
		LdapTemplate ldap = new LdapTemplate(this.openLdap.contextSource(null));

		assertThat(ldap.authenticate("ou=people", "(uid=leela)", "not-leelas-password")).isFalse();
	}

	@Test
	void authenticateWhenUnknownUserThenFalse() {
		LdapTemplate ldap = new LdapTemplate(this.openLdap.contextSource(null));

		assertThat(ldap.authenticate("ou=people", "(uid=nobody)", "leela")).isFalse();
	}

}
