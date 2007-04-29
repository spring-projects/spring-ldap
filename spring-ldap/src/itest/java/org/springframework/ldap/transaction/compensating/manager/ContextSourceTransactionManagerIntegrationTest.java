/*
 * Copyright 2005-2007 the original author or authors.
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

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapServerManager;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceAndDataSourceTransactionManager;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Integration tests for {@link ContextSourceAndDataSourceTransactionManager}.
 * 
 * @author Mattias Arthursson
 */
public class ContextSourceTransactionManagerIntegrationTest extends
        AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory
            .getLog(ContextSourceTransactionManagerIntegrationTest.class);

    public ContextSourceTransactionManagerIntegrationTest() {
        setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
    }

    private DummyDao dummyDao;

    private LdapTemplate ldapTemplate;

    private LdapServerManager ldapServerManager;

    public void setLdapServerManager(LdapServerManager ldapServerManager) {
        this.ldapServerManager = ldapServerManager;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setDummyDao(DummyDao dummyDaoImpl) {
        this.dummyDao = dummyDaoImpl;
    }

    protected String[] getConfigLocations() {
        return new String[] { "conf/ldapTemplateTransactionTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }

        ldapServerManager.cleanAndSetup("setup_data.ldif");
    }

    protected void onTearDown() throws Exception {
    }

    public void testCreateWithException() {
        try {
            dummyDao.createWithException("Sweden", "company1",
                    "some testperson", "testperson", "some description");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        log.debug("Verifying result");

        // Verify that no entry was created
        try {
            ldapTemplate.lookup("cn=some testperson, ou=company1, c=Sweden");
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }
    }

    public void testCreate() {
        dummyDao.create("Sweden", "company1", "some testperson", "testperson",
                "some description");

        log.debug("Verifying result");
        Object ldapResult = ldapTemplate
                .lookup("cn=some testperson, ou=company1, c=Sweden");
        assertNotNull(ldapResult);
    }

    public void testUpdateWithException() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        try {
            dummyDao.updateWithException(dn, "Some Person", "Updated Person",
                    "Updated description");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        log.debug("Verifying result");

        Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Person", attributes.get("sn").get());
                assertEquals("Sweden, Company1, Some Person", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(ldapResult);
    }

    public void testUpdate() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        dummyDao.update(dn, "Some Person", "Updated Person",
                "Updated description");

        log.debug("Verifying result");
        Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Updated Person", attributes.get("sn").get());
                assertEquals("Updated description", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(ldapResult);
    }

    public void testUpdateAndRenameWithException() {
        String dn = "cn=Some Person2,ou=company1,c=Sweden";
        String newDn = "cn=Some Person2,ou=company2,c=Sweden";
        try {
            // Perform test
            dummyDao.updateAndRenameWithException(dn, newDn,
                    "Updated description");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        // Verify that entry was not moved.
        try {
            ldapTemplate.lookup(newDn);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

        // Verify that original entry was not updated.
        Object object = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Sweden, Company1, Some Person2", attributes.get(
                        "description").get());
                return new Object();
            }
        });
        assertNotNull(object);
    }

    public void testUpdateAndRename() {
        String dn = "cn=Some Person2,ou=company1,c=Sweden";
        String newDn = "cn=Some Person2,ou=company2,c=Sweden";
        // Perform test
        dummyDao.updateAndRename(dn, newDn, "Updated description");

        // Verify that entry was moved and updated.
        Object object = ldapTemplate.lookup(newDn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Updated description", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(object);
    }

    public void testModifyAttributesWithException() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        try {
            // Perform test
            dummyDao.modifyAttributesWithException(dn, "Updated lastname",
                    "Updated description");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        // Verify result - check that the operation was properly rolled back
        Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Person", attributes.get("sn").get());
                assertEquals("Sweden, Company1, Some Person", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(result);
    }

    public void testModifyAttributes() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        // Perform test
        dummyDao
                .modifyAttributes(dn, "Updated lastname", "Updated description");

        // Verify result - check that the operation was not rolled back
        Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Updated lastname", attributes.get("sn").get());
                assertEquals("Updated description", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(result);
    }

    public void testUnbindWithException() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        try {
            // Perform test
            dummyDao.unbindWithException(dn, "Some Person");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        // Verify result - check that the operation was properly rolled back
        Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                // Just verify that the entry still exists.
                return new Object();
            }
        });

        assertNotNull(ldapResult);
    }

    public void testUnbind() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        // Perform test
        dummyDao.unbind(dn, "Some Person");

        try {
            // Verify result - check that the operation was not rolled back
            ldapTemplate.lookup(dn);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }

    }
}
