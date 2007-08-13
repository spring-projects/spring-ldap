/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapperFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ReferencedEntryEditorFactory is a factory for assembling ReferencedEntryEditors.
 *
 * @see ReferencedEntryEditor
 */
public class ReferencedEntryEditorFactory
{
    private static final Log LOGGER = LogFactory.getLog(ReferencedEntryEditorFactory.class);
    private String base;
    private ObjectDirectoryMapperFactory odmFactory;
    private LdapTemplate ldapTemplate;
    private Map<Class, ReferencedEntryEditor> referencedEntryEditors;

    public ReferencedEntryEditorFactory(String base, LdapTemplate ldapTemplate)
    {
        this.base = base;
        this.ldapTemplate = ldapTemplate;
        this.referencedEntryEditors = new HashMap();
    }

    /**
     * Attempts to build a ReferencedEntryEditor for the given type. If Object Directory
     * Mapping for the given type is successful an editor is returned, otherwise a
     * <code>MappingException</code> is thrown.
     *
     * @param clazz the type to build a ReferencedEntryEditor for.
     * @return A ReferencedEntryEditor for the given type.
     */
    public ReferencedEntryEditor referencedEntryEditorForClass(Class clazz)
            throws MappingException
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

            ObjectDirectoryMapper odm = odmFactory.objectDirectoryMapperForClass(clazz);
            ReferencedEntryEditor referencedEntryEditor = new ReferencedEntryEditor(
                    SavePolicy.CREATE_OR_UPDATE, LoadPolicy.SUPPRESS_REFERENTIAL_INTEGRITY_EXCEPTIONS,
                    new DistinguishedName(base), ldapTemplate, odm);
            referencedEntryEditors.put(clazz, referencedEntryEditor);
            return referencedEntryEditor;
        }
    }

    /**
     * @param mapperFactory the <code>ObjectDirectoryMapperFactory</code> to use when attempting
     *                      to build a <code>ReferencedEntryEditor</code>.
     */
    public void setObjectDirectoryMapperFactory(ObjectDirectoryMapperFactory
            mapperFactory)
    {
        this.odmFactory = mapperFactory;
    }
}
