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

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * An OpenLDAP server, started with Testcontainers, for verifying behavior that depends on
 * a real LDAP server: paged results and STARTTLS.
 *
 * <p>
 * The image is <a href="https://github.com/rroemhild/docker-test-openldap">
 * rroemhild/docker-test-openldap</a>, which comes seeded with seven people under
 * {@code ou=people,dc=planetexpress,dc=com} and a self-signed certificate. It is only
 * published for {@code linux/amd64}.
 *
 * <p>
 * The image is pinned by digest so that the tests do not change underneath us. To
 * upgrade, resolve the new digest with
 * {@code docker buildx imagetools inspect ghcr.io/rroemhild/docker-test-openldap:master}.
 *
 * <p>
 * A single container is shared by every test class in the module. It is started on first
 * use and removed by Testcontainers' resource reaper when the JVM exits. The tests only
 * read from it, so sharing is safe.
 */
final class OpenLdapContainer extends GenericContainer<OpenLdapContainer> {

	static final String BASE = "dc=planetexpress,dc=com";

	static final String ADMIN_DN = "cn=admin," + BASE;

	static final String ADMIN_PASSWORD = "GoodNewsEveryone";

	/** The number of people the image is seeded with. */
	static final int PEOPLE = 7;

	private static final String IMAGE = "ghcr.io/rroemhild/docker-test-openldap"
			+ "@sha256:27e88556a13358120ae6df406f0e9ebf9e40c6bc36d5a66dc5c6f174f6d1ea95";

	private static final String CERTIFICATE_PATH = "/etc/ldap/ssl/ldap.crt";

	private static final int PORT = 10389;

	private static OpenLdapContainer instance;

	private OpenLdapContainer() {
		super(DockerImageName.parse(IMAGE));
		withCreateContainerCmdModifier((cmd) -> cmd.withPlatform("linux/amd64"));
		withExposedPorts(PORT);
		// the log line appears before slapd necessarily accepts connections, so also
		// wait for the port
		waitingFor(new WaitAllStrategy().withStrategy(Wait.forLogMessage(".*slapd starting.*\\n", 1))
			.withStrategy(Wait.forListeningPort())
			.withStartupTimeout(Duration.ofMinutes(3)));
	}

	/**
	 * Get the shared container, starting it if necessary.
	 * @return the running container
	 */
	static synchronized OpenLdapContainer shared() {
		if (instance == null) {
			OpenLdapContainer container = new OpenLdapContainer();
			container.start();
			instance = container;
		}
		return instance;
	}

	/**
	 * Create an {@link SSLContext} that trusts exactly the self-signed certificate this
	 * server presents, and nothing else.
	 * @return the pinned {@link SSLContext}
	 * @throws Exception if the certificate cannot be read
	 */
	SSLContext pinnedSslContext() throws Exception {
		byte[] pem = copyFileFromContainer(CERTIFICATE_PATH, (in) -> in.readAllBytes());
		Certificate certificate = CertificateFactory.getInstance("X.509")
			.generateCertificate(new ByteArrayInputStream(pem));
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		trustStore.setCertificateEntry("openldap", certificate);
		TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagers.init(trustStore);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, trustManagers.getTrustManagers(), null);
		return context;
	}

	LdapContextSource contextSource(DirContextAuthenticationStrategy strategy) {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl("ldap://" + getHost() + ":" + getMappedPort(PORT));
		contextSource.setBase(BASE);
		contextSource.setUserDn(ADMIN_DN);
		contextSource.setPassword(ADMIN_PASSWORD);
		contextSource.setPooled(false);
		if (strategy != null) {
			contextSource.setAuthenticationStrategy(strategy);
		}
		contextSource.afterPropertiesSet();
		return contextSource;
	}

}
