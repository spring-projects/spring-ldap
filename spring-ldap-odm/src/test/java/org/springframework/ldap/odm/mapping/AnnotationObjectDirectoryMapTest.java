/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import junit.framework.Assert;
import org.springframework.ldap.odm.entity.UnitTestPerson;
import org.springframework.ldap.odm.entity.UnitTestPersonMissingNamingAttributeAnnotation;
import org.springframework.ldap.odm.entity.UnitTestPersonMissingNamingSuffixAnnotation;
import org.springframework.ldap.odm.entity.UnitTestPersonMissingObjectClassesAnnotation;
import org.springframework.ldap.odm.entity.UnitTestPersonSyntaxError1InNamingSuffix;
import org.springframework.ldap.odm.entity.UnitTestPersonSyntaxError2InNamingSuffix;
import org.springframework.ldap.odm.entity.UnitTestPersonSyntaxErrorInObjectClasses;

public class AnnotationObjectDirectoryMapTest extends AbstractObjectDirectoryMapTest
{

    protected void setUp() throws Exception
    {
        super.setUp();
        odm = new AnnotationObjectDirectoryMap(UnitTestPerson.class);
    }

    public void testThrowsExceptionWhenNamingAttributeAnnotationMissing()
    {
        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonMissingNamingAttributeAnnotation.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("The @NamingAttribute annotation is required."));
        }
    }

    public void testThrowsExceptionWhenObjectClassesAnnotationMissing()
    {
        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonMissingObjectClassesAnnotation.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("The @ObjectClasses annotation is required."));
        }
    }

    public void testThrowsExceptionWhenObjectClassesAnnotationHasSyntaxError()
    {
        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonSyntaxErrorInObjectClasses.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("Object classes must be defined as separate elements"));
        }
    }

    public void testThrowsExceptionWhenNamingSuffixAnnotationIsMissing()
    {
        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonMissingNamingSuffixAnnotation.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("The @NamingSuffix annotation is required."));
        }
    }

    public void testThrowsExceptionWhenNamingSuffixAnnotationHasSyntaxError()
    {
        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonSyntaxError1InNamingSuffix.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("Naming suffix elements must be declared separately"));
        }

        try
        {
            odm = new AnnotationObjectDirectoryMap(UnitTestPersonSyntaxError2InNamingSuffix.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            Assert.assertTrue(e.getMessage().contains("Syntax error in naming suffix"));
        }
    }
}
