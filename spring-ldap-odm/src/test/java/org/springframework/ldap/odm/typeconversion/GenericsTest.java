/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.odm.entity.SomeEntityWithGenericCollection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Field;

import junit.framework.TestCase;

public class GenericsTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(GenericsTest.class);


    public void testInspectGenericType() throws NoSuchFieldException
    {
        Class clazz = SomeEntityWithGenericCollection.class;
        for (Field f : clazz.getDeclaredFields())
        {
            Type type = f.getGenericType();
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                for (Type typeArg : parameterizedType.getActualTypeArguments())
                {
                    Class actualType = (Class) typeArg;
                    LOGGER.debug(actualType.getSimpleName());
                }
            }

        }
    }
}
