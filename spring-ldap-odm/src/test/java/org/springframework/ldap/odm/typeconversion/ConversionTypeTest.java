/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DistinguishedName;
import javax.naming.ldap.LdapName;
import java.util.Date;
import java.util.List;

public class ConversionTypeTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(ConversionTypeTest.class);

    public void testListTypes()
    {
        LOGGER.debug(ConversionType.listTypes());
    }

    public void testIsConversionType()
    {
        Assert.assertFalse(ConversionType.isConversionType(List.class));

        Assert.assertTrue(ConversionType.isConversionType(byte[].class));
        Assert.assertTrue(ConversionType.isConversionType(Boolean.class));
        Assert.assertTrue(ConversionType.isConversionType(String.class));
        Assert.assertTrue(ConversionType.isConversionType(String[].class));
        Assert.assertTrue(ConversionType.isConversionType(Date.class));
        Assert.assertTrue(ConversionType.isConversionType(Date[].class));
        Assert.assertTrue(ConversionType.isConversionType(LdapName.class));
        Assert.assertTrue(ConversionType.isConversionType(LdapName[].class));
        Assert.assertTrue(ConversionType.isConversionType(DistinguishedName.class));
        Assert.assertTrue(ConversionType.isConversionType(DistinguishedName[].class));
        Assert.assertTrue(ConversionType.isConversionType(Integer.class));
        Assert.assertTrue(ConversionType.isConversionType(Integer[].class));
        Assert.assertTrue(ConversionType.isConversionType(Long.class));
        Assert.assertTrue(ConversionType.isConversionType(Long[].class));
    }


}
