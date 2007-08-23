/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.entity.ITestPerson;
import org.springframework.ldap.odm.entity.ITestRole;

import java.util.List;


public class LdapDaoITest extends AbstractLdapTemplateIntegrationTest
{
    protected static final Log LOGGER = LogFactory.getLog(LdapDaoITest.class);
    protected LdapDao ldapDao;
    private ITestPerson testPerson;


    protected String[] getConfigLocations()
    {
        LOGGER.debug("********************INIT TEST CLASS**************************");
        return new String[]{"beans.xml"};
    }
                     
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        createPerMethodConsumer();
    }

    public void setLdapDao(LdapDao ldapDao)
    {
        this.ldapDao = ldapDao;
    }

    public void createPerMethodConsumer()
    {
        LOGGER.debug("********************CREATE PER METHOD CONSUMER**************************");
        testPerson = new ITestPerson();
        testPerson.setIdentifier("fred" + System.currentTimeMillis());
        testPerson.setEmailAddress("fred@fred.com");
        testPerson.setFullName("Fred (Freddy) Fredrickson");
        testPerson.setLastName("Fredrickson");
        testPerson.setGivenName("Fred");
        testPerson.setPassword("test1234".getBytes());
    }

    public void testCreate()
    {
        try
        {
            ldapDao.create(testPerson);
        }
        catch (Exception unexpected)
        {
            Assert.fail(unexpected.getMessage());
        }
    }


    public void testCreateOrUpdate()
    {
        try
        {
            ldapDao.create(testPerson);
            ldapDao.createOrUpdate(testPerson);
        }
        catch (Exception unexpected)
        {
            Assert.fail(unexpected.getMessage());
        }
    }


    public void testFindByNamingAttribute()
    {
        ldapDao.create(testPerson);
        ITestPerson retrieved = (ITestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(), ITestPerson.class);
        LOGGER.debug(retrieved);
        Assert.assertNotNull("Should've found the created entity.", retrieved);
    }


    public void testLoadsReferences()
    {
        ITestRole webUser = (ITestRole) ldapDao.findByNamingAttribute("webUser", ITestRole.class);     
        webUser.addMember(testPerson);
        ldapDao.update(webUser);

        ITestRole updated = (ITestRole) ldapDao.findByNamingAttribute("webUser", ITestRole.class);
        LOGGER.debug(updated);
        Assert.assertTrue(updated.getMembers().contains(testPerson));        
    }

    public void testFindByDn()
    {
        ldapDao.create(testPerson);
        DistinguishedName dn = new DistinguishedName("uid=" + testPerson.getIdentifier() + ",ou=people");
        ITestPerson retrieved = (ITestPerson) ldapDao.findByDn(dn, ITestPerson.class);
        LOGGER.debug(retrieved);
        Assert.assertNotNull("Should've found the created entity.", retrieved);
    }

    public void testFindByDnReturnsNullWhenNameNotFound()
    {
        DistinguishedName dn = new DistinguishedName("uid=" + testPerson.getIdentifier() + ",ou=people");
        ITestPerson retrieved = (ITestPerson) ldapDao.findByDn(dn, ITestPerson.class);
        LOGGER.debug(retrieved);
        Assert.assertNull(retrieved);
    }


    public void testCreateThrowsExceptionWhenNameAlreadyBound()
    {
        try
        {
            ldapDao.create(testPerson);
            ldapDao.create(testPerson);
            Assert.fail("Should throw DaoException when name already bound.");
        }
        catch (Exception expected)
        {
            Assert.assertTrue("Exception type doesn't match expected. ( got: "
                    + expected.getMessage(),
                    expected instanceof NameAlreadyBoundException);
        }
    }

    public void testFindAll()
    {
        ldapDao.create(testPerson);
        List results = ldapDao.findAll(ITestPerson.class);
        Assert.assertTrue(results.size() > 0);
    }

    public void testFilterByBeanProperty()
    {
        ldapDao.create(testPerson);

        List results = ldapDao.filterByBeanProperty(
                "emailAddress", testPerson.getEmailAddress(), ITestPerson.class);

        LOGGER.debug("filterByBeanProperty returned: " + results.size() + " results.");
        Assert.assertTrue("Filter should return collection containing more than one result. ",
                results.size() > 0);
    }

    public void testUpdate()
    {
        try
        {
            ldapDao.create(testPerson);
            String newEmail = "new@email.com";
            testPerson.setEmailAddress(newEmail);
            ldapDao.update(testPerson);
            ITestPerson retrieved = (ITestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(),
                    ITestPerson.class);
            Assert.assertEquals("Email name not updated.", retrieved.getEmailAddress(), newEmail);
        }
        catch (Exception unexpected)
        {
            Assert.fail("Unable to update the details of an existing user: " + unexpected.getMessage());
        }
    }

    public void testUpdateThrowsExceptionWhenNameNotBound()
    {
        try
        {
            String newEmail = "new@email.com";
            testPerson.setEmailAddress(newEmail);
            ldapDao.update(testPerson);
            Assert.fail("Should've failed since the entry doesn't exist");
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof NameNotFoundException);
        }
    }

    public void testDelete()
    {
        ldapDao.create(testPerson);
        LOGGER.debug(testPerson);
        ldapDao.delete(testPerson);
        ITestPerson retrieved = (ITestPerson) ldapDao.findByNamingAttribute(testPerson.getIdentifier(), ITestPerson.class);
        Assert.assertNull("Entity should've been deleted.", retrieved);
    }

}
