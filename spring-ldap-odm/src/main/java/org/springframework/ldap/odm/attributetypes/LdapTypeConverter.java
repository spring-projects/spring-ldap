/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.ldap.LdapName;
import java.beans.PropertyEditor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LdapTypeConverter is responsible for the conversion of LDAP attributes returned in String form
 * to native java types and vice versa. Mostly it leverages Spring's property editors, however
 * it registers some custom editors to:
 * <li>
 * <ul>convert from Generalized Time strings to java.util.Date</ul>
 * <ul>convert dn strings to <code>javax.naming.ldap.LdapName</code> or
 * <code>org.springframework.ldap.core.DistinguishedName</code>.
 * </li>
 */
public class LdapTypeConverter extends SimpleTypeConverter
{
    private static final DateFormat GENERALIZED_TIME = new SimpleDateFormat("yyyyMMddHHmmss.S");

    public LdapTypeConverter()
    {
        super();
        registerCustomEditor(Date.class, new CustomDateEditor(GENERALIZED_TIME, true));
        registerCustomEditor(LdapName.class, new NameEditor(LdapName.class));
        registerCustomEditor(DistinguishedName.class, new NameEditor(DistinguishedName.class));
    }

    /**
     * Convert Object to String
     */
    public String getAsText(Object value)
    {
        if (value instanceof String)
        {
            return (String) value;
        }
        PropertyEditor pe = propertyEditorFor(value.getClass());
        if (pe != null)
        {
            pe.setValue(value);
            return pe.getAsText();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert Object array to String array
     */
    public String[] getAllAsText(Object[] values)
    {
        String[] textValues = new String[values.length];
        for (int i = 0; i < values.length; i++)
        {
            textValues[i] = getAsText(values[i]);
        }
        return textValues;
    }

    private PropertyEditor propertyEditorFor(Class clazz)
    {
        PropertyEditor pe = findCustomEditor(clazz, null);
        if (pe != null)
        {
            return pe;
        }
        else
        {
            pe = getDefaultEditor(clazz);
            return pe;
        }
    }


}
