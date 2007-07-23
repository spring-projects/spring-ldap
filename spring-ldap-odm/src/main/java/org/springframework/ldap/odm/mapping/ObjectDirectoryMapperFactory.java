/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.odm.attributetypes.LdapTypeConverter;
import org.springframework.ldap.odm.attributetypes.ReferencedEntryEditorFactory;

import java.util.HashMap;
import java.util.Map;

public class ObjectDirectoryMapperFactory
{
    private static final Log LOGGER = LogFactory.getLog(ObjectDirectoryMapperFactory.class);
    private Map<Class, ObjectDirectoryMapper> mappers;
    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory referencedEntryEditorFactory;

    public ObjectDirectoryMapperFactory(LdapTypeConverter typeConverter,
                                        ReferencedEntryEditorFactory referencedEntryEditorFactory)
    {
        mappers = new HashMap<Class, ObjectDirectoryMapper>();
        this.typeConverter = typeConverter;
        this.referencedEntryEditorFactory = referencedEntryEditorFactory;
        this.referencedEntryEditorFactory.setObjectDirectoryMapperFactory(this);
    }

    public ObjectDirectoryMapper objectDirectoryMapperForClass(Class clazz)
            throws MappingException
    {
        if (mappers.containsKey(clazz))
        {
            LOGGER.debug("Returning cached object directory mapper for class: " + clazz.getSimpleName());
            return mappers.get(clazz);
        }
        else
        {
            LOGGER.debug("Attempting to create an object directory mapper for class: " + clazz.getSimpleName());
            AnnotationObjectDirectoryMap map = new AnnotationObjectDirectoryMap(clazz);
            ObjectDirectoryMapper mapper =
                    new ObjectDirectoryMapperImpl(map, typeConverter, referencedEntryEditorFactory);
            mappers.put(clazz, mapper);
            return mapper;
        }
    }
}
