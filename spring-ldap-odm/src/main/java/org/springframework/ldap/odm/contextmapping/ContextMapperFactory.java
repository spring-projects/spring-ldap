/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.contextmapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.odm.attributetypes.LdapTypeConverter;
import org.springframework.ldap.odm.attributetypes.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;

import java.util.HashMap;
import java.util.Map;

public class ContextMapperFactory
{
    private static final Log LOGGER = LogFactory.getLog(ContextMapperFactory.class);
    private Map<Class, AnnotatedClassContextMapper> contextMappers;
    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory referencedEntryEditorFactory;

    public ContextMapperFactory(LdapTypeConverter typeConverter,
                                ReferencedEntryEditorFactory referencedEntryEditorFactory)
    {
        contextMappers = new HashMap<Class, AnnotatedClassContextMapper>();
        this.typeConverter = typeConverter;
        this.referencedEntryEditorFactory = referencedEntryEditorFactory;
        this.referencedEntryEditorFactory.setContextMapperFactory(this);
    }

    public AnnotatedClassContextMapper contextMapperForClass(Class clazz)
            throws ContextMapperException
    {
        if (contextMappers.containsKey(clazz))
        {
            LOGGER.debug("Returning cached context mapper for class: " + clazz.getSimpleName());
            return contextMappers.get(clazz);
        }
        else
        {
            LOGGER.debug("Attempting to create a context mapper for class: " + clazz.getSimpleName());
            AnnotatedClassContextMapper contextMapper =
                    new AnnotatedClassContextMapper(clazz, typeConverter, referencedEntryEditorFactory);
            contextMappers.put(clazz, contextMapper);
            return contextMapper;
        }
    }
}
