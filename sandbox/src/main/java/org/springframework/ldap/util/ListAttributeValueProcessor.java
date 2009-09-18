package org.springframework.ldap.util;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Marius Scurtescu
 */
public class ListAttributeValueProcessor implements AttributeValueProcessor
{
    private List<String> _values = new ArrayList<String>();

    public void process(Object value)
    {
        _values.add((String) value);
    }

    public List<String> getValues()
    {
        return _values;
    }
}
