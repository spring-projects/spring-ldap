/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import java.beans.PropertyEditorSupport;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.contextmapping.AnnotatedClassContextMapper;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;

public class ReferencedEntryEditor extends PropertyEditorSupport
{    
    private LdapTemplate ldapTemplate;
    private AnnotatedClassContextMapper contextMapper;

    public ReferencedEntryEditor(LdapTemplate ldapTemplate, AnnotatedClassContextMapper contextMapper)
    {
        this.ldapTemplate = ldapTemplate;
        this.contextMapper = contextMapper;
    }

    public String getAsText()
    {
        try
        {
            return contextMapper.buildDn(getValue()).toString();
        }
        catch (ContextMapperException e)
        {
            throw new RuntimeException("No context mapper for class: " + getValue().getClass().getSimpleName());
        }
    }

    public void setAsText(String text) throws IllegalArgumentException
    {
        try
        {
            setValue(ldapTemplate.lookup(new LdapName(text), contextMapper));
        }
        catch (InvalidNameException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
