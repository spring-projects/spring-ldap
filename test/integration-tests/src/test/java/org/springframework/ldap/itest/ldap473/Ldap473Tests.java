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
		DirContext readOnlyContext = contextSource.getReadOnlyContext();
		assertThat(readOnlyContext).isNotNull();
	}

	@Configuration
	static class ContextSourceConfig {

		@Bean
		public ContextSource contextSource() {
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
		public EmbeddedLdapServerFactoryBean embeddedLdapServer() {
			EmbeddedLdapServerFactoryBean embeddedLdapServer = new EmbeddedLdapServerFactoryBean();
			embeddedLdapServer.setPartitionName("example");
			embeddedLdapServer.setPartitionSuffix("dc=261consulting,dc=com");
			embeddedLdapServer.setPort(9322);
			return embeddedLdapServer;
		}

	}

}
