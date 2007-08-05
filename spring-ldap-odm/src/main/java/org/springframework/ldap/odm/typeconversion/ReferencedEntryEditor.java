/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;
import org.springframework.ldap.NameNotFoundException;

import java.beans.PropertyEditorSupport;

/**
 * ReferencedEntryEditor is responsible for converting references in an object
 * directory map from distinguished name strings to the target type and vice versa.
 */
public class ReferencedEntryEditor extends PropertyEditorSupport
{
    private DistinguishedName base;
    private LdapTemplate ldapTemplate;
    private ObjectDirectoryMapper objectDirectoryMapper;

    public ReferencedEntryEditor(DistinguishedName baseDn,
                                 LdapTemplate ldapTemplate,
                                 ObjectDirectoryMapper objectDirectoryMapper)
    {
        this.base = baseDn;
        this.ldapTemplate = ldapTemplate;
        this.objectDirectoryMapper = objectDirectoryMapper;
    }

    /**
     * Builds a distinguished name from the instance value
     */
    public String getAsText()
    {
        try
        {
            DistinguishedName value = (DistinguishedName) base.clone();
            value.append((DistinguishedName) objectDirectoryMapper.buildDn(getValue()));
            return value.toString();
        }
        catch (MappingException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Sets the value of the editor by performing a lookup and mapping the result
     * to the target type using an object directory mapper.
     *
     * @param text The distinguished name of an ldap entry.
     * @throws IllegalArgumentException
     */
    public void setAsText(String text) throws IllegalArgumentException
    {
        DistinguishedName dn = new DistinguishedName(text);
        if (dn.startsWith(base))
        {
            for (int i = 0; i < base.size(); i++)
            {
                dn.removeFirst();
            }
        }
        try
        {
            setValue(ldapTemplate.lookup(dn, objectDirectoryMapper));
        }
        catch (NameNotFoundException e)
        {
            setValue(null);

        }

    }
}
