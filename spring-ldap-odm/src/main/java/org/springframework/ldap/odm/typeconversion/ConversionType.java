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
public enum ConversionType
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


    ConversionType(Class type)
    {
        this.clazz = type;

    }

    /** Returns the enumeration of types as a human-friendly string. */
    public static String listTypes()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CONVERSION TYPES: ");
        for (int i = 0; i < values().length; i++)
        {
            ConversionType type = values()[i];
            sb.append("\n");
            sb.append(type.clazz.getSimpleName());
            if (i != values().length - 1)
            {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /** Returns true if the argument is a member of this enumeration. */
    public static boolean isConversionType(Class candidate)
    {
        for (ConversionType type : values())
        {
            if (candidate.equals(type.clazz))
            {
                return true;
            }
        }
        return false;
    }

    

}

