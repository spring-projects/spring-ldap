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

package org.springframework.ldap.transaction.compensating.manager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyDao;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Integration tests for {@link org.springframework.ldap.transaction.compensating.manager.ContextSourceAndDataSourceTransactionManager}
 * that tests unbind/rebind of recursive entries.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTransactionSubtreeTestContext.xml" })
public class ContextSourceTransactionManagerSubtreeIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	@Qualifier("dummyDao")
	private DummyDao dummyDao;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Before
	public void prepareTestedInstance() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

    protected Resource getLdifFileResource() {
        return new ClassPathResource("/setup_data_subtree.ldif");
    }

    @Test
    public void testLdap168DeleteRecursively() {
        dummyDao.deleteRecursively("ou=company1,c=Sweden");

        try {
            ldapTemplate.lookup("ou=company1,c=Sweden");
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testLdap168DeleteWithException() {
        try {
            dummyDao.deleteRecursivelyWithException("ou=company1,c=Sweden");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        // Entry should have been restored
        ldapTemplate.lookup("ou=company1,c=Sweden");
    }

    @Test
    public void testLdap244CreateRecursively() {
        dummyDao.createRecursivelyAndUnbindSubnode();
    }

    @Test
    public void testLdap244CreateRecursivelyWithException() {
        try {
            dummyDao.createRecursivelyAndUnbindSubnodeWithException();
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }
    }
}
