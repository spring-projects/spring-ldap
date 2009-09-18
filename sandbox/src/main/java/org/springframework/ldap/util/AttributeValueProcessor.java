package org.springframework.ldap.util;

/**
 * @author Marius Scurtescu
 */
public interface AttributeValueProcessor
{
    public void process(Object value);
}
