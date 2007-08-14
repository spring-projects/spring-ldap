/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.classextension.EasyMock;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.typeconversion.LdapTypeConverter;
import org.springframework.ldap.odm.typeconversion.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.entity.UnitTestPerson;

import javax.naming.Name;
import java.util.Date;

public class ObjectDirectoryMapperImplTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(ObjectDirectoryMapperImplTest.class);
    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory refEditorFactory;
    private AnnotationObjectDirectoryMap personOdm;

    protected void setUp() throws Exception
    {
        super.setUp();
        this.typeConverter = new LdapTypeConverter();
        this.refEditorFactory = EasyMock.createMock(ReferencedEntryEditorFactory.class);
        this.personOdm = new AnnotationObjectDirectoryMap(UnitTestPerson.class);
    }


    public void testThrowsIllegalArgumentExceptionWhenNotConfigured() throws MappingException
    {
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(null, typeConverter, refEditorFactory);
            fail("Should've thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, null, refEditorFactory);
            fail("Should've thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, typeConverter, null);
            fail("Should've thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    public void testBuildDn()
    {
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);
            UnitTestPerson person = new UnitTestPerson();
            person.setIdentifier("x332");
            person.setFullName("Mr Bean");
            person.setEmailAddress("mrbean@bean.com");
            person.setDescription(new String[]{"The quick brown fox jumped",
                    "over the lazy dog"});
            Name dn = mapper.buildDn(person);
            Assert.assertEquals("uid=x332, ou=people", dn.toString());

        }
        catch (MappingException e)
        {
            LOGGER.debug(e.getMessage());
            Assert.fail(e.getMessage());
        }
    }

    public void testBuildDnThrowsMappingExceptionWhenArgumentIsNull()
    {
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);
            UnitTestPerson person = null;
            mapper.buildDn(person);
            fail("Should've thrown mapping exception - argument is null");
        }
        catch (MappingException e)
        {
            Assert.assertEquals(e.getMessage(), "Can't build Dn from beanInstance, beanInstance is null");
        }
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);
            String namingAttributeValue = null;
            mapper.buildDn(namingAttributeValue);
            fail("Should've thrown mapping exception - argument is null");
        }
        catch (MappingException e)
        {
            Assert.assertEquals(e.getMessage(), "Can't build Dn from namingAttributeValue, namingAttributeValue is null");
        }
    }

    //CHECKSTYLE:OFF ExecutableStatementCount
    public void testMapToContext() 
    {
        try
        {
            ObjectDirectoryMapperImpl mapper =
                    new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);

            UnitTestPerson person = new UnitTestPerson();
            person.setIdentifier("x232");
            person.setFullName("Mr Bean");
            person.setEmailAddress("bean@bean.com");
            person.setPassword("fred".getBytes());
            person.setAcceptEmails(false);
            person.setResetLogin(new Date(1L));
            person.setCreator(new DistinguishedName(
                    "uid=amAdmin,ou=people,dc=myretsu,dc=com"));
            person.setFailedLogins(3);
            person.setDescription(new String[]{"the quick", "brown fox"});

            EasyMock.replay(refEditorFactory);

            DirContextAdapter ctxAdapter = new DirContextAdapter();
            mapper.mapToContext(person, ctxAdapter);

            Assert.assertEquals("x232", ctxAdapter.getStringAttribute("uid"));
            Assert.assertEquals("Mr Bean", ctxAdapter.getStringAttribute("cn"));
            Assert.assertEquals("bean@bean.com", ctxAdapter.getStringAttribute("mail"));
            Assert.assertEquals("fred", new String((byte[]) ctxAdapter.getObjectAttribute("userpassword")));
            Assert.assertEquals("false", ctxAdapter.getStringAttribute("acceptemails"));
            Assert.assertTrue(ctxAdapter.getStringAttribute("loginresettime")
                    .matches("19700101\\d\\d\\d\\d00.001\\+\\d\\d\\d\\d"));
            Assert.assertEquals("3", ctxAdapter.getStringAttribute("failedlogins"));
            Assert.assertEquals("uid=amAdmin, ou=people, dc=myretsu, dc=com",
                    ctxAdapter.getStringAttribute("creatorname"));
            Assert.assertEquals("the quick", ctxAdapter.getStringAttributes("description")[0]);

            EasyMock.verify(refEditorFactory);

        }
        catch (MappingException e)
        {
            LOGGER.debug(e.getMessage());
            Assert.fail(e.getMessage());
        }
    }
    //CHECKSTYLE:ON ExecutableStatementCount

    public void testMapFromContext() throws MappingException
    {
        ObjectDirectoryMapperImpl mapper =
                new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);
        DirContextAdapter ctxAdapter = new DirContextAdapter();
        ctxAdapter.setAttributeValue("acceptemails", "false");
        ctxAdapter.setAttributeValue("creatorname", "uid=admin, ou=people");
        ctxAdapter.setAttributeValue("mail", "person@person.com");

        EasyMock.replay(refEditorFactory);

        UnitTestPerson person = (UnitTestPerson) mapper.mapFromContext(ctxAdapter);
        Assert.assertEquals(person.getAcceptEmails(), Boolean.FALSE);
        Assert.assertEquals(person.getCreator(), new DistinguishedName("uid=admin, ou=people"));
        Assert.assertEquals(person.getEmailAddress(), "person@person.com");

        EasyMock.verify(refEditorFactory);


    }

    public void testGetObjectDirectoryMap() throws MappingException
    {
        ObjectDirectoryMapperImpl mapper =
                new ObjectDirectoryMapperImpl(personOdm, typeConverter, refEditorFactory);
        Assert.assertNotNull(mapper.getObjectDirectoryMap());
    }


}
