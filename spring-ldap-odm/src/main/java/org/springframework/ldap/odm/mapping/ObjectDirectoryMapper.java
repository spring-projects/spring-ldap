/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.ContextAssembler;
import org.springframework.ldap.core.ContextMapper;

import javax.naming.Name;

public interface ObjectDirectoryMapper extends ContextMapper, ContextAssembler
{
    Object mapFromContext(Object ctx);

    void mapToContext(Object beanInstance, Object ctx);

    Name buildDn(Object beanInstance) throws MappingException;

    Name buildDn(String namingAttributeValue) throws MappingException;

    ObjectDirectoryMap getObjectDirectoryMap();
}
