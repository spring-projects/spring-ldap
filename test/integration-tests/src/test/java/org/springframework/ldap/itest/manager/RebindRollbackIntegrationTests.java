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

package org.springframework.ldap.itest.manager;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.unboundid.EmbeddedLdapServer;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies that a rebind whose bind step (or rename step) fails inside a compensating
 * transaction leaves the directory as it was before the transaction started.
 */
public class RebindRollbackIntegrationTests {

	private static final String DN = "cn=John Doe";

	// DefaultTempEntryRenamingStrategy appends "_temp" to the leftmost RDN value
	private static final String TEMP_DN = "cn=John Doe_temp";

	private EmbeddedLdapServer server;

	private LdapTemplate ldapTemplate;

	private TransactionTemplate tx;

	@Before
	public void setUp() throws Exception {
		this.server = EmbeddedLdapServer.withPartitionSuffix("dc=example,dc=org").port(10389).build();
		this.server.start();

		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl("ldap://localhost:10389");
		contextSource.setBase("dc=example,dc=org");
		contextSource.setUserDn("uid=admin,ou=system");
		contextSource.setPassword("secret");
		contextSource.afterPropertiesSet();
		this.ldapTemplate = new LdapTemplate(new TransactionAwareContextSourceProxy(contextSource));

		ContextSourceTransactionManager txManager = new ContextSourceTransactionManager();
		txManager.setContextSource(contextSource);
		txManager.setRenamingStrategy(new DefaultTempEntryRenamingStrategy());
		txManager.afterPropertiesSet();
		this.tx = new TransactionTemplate(txManager);
	}

	@After
	public void tearDown() {
		this.server.close();
	}

	@Test
	public void failedBindAfterRenameIsRolledBack() throws Exception {
		this.ldapTemplate.bind(DN, null, person("Doe"));

		// a person entry without the required "sn" attribute violates the schema
		Attributes invalid = person(null);

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> this.tx.executeWithoutResult((status) -> this.ldapTemplate.rebind(DN, null, invalid)));

		assertThat(this.ldapTemplate.lookup(DN, (Attributes a) -> a)).isNotNull();
		assertThat(this.ldapTemplate.lookup(DN, (Attributes a) -> a).get("sn").get()).isEqualTo("Doe");
		assertThatExceptionOfType(org.springframework.ldap.NameNotFoundException.class)
			.isThrownBy(() -> this.ldapTemplate.lookup(TEMP_DN));
	}

	@Test
	public void rebindWithPreexistingTemporaryEntryLeavesBothEntriesUntouched() throws Exception {
		this.ldapTemplate.bind(DN, null, person("Doe"));
		this.ldapTemplate.bind(TEMP_DN, null, person("Attacker"));

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(
				() -> this.tx.executeWithoutResult((status) -> this.ldapTemplate.rebind(DN, null, person("New"))));

		assertThat(this.ldapTemplate.lookup(DN, (Attributes a) -> a).get("sn").get()).isEqualTo("Doe");
		assertThat(this.ldapTemplate.lookup(TEMP_DN, (Attributes a) -> a).get("sn").get()).isEqualTo("Attacker");
	}

	private static Attributes person(String sn) {
		BasicAttributes person = new BasicAttributes("objectClass", "person", true);
		person.put(new BasicAttribute("cn", "John Doe"));
		if (sn != null) {
			person.put(new BasicAttribute("sn", sn));
		}
		return person;
	}

}
