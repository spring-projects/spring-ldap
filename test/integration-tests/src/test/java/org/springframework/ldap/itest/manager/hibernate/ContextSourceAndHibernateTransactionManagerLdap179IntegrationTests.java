/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.itest.manager.hibernate;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPerson;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPersonDao;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for
 * {@link org.springframework.ldap.transaction.compensating.manager.ContextSourceAndHibernateTransactionManager}.
 *
 * @author Hans Westerbeek
 */
@ContextConfiguration(locations = { "/conf/missingLdapAndHibernateTransactionTestContext.xml" })
public class ContextSourceAndHibernateTransactionManagerLdap179IntegrationTests
		extends AbstractJUnit4SpringContextTests {

	private static Logger log = LoggerFactory
		.getLogger(ContextSourceAndHibernateTransactionManagerLdap179IntegrationTests.class);

	@Autowired
	@Qualifier("dummyDao")
	private OrgPersonDao dummyDao;

	@Before
	public void prepareTest() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void testCreate() {
		OrgPerson person = new OrgPerson();

		person.setId(2);
		person.setDescription("some description");
		person.setFullname("Some testperson");
		person.setLastname("testperson");
		person.setCountry("Sweden");
		person.setCompany("company1");

		try {
			this.dummyDao.create(person);
		}
		catch (CannotCreateTransactionException expected) {
			assertThat(expected.getCause() instanceof CommunicationException).isTrue();
		}

		// Make sure there is no transaction synchronization
		assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();

		try {
			this.dummyDao.create(person);
		}
		catch (CannotCreateTransactionException expected) {
			assertThat(expected.getCause() instanceof CommunicationException).isTrue();
		}
	}

}
