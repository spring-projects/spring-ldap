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

package org.springframework.ldap.itest.ldap473;

import javax.naming.directory.DirContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.EmbeddedLdapServerFactoryBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eddú Meléndez
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class Ldap473Tests {

	@Autowired
	private ContextSource contextSource;

	@Test
	public void anonymous() {
		DirContext readOnlyContext = this.contextSource.getReadOnlyContext();
		assertThat(readOnlyContext).isNotNull();
	}

	@Configuration
	static class ContextSourceConfig {

		@Bean
		ContextSource contextSource() {
			LdapContextSource contextSource = new LdapContextSource();
			contextSource.setUrl("ldap://localhost:9322");
			contextSource.setUserDn(null);
			contextSource.setPassword(null);
			return contextSource;
		}

	}

	@Configuration
	static class EmbeddedLdapConfig {

		@Bean
		EmbeddedLdapServerFactoryBean embeddedLdapServer() {
			EmbeddedLdapServerFactoryBean embeddedLdapServer = new EmbeddedLdapServerFactoryBean();
			embeddedLdapServer.setPartitionName("example");
			embeddedLdapServer.setPartitionSuffix("dc=261consulting,dc=com");
			embeddedLdapServer.setPort(9322);
			return embeddedLdapServer;
		}

	}

}
