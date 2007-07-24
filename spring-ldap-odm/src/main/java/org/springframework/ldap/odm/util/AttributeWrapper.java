/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.util;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

/** Adds the ability to <code>Attribute</code> to return all values as Object */
public class AttributeWrapper
{
    private Attribute attribute;

    public AttributeWrapper(Attribute attribute)
    {
        this.attribute = attribute;
    }

    /** Returns all of an Attribute's values as an object. If the attribute contains a
     * single value the return type is Object, otherwise the return type is Object[].
     * @return All of an Attribute's values.
     * @throws NamingException
     */
    public Object getAllAsObject() throws NamingException
    {
        if (attribute.size() == 1)
        {
            return attribute.get(0);
        }
        else
        {
            Object[] o = new Object[attribute.size()];
            for (int i = 0; i < attribute.size(); i++)
            {
                o[i] = attribute.get(i);
            }
            return o;
        }
    }
}
