/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.DistinguishedName;
import java.util.Set;

/**
 * Encapsulates the information required to serialize a java object to and from an LDAP repository.
 * An {@link ObjectDirectoryMapper} performs the serialization using this information.
 *
 * @see ObjectDirectoryMapper
 */
public interface ObjectDirectoryMap
{
    /** The attribute name corresponding to a bean property name. */
    String attributeNameFor(String beanPropertyName);

    /** The set of attribute names in the Object Directory Map. */
    Set<String> attributeNames();

    /** The bean property name corresponding to an attribute name. */
    String beanPropertyNameFor(String attributeName);

    /** The set of bean property names in the Object Directory Map. */
    Set<String> beanPropertyNames();

    /** The <code>Class</code> that the Object Directory Map represents. That is,
     * LDAP entries will be serialized to and from this class.
     */
    Class getClazz();

    /** The name of the attribute corresponding to the first element in a distinguished
     * name.  For example, in the distinguished name 'uid=admin, ou=people, dc=example, dc=com',
     * the naming attribute is 'uid'.
     */
    String getNamingAttribute();

    /**
     * The distinguished name representing the branch in the directory where an entity
     * is persisted.
     */
    DistinguishedName getNamingSuffix();

    /**
     * The ldap object classes that an entity declares. 
     */
    String[] getObjectClasses();
}
