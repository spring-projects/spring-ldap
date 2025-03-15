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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Test;

public class EmbeddedLdapServerBuilderTests {

	private static String tempLogFile;

	@BeforeClass
	public static void before() throws IOException {
		tempLogFile = Files.createTempFile("ldap-log-", ".txt").toAbsolutePath().toString();
	}

	@Test
	public void testServerStartup_withCustomConfig() {
		EmbeddedLdapServerBuilder serverBuilder = EmbeddedLdapServer.builder()
			.withPort(1234)
			.withConfigurationCustomizer(config -> config.setCodeLogDetails(tempLogFile, true));

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();

			// ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
			// "/applicationContext-testContextSource.xml");
			// LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);
			// assertThat(ldapTemplate).isNotNull();
			//
			// ldapTemplate.search(LdapQueryBuilder.query().where("objectclass").is("person"),
			// new AttributesMapper<>() {
			// public String mapFromAttributes(Attributes attrs) throws NamingException {
			// return (String) attrs.get("cn").get();
			// }
			// });

			assertThat(Path.of(tempLogFile)).isNotEmptyFile();
		}
	}

	@Test
	public void shouldBuildButNotStartTheServer() {
		int port = SocketTestUtils.getFreePort();

		EmbeddedLdapServer server = EmbeddedLdapServer.builder().withPort(port).build();

		assertThat(SocketTestUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void shouldBuildTheServerWithCustomPort() {
		int port = SocketTestUtils.getFreePort();

		EmbeddedLdapServerBuilder serverBuilder = EmbeddedLdapServer.builder()
			.withPartitionName("test")
			.withPartitionSuffix("dc=test,dc=se")
			.withPort(port);

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();
			assertThat(SocketTestUtils.isPortOpen(port)).isTrue();
		}
		assertThat(SocketTestUtils.isPortOpen(port)).isFalse();
	}

}
