/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.entity.UnitTestPerson;

import java.util.Arrays;
import java.util.Set;

public abstract class AbstractObjectDirectoryMapTest extends TestCase
{
    protected ObjectDirectoryMap odm;

    public void testAttributeNameFor()
    {
        Assert.assertEquals(odm.attributeNameFor("identifier"), "uid");
    }

    public void testThrowsExceptionWhenClassArgumentIsNull() throws MappingException
    {
        try
        {
            //Exercise code in the abstract base using something instantiable
            odm = new AnnotationObjectDirectoryMap(null);
            fail("Should've thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            //Expected behaviour
        }
    }

    public void testAttributeNames()
    {
        Set<String> attributes = odm.attributeNames();
        Assert.assertTrue(attributes.size() == 11);
    }

    public void testbeanPropertyNameFor()
    {
        Assert.assertEquals(odm.beanPropertyNameFor("uid"), "identifier");
    }

    public void testBeanPropertyNames()
    {
        Set<String> properties = odm.beanPropertyNames();
        Assert.assertTrue(properties.size() == 11);
    }

    public void testGetClazz()
    {
        Assert.assertEquals(odm.getClazz(), UnitTestPerson.class);
    }

    public void testGetNamingAttribute()
    {
        Assert.assertEquals(odm.getNamingAttribute(), "uid");
    }

    public void testGetNamingSuffix()
    {
        Assert.assertEquals(odm.getNamingSuffix(), new DistinguishedName("ou=people"));
    }

    public void testGetObjectClasses()
    {
        Assert.assertTrue(Arrays.equals(odm.getObjectClasses(),
                new String[]{"person", "organizationalPerson", "inetorgperson"}));
    }
}
