/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.transaction.compensating.manager.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPerson;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPersonDao;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link org.springframework.ldap.transaction.compensating.manager.ContextSourceAndHibernateTransactionManager}.
 *
 * @author Hans Westerbeek
 */
@ContextConfiguration(locations = { "/conf/missingLdapAndHibernateTransactionTestContext.xml" })
public class ContextSourceAndHibernateTransactionManagerLdap179IntegrationTest extends AbstractJUnit4SpringContextTests {

	private static Log log = LogFactory.getLog(ContextSourceAndHibernateTransactionManagerLdap179IntegrationTest.class);

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

		person.setId(new Integer(2));
		person.setDescription("some description");
		person.setFullname("Some testperson");
		person.setLastname("testperson");
		person.setCountry("Sweden");
		person.setCompany("company1");

        try {
            this.dummyDao.create(person);
        } catch (CannotCreateTransactionException expected) {
            assertTrue(expected.getCause() instanceof CommunicationException);
        }

        // Make sure there is no transaction synchronization
        assertFalse(TransactionSynchronizationManager.isSynchronizationActive());

        try {
            this.dummyDao.create(person);
        } catch (CannotCreateTransactionException expected) {
            assertTrue(expected.getCause() instanceof CommunicationException);
        }
    }
}
