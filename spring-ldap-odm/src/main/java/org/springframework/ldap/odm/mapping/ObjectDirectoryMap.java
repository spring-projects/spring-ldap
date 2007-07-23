/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.DistinguishedName;

import java.util.Set;

public interface ObjectDirectoryMap
{
    String attributeNameFor(String beanPropertyName);

    Set<String> attributeNames();

    String beanPropertyNameFor(String attributeName);

    Set<String> beanPropertyNames();

    Class getClazz();

    String getNamingAttribute();

    DistinguishedName getNamingSuffix();

    String[] getObjectClasses();
}
