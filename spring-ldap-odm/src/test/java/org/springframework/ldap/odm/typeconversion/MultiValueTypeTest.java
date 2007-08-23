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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class MultiValueTypeTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(MultiValueTypeTest.class);

    public void testListTypes()
    {
        LOGGER.debug(MultiValueType.listTypes());
    }

    public void testIsMultiValueType()
    {
        Assert.assertFalse(MultiValueType.isMultiValueType(String.class));
        Assert.assertTrue(MultiValueType.isMultiValueType(Object[].class));
        Assert.assertTrue(MultiValueType.isMultiValueType(Collection.class));
        Assert.assertTrue(MultiValueType.isMultiValueType(List.class));
        Assert.assertTrue(MultiValueType.isMultiValueType(Set.class));
    }

    public void testIsAssignableTo()
    {
        Assert.assertFalse(MultiValueType.isAssignableTo(String.class));
        Assert.assertTrue(MultiValueType.isAssignableTo(ArrayList.class));
        Assert.assertTrue(MultiValueType.isAssignableTo(HashSet.class));
    }


}
