/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.dao.exception.DataIntegrityViolationException;
import org.springframework.ldap.odm.dao.exception.EntryNotFoundException;
import org.springframework.ldap.odm.entity.TestPerson;
import org.springframework.ldap.odm.entity.TestRole;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class LdapDaoTest
{

    protected static final Log LOGGER = LogFactory.getLog(LdapDaoTest.class);
    protected LdapDao ldapDao;
    private ClassPathXmlApplicationContext applicationContext;
    private TestPerson testPerson;
    private TestRole validRole;


    @BeforeClass(alwaysRun = true)
    public void initTestClass()
    {
        LOGGER.debug("********************INIT TEST CLASS**************************");

        applicationContext = new ClassPathXmlApplicationContext("beans.xml");
        ldapDao = (LdapDao) applicationContext.getBean("ldapDao");
    }

    @BeforeMethod(alwaysRun = true)
    public void createPerMethodConsumer()
    {
        LOGGER.debug("********************CREATE PER METHOD CONSUMER**************************");
        testPerson = new TestPerson();
        testPerson.setIdentifier("fred" + System.currentTimeMillis());
        testPerson.setEmailAddress("fred@fred.com");
        testPerson.setFullName("Fred (Freddy) Fredrickson");
        testPerson.setLastName("Fredrickson");
        testPerson.setGivenName("Fred");
        testPerson.setPassword("test1234".getBytes());
    }

    @BeforeClass(alwaysRun = true)
    public void createValidRole()
    {
        LOGGER.debug("********************CREATE VALID ROLE**************************");
        validRole = new TestRole();
        validRole.setRoleName("webUser");
        validRole.setDescription("A valid role");
    }


    @Test(groups = "LdapDao")
    public void createEntity()
    {
        try
        {
            ldapDao.create(testPerson);
        }
        catch (Exception unexpected)
        {
            Assert.fail("Creating a new user failed: " + unexpected.getMessage());
        }
    }

    @Test(groups = "LdapDao")
    public void createOrUpdateEntity()
    {
        try
        {
            ldapDao.create(testPerson);
            ldapDao.createOrUpdate(testPerson);
        }
        catch (Exception unexpected)
        {
            Assert.fail("Creating a new user failed: " + unexpected.getMessage());
        }
    }


    @Test(groups = "LdapDao")
    public void findByNamingAttribute()
    {
        ldapDao.create(testPerson);
        TestPerson retrieved = (TestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(), TestPerson.class);
        LOGGER.debug(retrieved);
        Assert.assertNotNull(retrieved, "Should've found the created entity.");
    }

    @Test(groups = "LdapDao")
    public void findRoles()
    {
        ldapDao.create(testPerson);
        validRole.setMembers(new TestPerson[]{testPerson});
        ldapDao.createOrUpdate(validRole);
        TestRole role = (TestRole) ldapDao.findByNamingAttribute("webUser", TestRole.class);
        LOGGER.debug(role);
    }

    @Test(groups = "LdapDao")
    public void findByDn()
    {
        ldapDao.create(testPerson);
        DistinguishedName dn = new DistinguishedName("uid=" + testPerson.getIdentifier() + ",ou=people");
        TestPerson retrieved = (TestPerson) ldapDao.findByDn(dn, TestPerson.class);
        LOGGER.debug(retrieved);
        Assert.assertNotNull(retrieved, "Should've found the created entity.");
    }


    @Test(groups = "LdapDao")
    public void createEntityThrowsExceptionWhenNameAlreadyBound()
    {
        try
        {
            ldapDao.create(testPerson);
            ldapDao.create(testPerson);
            Assert.fail("Should throw DaoException when name already bound.");
        }
        catch (Exception expected)
        {
            Assert.assertTrue(expected instanceof DataIntegrityViolationException, "Exception type doesn't match expected. ( got: "
                    + expected.getMessage());
            Assert.assertTrue(expected.getMessage().contains("Name already bound"),
                    "Exception message doesn't match expected.");
        }
    }


    @Test(groups = "LdapDao")
    public void findAll()
    {
        List results = ldapDao.findAll(TestPerson.class);
        Assert.assertTrue(results.size() > 0);
    }

    @Test(groups = "LdapDao")
    public void filterByBeanProperty()
    {
        ldapDao.create(testPerson);
        List results = ldapDao.filterByBeanProperty(testPerson.getEmailAddress(), "EmailAddress", TestPerson.class);
        LOGGER.debug("filterByBeanProperty returned: " + results.size() + " results.");
        Assert.assertTrue(results.size() > 0, "Filter should return collection containing more than one result. ");
    }


    @Test(groups = "LdapDao")
    public void updateEntity()
    {
        try
        {
            ldapDao.create(testPerson);
            String newEmail = "new@email.com";
            testPerson.setEmailAddress(newEmail);
            ldapDao.update(testPerson);
            TestPerson retrieved = (TestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(),
                    TestPerson.class);
            Assert.assertEquals(retrieved.getEmailAddress(), newEmail, "Email name not updated.");
        }
        catch (Exception unexpected)
        {
            Assert.fail("Unable to update the details of an existing user: " + unexpected.getMessage());
        }
    }


    @Test(groups = "LdapDao")
    public void updateEntityThrowsExceptionWhenNameNotBound()
    {
        try
        {
            String newEmail = "new@email.com";
            testPerson.setEmailAddress(newEmail);
            ldapDao.update(testPerson);
            Assert.fail("Should've failed since the entry doesn't exist");
        }
        catch (EntryNotFoundException e)
        {
            Assert.assertTrue(e instanceof EntryNotFoundException);
            Assert.assertTrue(e.getMessage().contains("Entry not found"), "Error message not as expected.");
        }
    }

    @Test(groups = "LdapDao")
    public void deleteEntity()
    {
        ldapDao.create(testPerson);
        LOGGER.debug(testPerson);
        ldapDao.delete(testPerson);
        TestPerson retrieved = (TestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(), TestPerson.class);
        Assert.assertNull(retrieved, "Entity should've been deleted.");
    }


}
