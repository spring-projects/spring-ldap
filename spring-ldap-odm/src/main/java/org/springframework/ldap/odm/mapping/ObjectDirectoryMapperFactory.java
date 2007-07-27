/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.odm.typeconversion.LdapTypeConverter;
import org.springframework.ldap.odm.typeconversion.ReferencedEntryEditorFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for assembling {@link ObjectDirectoryMapper ObjectDirectoryMappers}.
 * It builds a registry such that the first request for an {@link ObjectDirectoryMapper} 
 * of a given type results in the attempt to build one. Subsequent requests return  a
 * cached instance.
 *
 * @see ObjectDirectoryMapper 
 * @see org.springframework.ldap.odm.typeconversion.ReferencedEntryEditorFactory
 */
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

    /** Attempts to return an {@link ObjectDirectoryMapper} for the given class. Upon the
     * first encounter of the given class, mapping is attempted. If mapping is successful
     * the mapper is returned. Subsequent requests for same class return a cached mapper.
     * @param clazz The Class to return an object directory mapper for. 
     * @return An {@link ObjectDirectoryMapper} for the Class <code>clazz</code>
     * @throws MappingException when the mapping information for the given class contains errors.  
     */
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
