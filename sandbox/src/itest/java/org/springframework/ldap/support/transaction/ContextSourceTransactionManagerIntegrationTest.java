package org.springframework.ldap.support.transaction;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.EntryNotFoundException;
import org.springframework.ldap.LdapServerManager;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class ContextSourceTransactionManagerIntegrationTest extends
        AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory
            .getLog(ContextSourceTransactionManagerIntegrationTest.class);

    public ContextSourceTransactionManagerIntegrationTest() {
        setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
    }

    private DummyDaoImpl dummyDao;

    private LdapTemplate ldapTemplate;

    private LdapServerManager ldapServerManager;

    public void setLdapServerManager(LdapServerManager ldapServerManager) {
        this.ldapServerManager = ldapServerManager;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setDummyDao(DummyDaoImpl dummyDaoImpl) {
        this.dummyDao = dummyDaoImpl;
    }

    protected String[] getConfigLocations() {
        return new String[] { "conf/ldapTemplateTransactionTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        ldapServerManager.cleanAndSetup("setup_data.ldif");
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

        try {
            ldapTemplate.lookup("cn=some testperson, ou=company1, c=Sweden");
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }
    }

    public void testCreate() {
        dummyDao.create("Sweden", "company1", "some testperson", "testperson",
                "some description");

        log.debug("Verifying result");
        ldapTemplate.lookup("cn=some testperson, ou=company1, c=Sweden");
    }

    public void testUpdateWithException() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        try {
            dummyDao.updateWithException(dn, "Updated Person",
                    "Updated description");
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        log.debug("Verifying result");

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

    public void testUpdate() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        dummyDao.update(dn, "Updated Person", "Updated description");

        log.debug("Verifying result");
        Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Updated Person", attributes.get("sn").get());
                assertEquals("Updated description", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(result);
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
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
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
            dummyDao.unbindWithException(dn);
            fail("DummyException expected");
        } catch (DummyException expected) {
            assertTrue(true);
        }

        // Verify result - check that the operation was properly rolled back
        Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                assertEquals("Some Person", attributes.get("cn").get());
                assertEquals("Person", attributes.get("sn").get());
                assertEquals("Sweden, Company1, Some Person", attributes.get(
                        "description").get());
                return new Object();
            }
        });

        assertNotNull(result);
    }

    public void testUnbind() {
        String dn = "cn=Some Person,ou=company1,c=Sweden";
        // Perform test
        dummyDao.unbind(dn);

        try {
            // Verify result - check that the operation was not rolled back
            ldapTemplate.lookup(dn);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

    }
}
