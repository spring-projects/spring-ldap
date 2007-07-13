/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.attributetypes;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Date;

public class ValidConversionTypeTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(ValidConversionTypeTest.class);

    public void testListTypes()
    {
        LOGGER.debug(ValidConversionType.listTypes());
    }

    public void testIsValidConversionType()
    {
        Assert.assertFalse(ValidConversionType.isValidConversionType(List.class));

        Assert.assertTrue(ValidConversionType.isValidConversionType(byte[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Boolean.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(String.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(String[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Date.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Date[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(LdapName.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(LdapName[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(DistinguishedName.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(DistinguishedName[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Integer.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Integer[].class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Long.class));
        Assert.assertTrue(ValidConversionType.isValidConversionType(Long[].class));
    }


}
