/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.contextmapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** A bi-directional map of bean property names to attribute names */
public class ContextMap
{
    private static final Log LOGGER = LogFactory.getLog(ContextMap.class);
    private Map<String, String> beanPropertyNameKeys;
    private Map<String, String> attributeNameKeys;

    public ContextMap()
    {
        beanPropertyNameKeys = new HashMap();
        attributeNameKeys = new HashMap();
    }

    public void map(String beanPropertyName, String toAttributeName)
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
}
