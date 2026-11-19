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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies STARTTLS against OpenLDAP, which presents a self-signed certificate.
 *
 * @author Josh Cummings
 */
class StartTlsITests {

	private final OpenLdapContainer openLdap = OpenLdapContainer.shared();

	@Test
	void searchOverStartTls() throws Exception {
		DefaultTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();
		// the certificate is issued for planetexpress.com, not for the container's host
		// name, so skip host name verification; the certificate itself is pinned
		strategy.setHostnameVerifier((hostname, session) -> true);
		strategy.setSslSocketFactory(this.openLdap.pinnedSslContext().getSocketFactory());
		LdapContextSource contextSource = this.openLdap.contextSource(strategy);

		List<String> result = new LdapTemplate(contextSource).search("ou=people", "(&(objectclass=person)(uid=leela))",
				(AttributesMapper<String>) (attrs) -> attrs.get("cn").get().toString());

		assertThat(result).containsExactly("Turanga Leela");
	}

	@Test
	void searchOverStartTlsWhenCertificateNotTrustedThenFails() {
		// proves that a TLS handshake really happens and is validated: the JVM's default
		// trust store does not contain the server's self-signed certificate
		DefaultTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();
		strategy.setHostnameVerifier((hostname, session) -> true);
		LdapContextSource contextSource = this.openLdap.contextSource(strategy);

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> new LdapTemplate(contextSource)
			.search("ou=people", "(uid=leela)", (AttributesMapper<String>) (attrs) -> "unused"));
	}

}
