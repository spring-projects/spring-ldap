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
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class GenericsTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(GenericsTest.class);


    public void testInspectGenericFields() throws NoSuchFieldException
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
                    LOGGER.debug(f.getName() + ": " + actualType.getSimpleName());
                }
            }
        }
    }

    public void testInspectGenericMethods() throws NoSuchFieldException
    {
        Class clazz = SomeEntityWithGenericCollection.class;
        for (Method m : clazz.getDeclaredMethods())
        {
            Type type = m.getGenericReturnType();
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                if (typeArgs[0] != null)
                {
                    Class actualType = (Class) typeArgs[0];
                    LOGGER.debug(m.getName() + ": " + actualType.getSimpleName());
                }
            }
        }
    }
}
