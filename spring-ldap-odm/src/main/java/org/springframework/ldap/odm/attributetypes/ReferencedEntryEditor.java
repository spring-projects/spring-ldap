/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.beans.PropertyEditorSupport;

public class ReferencedEntryEditor extends PropertyEditorSupport
{
    private LdapTemplate ldapTemplate;
    private ObjectDirectoryMapper objectDirectoryMapper;

    public ReferencedEntryEditor(LdapTemplate ldapTemplate,
                                 ObjectDirectoryMapper objectDirectoryMapper)
    {
        this.ldapTemplate = ldapTemplate;
        this.objectDirectoryMapper = objectDirectoryMapper;
    }

    public String getAsText()
    {
        try
        {
            return objectDirectoryMapper.buildDn(getValue()).toString();
        }
        catch (MappingException e)
        {
            throw new RuntimeException(
                    "Mapping exception: " + getValue().getClass().getSimpleName());
        }
    }

    public void setAsText(String text) throws IllegalArgumentException
    {
        try
        {
            setValue(ldapTemplate.lookup(new LdapName(text), objectDirectoryMapper));
        }
        catch (InvalidNameException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
