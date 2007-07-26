/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DistinguishedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An abstract base class implementing an {@link ObjectDirectoryMap}. Actual parsing of
 * mapping information needs to be implemented in a concrete sub class.
 */
public abstract class AbstractObjectDirectoryMap implements ObjectDirectoryMap
{
    protected static final Log LOGGER = LogFactory.getLog(AnnotationObjectDirectoryMap.class);
    protected Class clazz;
    protected String namingAttribute;
    protected String[] objectClasses;
    protected DistinguishedName namingSuffix;
    protected Map<String, String> beanPropertyNameKeys;
    protected Map<String, String> attributeNameKeys;

    public AbstractObjectDirectoryMap(Class<?> clazz) throws MappingException
    {
        this.clazz = clazz;
        if (clazz == null)
        {
            throw new IllegalArgumentException("Unable to create mapping parser. Clazz argument is null.");
        }
        beanPropertyNameKeys = new HashMap();
        attributeNameKeys = new HashMap();

        this.namingAttribute = parseNamingAttribute();
        this.objectClasses = parseObjectClasses();
        this.namingSuffix = parseNamingSuffix();
        mapAttributesToBeanProperties();
    }


    /* ********************************Template *****************************************
     */
    protected abstract String parseNamingAttribute() throws MappingException;

    protected abstract String[] parseObjectClasses() throws MappingException;

    protected abstract DistinguishedName parseNamingSuffix() throws MappingException;

    protected abstract void mapAttributesToBeanProperties() throws MappingException;

    /* **********************************************************************************
     */

    protected void map(String beanPropertyName, String toAttributeName)
    {
        LOGGER.info("Bean property '" + beanPropertyName + "' maps to ldap attribute '" + toAttributeName + "'");
        beanPropertyNameKeys.put(beanPropertyName, toAttributeName);
        attributeNameKeys.put(toAttributeName, beanPropertyName);
    }

    public String beanPropertyNameFor(String attributeName)
    {
        return attributeNameKeys.get(attributeName);
    }

    public String attributeNameFor(String beanPropertyName)
    {
        return beanPropertyNameKeys.get(beanPropertyName);
    }

    public Set<String> beanPropertyNames()
    {
        return beanPropertyNameKeys.keySet();
    }

    public Set<String> attributeNames()
    {
        return attributeNameKeys.keySet();
    }

    public Class getClazz()
    {
        return clazz;
    }

    public String getNamingAttribute()
    {
        return namingAttribute;
    }

    public String[] getObjectClasses()
    {
        return objectClasses;
    }

    public DistinguishedName getNamingSuffix()
    {
        return namingSuffix;
    }


}
