/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import org.springframework.ldap.core.DistinguishedName;

import java.beans.PropertyEditorSupport;
import javax.naming.ldap.LdapName;

public class NameEditor extends PropertyEditorSupport
{
    private Class conversionClass;

    public NameEditor(Class conversionClass)
    {
        this.conversionClass = conversionClass;
        if (!conversionClass.equals(DistinguishedName.class) && !conversionClass.equals(LdapName.class))
        {
            throw new RuntimeException("NameEditor can only be created for LdapName or DistinguishedName");
        }
    }

    public String getAsText()
    {
        Object value = conversionClass.cast(getValue());
        return value != null ? value.toString() : "";
    }

    public void setAsText(String text) throws IllegalArgumentException
    {
        try
        {
            setValue(conversionClass.getConstructor(String.class).newInstance(text));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
