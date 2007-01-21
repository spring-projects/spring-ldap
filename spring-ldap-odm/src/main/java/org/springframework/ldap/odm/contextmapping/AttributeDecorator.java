/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.contextmapping;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

public class AttributeDecorator
{
    private Attribute attribute;

    public AttributeDecorator(Attribute attribute)
    {
        this.attribute = attribute;
    }

    public Object getAllAsObject() throws NamingException
    {
        if (attribute != null)
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
        else
        {
            return null;
        }

    }
}
