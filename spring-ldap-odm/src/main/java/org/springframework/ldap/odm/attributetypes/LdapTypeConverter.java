/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.ldap.LdapName;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.ldap.core.DistinguishedName;

public class LdapTypeConverter extends SimpleTypeConverter
{

    public LdapTypeConverter()
    {
        super();
        registerCustomEditor(Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyyMMddHHmmss.S"), true));
        registerCustomEditor(LdapName.class, new NameEditor(LdapName.class));
        registerCustomEditor(DistinguishedName.class, new NameEditor(DistinguishedName.class));
    }

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
