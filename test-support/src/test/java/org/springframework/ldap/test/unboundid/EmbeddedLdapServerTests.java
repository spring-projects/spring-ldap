/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ldap.test.unboundid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EmbeddedLdapServerTests {

	private int port;

	@Before
	public void setUp() throws IOException {
		this.port = getFreePort();
	}

	@Test
	public void shouldStartAndCloseServer() {
		assertPortIsFree(this.port);

		EmbeddedLdapServer server = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", this.port);
		assertPortIsUsed(this.port);

		server.close();
		assertPortIsFree(this.port);
	}

	@Test
	public void shouldStartAndAutoCloseServer() {
		assertPortIsFree(this.port);

		try (EmbeddedLdapServer ignored = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se",
				this.port)) {
			assertPortIsUsed(this.port);
		}
		assertPortIsFree(this.port);
	}

	@Test
	public void shouldStartAndCloseServerViaLdapTestUtils() throws Exception {
		assertPortIsFree(this.port);

		LdapTestUtils.startEmbeddedServer(this.port, "dc=jayway,dc=se", "jayway");
		assertPortIsUsed(this.port);

		LdapTestUtils.shutdownEmbeddedServer();
		assertPortIsFree(this.port);
	}

	@Test
	public void startWhenNewEmbeddedServerThenException() {
		EmbeddedLdapServer server = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", this.port);
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(server::start);
	}

	@Test
	public void startWhenUnstartedThenWorks() throws Exception {
		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jayway,dc=se");
		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", this.port));
		InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
		try (EmbeddedLdapServer server = new EmbeddedLdapServer(ds)) {
			server.start();
			assertPortIsUsed(this.port);
		}
	}

	@Test
	public void startWhenAlreadyStartedThenFails() throws Exception {
		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jayway,dc=se");
		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", this.port));
		InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
		try (EmbeddedLdapServer server = new EmbeddedLdapServer(ds)) {
			server.start();
			assertPortIsUsed(this.port);
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(server::start);
		}
	}

	@Test
	public void shouldBuildButNotStartTheServer() {
		EmbeddedLdapServer.withPartitionSuffix("dc=jayway,dc=se").port(this.port).build();
		assertPortIsFree(this.port);
	}

	@Test
	public void shouldBuildTheServerWithCustomPort() {
		EmbeddedLdapServer.Builder serverBuilder = EmbeddedLdapServer.withPartitionSuffix("dc=jayway,dc=se")
			.port(this.port);

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();
			assertPortIsUsed(this.port);
		}
		assertPortIsFree(this.port);
	}

	@Test
	public void shouldBuildLdapServerAndApplyCustomConfiguration() throws IOException {
		String tempLogFile = Files.createTempFile("ldap-log-", ".txt").toAbsolutePath().toString();

		EmbeddedLdapServer.Builder serverBuilder = EmbeddedLdapServer.withPartitionSuffix("dc=jayway,dc=se")
			.port(this.port)
			.configurationCustomizer((config) -> config.setCodeLogDetails(tempLogFile, true));

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();

			ldapTemplate("dc=jayway,dc=se", this.port)
				.search(LdapQueryBuilder.query().where("objectclass").is("person"), new AttributesMapper<>() {
					public String mapFromAttributes(Attributes attrs) throws NamingException {
						return (String) attrs.get("cn").get();
					}
				});
		}

		assertThat(Path.of(tempLogFile))
			.as("Applying the custom configuration should create a log file and populate it with the request")
			.isNotEmptyFile();
	}

	static void assertPortIsFree(int port) {
		assertThat(isPortOpen(port)).isFalse();
	}

	static void assertPortIsUsed(int port) {
		assertThat(isPortOpen(port)).isTrue();
	}

	static boolean isPortOpen(int port) {
		try (Socket ignored = new Socket("localhost", port)) {
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	static int getFreePort() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
	}

	static LdapTemplate ldapTemplate(String base, int port) {
		LdapContextSource ctx = new LdapContextSource();
		ctx.setBase(base);
		ctx.setUrl("ldap://127.0.0.1:" + port);
		ctx.setUserDn("uid=admin,ou=system");
		ctx.setPassword("secret");
		ctx.afterPropertiesSet();
		return new LdapTemplate(ctx);
	}

}
