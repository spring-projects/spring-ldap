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

package org.springframework.ldap.itest.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyDao;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Integration tests for {@link org.springframework.ldap.transaction.compensating.manager.ContextSourceAndDataSourceTransactionManager}.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/missingLdapAndJdbcTransactionTestContext.xml"})
public class ContextSourceAndDataSourceTransactionManagerLdap179IntegrationTest extends AbstractJUnit4SpringContextTests {

    private static Logger log = LoggerFactory.getLogger(ContextSourceAndDataSourceTransactionManagerLdap179IntegrationTest.class);

    @Autowired
    @Qualifier("dummyDao")
    private DummyDao dummyDao;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void prepareTestedInstance() throws Exception {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @After
    public void cleanup() throws Exception {
        jdbcTemplate.execute("drop table PERSON if exists");
    }


    @Test
    public void verifyThatJdbcTransactionIsClosedIfLdapServerUnavailable_ldap179() {
        try {
            dummyDao.create("Sweden", "company1", "some testperson", "testperson", "some description");
            fail("CannotCreateTransactionException expected");
        } catch (CannotCreateTransactionException expected) {
            assertThat(expected.getCause() instanceof CommunicationException).isTrue();
        }

        // Make sure there is no transaction synchronization
        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();

        try {
            dummyDao.create("Sweden", "company1", "some testperson", "testperson", "some description");
            fail("CannotCreateTransactionException expected");
        } catch (CannotCreateTransactionException expected) {
            assertThat(expected.getCause() instanceof CommunicationException).isTrue();
        }
    }
}
