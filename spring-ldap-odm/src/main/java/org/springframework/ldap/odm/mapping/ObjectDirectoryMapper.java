/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.ContextAssembler;
import org.springframework.ldap.core.ContextMapper;

import javax.naming.Name;

/** An <code>ObjectDirectoryMapper</code> performs serialization between java beans and and
 * an LDAP repository using the information in an <code>ObjectDirectoryMap</code>.
 *
 * @see ObjectDirectoryMap 
 */
public interface ObjectDirectoryMapper extends ContextMapper, ContextAssembler
{
    /** Map the supplied object to the specified context. */
    Object mapFromContext(Object ctx);

    /** Map a single LDAP Context to an object. */
    void mapToContext(Object beanInstance, Object ctx);

    /** Builds a DistinguishedName from an object instance. */
    Name buildDn(Object beanInstance) throws MappingException;

    /** Builds a DistinguishedName given the value of the naming attribute. */
    Name buildDn(String namingAttributeValue) throws MappingException;

    /** Returns the <code>ObjectDirectoryMap</code> that the <code>ObjectDirectoryMapper</code>
     * corresponds to.
     */
    ObjectDirectoryMap getObjectDirectoryMap();
}
