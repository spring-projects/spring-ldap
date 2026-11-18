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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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
 */
final class OpenLdapContainer extends GenericContainer<OpenLdapContainer> {

	static final String BASE = "dc=planetexpress,dc=com";

	static final String ADMIN_DN = "cn=admin," + BASE;

	static final String ADMIN_PASSWORD = "GoodNewsEveryone";

	/** The number of people the image is seeded with. */
	static final int PEOPLE = 7;

	OpenLdapContainer() {
		super(DockerImageName.parse("ghcr.io/rroemhild/docker-test-openldap:master"));
		withCreateContainerCmdModifier((cmd) -> cmd.withPlatform("linux/amd64"));
		withExposedPorts(10389);
		waitingFor(Wait.forLogMessage(".*slapd starting.*\\n", 1));
	}

	LdapContextSource contextSource(DirContextAuthenticationStrategy strategy) {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl("ldap://" + getHost() + ":" + getMappedPort(10389));
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
