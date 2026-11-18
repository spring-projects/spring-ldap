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

import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies STARTTLS against OpenLDAP, which presents a self-signed certificate.
 *
 * @author Mattias Hellborg Arthursson
 */
@Testcontainers
class StartTlsITests {

	@Container
	static OpenLdapContainer openLdap = new OpenLdapContainer();

	@Test
	void searchOverStartTls() throws Exception {
		DefaultTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();
		strategy.setHostnameVerifier((hostname, session) -> true);
		strategy.setSslSocketFactory(trustEverything().getSocketFactory());
		LdapContextSource contextSource = openLdap.contextSource(strategy);

		List<String> result = new LdapTemplate(contextSource).search("ou=people", "(&(objectclass=person)(uid=leela))",
				(AttributesMapper<String>) (attrs) -> attrs.get("cn").get().toString());

		assertThat(result).containsExactly("Turanga Leela");
	}

	private static SSLContext trustEverything() throws Exception {
		TrustManager trustAll = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { trustAll }, null);
		return context;
	}

}
