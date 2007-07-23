/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

public class AttributeWrapperTest extends TestCase
{

    public void testGetAllAsObject()
    {
        Attribute multiValueAttribute = new BasicAttribute("multiValueAttribute");
        multiValueAttribute.add("value1");
        multiValueAttribute.add("value2");
        multiValueAttribute.add("value3");

        AttributeWrapper attributeWrapper = new AttributeWrapper(multiValueAttribute);
        try
        {
            Object[] all = (Object[]) attributeWrapper.getAllAsObject();
            Assert.assertEquals(all[0], "value1");
            Assert.assertEquals(all[1], "value2");
            Assert.assertEquals(all[2], "value3");
        }
        catch (NamingException e)
        {
            Assert.fail();
        }

        Attribute singleValueAttribute = new BasicAttribute("attribute", "value");
        attributeWrapper = new AttributeWrapper(singleValueAttribute);
        try
        {
            Assert.assertEquals(attributeWrapper.getAllAsObject(), "value");
        }
        catch (NamingException e)
        {
            Assert.fail();
        }
    }
}
