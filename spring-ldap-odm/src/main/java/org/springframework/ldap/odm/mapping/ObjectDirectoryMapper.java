/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.ContextAssembler;
import org.springframework.ldap.core.ContextMapper;

import javax.naming.Name;

/**
 * Performs serialization between java objects and and an LDAP repository using the information
 * in an {@link ObjectDirectoryMap}.
 *
 * @see ObjectDirectoryMap
 */
public interface ObjectDirectoryMapper extends ContextMapper, ContextAssembler
{
    /**
     * Map the supplied object to the specified context, using the associated
     * {@link ObjectDirectoryMap}.
     */
    Object mapFromContext(Object ctx);

    /**
     * Map a single LDAP Context to an object using the associated {@link ObjectDirectoryMap}.
     */
    void mapToContext(Object beanInstance, Object ctx);

    /**
     * Builds a Distinguished Name from an object instance. Looks up the Naming Attribute and
     * Naming Suffix from the {@link ObjectDirectoryMap}. Populates the Naming Attribute value
     * with value on the beanInstance.
     *
     * @throws MappingException if the naming attribute value on the beanInstance is null.
     */
    Name buildDn(Object beanInstance) throws MappingException;

    /**
     * Builds a Distinguished Name from the given <code>namingAttributeValue</code>.
     * Looks up the Naming Attribute and Naming Suffix from the {@link ObjectDirectoryMap}.
     * Populates the Naming Attribute value with the given value.
     *
     * @throws MappingException if the given value is null. 
     */
    Name buildDn(String namingAttributeValue) throws MappingException;

    /**
     * Returns the {@link ObjectDirectoryMap} associated with the given instance of the
     * <code>ObjectDirectoryMapper</code>.     
     */
    ObjectDirectoryMap getObjectDirectoryMap();
}
