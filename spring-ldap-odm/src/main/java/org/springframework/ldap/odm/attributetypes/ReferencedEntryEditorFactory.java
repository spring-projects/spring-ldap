/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.attributetypes;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.attributetypes.exception.ReferencedEntryEditorCreationException;
import org.springframework.ldap.odm.contextmapping.AnnotatedClassContextMapper;
import org.springframework.ldap.odm.contextmapping.ContextMapperFactory;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;

public class ReferencedEntryEditorFactory
{
    private static final Log LOGGER = LogFactory.getLog(ReferencedEntryEditorFactory.class);
    private ContextMapperFactory contextMapperFactory;
    private LdapTemplate ldapTemplate;
    private Map<Class, ReferencedEntryEditor> referencedEntryEditors;

    public ReferencedEntryEditorFactory(
            LdapTemplate ldapTemplate)
    {
        this.ldapTemplate = ldapTemplate;
        this.referencedEntryEditors = new HashMap();
    }

    public ReferencedEntryEditor referencedEntryEditorForClass(Class clazz)
            throws ReferencedEntryEditorCreationException
    {
        if (referencedEntryEditors.containsKey(clazz))
        {
            LOGGER.debug("Returning cached referenced entry editor for class: " + clazz.getSimpleName());
            return referencedEntryEditors.get(clazz);
        }
        else
        {
            LOGGER.debug("Attempting to create a referenced entry editor for class: "
                    + clazz.getSimpleName());
            AnnotatedClassContextMapper contextMapper = null;
            try
            {
                contextMapper = contextMapperFactory.contextMapperForClass(clazz);
                ReferencedEntryEditor referencedEntryEditor =
                        new ReferencedEntryEditor(ldapTemplate, contextMapper);
                referencedEntryEditors.put(clazz, referencedEntryEditor);
                return referencedEntryEditor;
            }
            catch (ContextMapperException e)
            {
                throw new ReferencedEntryEditorCreationException(e.getMessage(), e);
            }
        }
    }

    public void setContextMapperFactory(ContextMapperFactory contextMapperFactory)
    {
        this.contextMapperFactory = contextMapperFactory;
    }
}
