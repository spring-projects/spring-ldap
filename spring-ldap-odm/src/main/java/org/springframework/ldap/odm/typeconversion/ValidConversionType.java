/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.core.DistinguishedName;

import javax.naming.ldap.LdapName;
import java.util.Date;


/** This list of types supported for mapping between ldap attributes and bean properties. */
public enum ValidConversionType
{
    BYTE_ARRAY(byte[].class),
    BOOLEAN(Boolean.class),
    STRING(String.class),
    STRING_ARRAY(String[].class),
    DATE(Date.class),
    DATE_ARRAY(Date[].class),
    LDAP_NAME(LdapName.class),
    LDAP_NAME_ARRAY(LdapName[].class),
    DISTINGUISHED_NAME(DistinguishedName.class),
    DISTINGUISHED_NAME_ARRAY(DistinguishedName[].class),
    INTEGER(Integer.class),
    INTEGER_ARRAY(Integer[].class),
    LONG(Long.class),
    LONG_ARRAY(Long[].class);

    private final Class clazz;


    ValidConversionType(Class validType)
    {
        this.clazz = validType;

    }

    /** Returns the enumeration of types as a human-friendly string. */
    public static String listTypes()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ValidConversionType.values().length; i++)
        {
            ValidConversionType validType = ValidConversionType.values()[i];
            sb.append("\n");
            sb.append(validType.clazz.getSimpleName());
            if (i != ValidConversionType.values().length - 1)
            {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /** Returns true if the argument is a member of this enumeration. */
    public static boolean isValidConversionType(Class returnType)
    {
        for (ValidConversionType type : ValidConversionType.values())
        {
            if (returnType.equals(type.clazz))
            {
                return true;
            }
        }
        return false;
    }

    

}

