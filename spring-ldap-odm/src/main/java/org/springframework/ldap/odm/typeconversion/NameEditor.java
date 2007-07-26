/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.core.DistinguishedName;

import javax.naming.ldap.LdapName;
import java.beans.PropertyEditorSupport;

/**
 * NameEditor is responsible for converting Strings to the type LdapName or DistinguishedName.
 * And, conversely LdapNames and DistinguishedNames to the type String.
 */
public class NameEditor extends PropertyEditorSupport
{
    private Class conversionClass;

    /**
     * @param conversionClass <code>LdapName.class</code> for String <--> LdapName and
     *                        <code>DistinguishedName.class</code> for String <--> DistinguishedName
     */
    public NameEditor(Class conversionClass)
    {
        this.conversionClass = conversionClass;
        if (!conversionClass.equals(DistinguishedName.class) && !conversionClass.equals(LdapName.class))
        {
            throw new IllegalArgumentException(
                    "NameEditor can only be created for LdapName or DistinguishedName");
        }
    }

    /**
     * Invokes toString() on the LdapName or DistinguishedName
     */
    public String getAsText()
    {
        Object value = conversionClass.cast(getValue());
        return value != null ? value.toString() : "";
    }

    /**
     * Attempts to parse and String and return an LdapName or DistinguishedName
     */
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
