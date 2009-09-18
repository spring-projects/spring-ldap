package org.springframework.ldap.util;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;

/**
 * Utility class that helps with reading all attribute values from Active Directory using <em>Incremental Retrieval of
 * Multi-valued Properties</em>.
 *
 * <p>Example usage of this attribute mapper:
 * <pre>
 *     public void retrieveAttributeIncrementally(LdapTemplate ldap, LdapName entrDn, String attributeName, AttributeValueProcessor valueProcessor)
 *     {
 *         IncrementalAttributeMapper incrementalAttributeMapper = new IncrementalAttributeMapper(attributeName, valueProcessor);
 *
 *         while (incrementalAttributeMapper.hasMore())
 *         {
 *             ldap.lookup(entrDn, incrementalAttributeMapper.getAttributesArray(), incrementalAttributeMapper);
 *         }
 *     }
 * </pre>
 *
 * @author Marius Scurtescu
 * @see <a href="http://www.watersprings.org/pub/id/draft-kashi-incremental-00.txt">Incremental Retrieval of Multi-valued Properties</a>
 */
public class IncrementalAttributeMapper implements AttributesMapper
{
    private String _attributeName;
    private boolean _more = true;
    private int _pageSize = RangeOption.TERMINAL_END_OF_RANGE;
    private RangeOption _requestRange = new RangeOption(0, _pageSize);
    private AttributeValueProcessor _valueProcessor;
    private boolean _omitFullRange = true;

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor)
    {
        _attributeName = attributeName;
        _valueProcessor = valueProcessor;
    }

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor, int pageSize)
    {
        _attributeName = attributeName;
        _pageSize = pageSize;
        _requestRange = new RangeOption(0, pageSize);
        _valueProcessor = valueProcessor;
    }

    public boolean isOmitFullRange()
    {
        return _omitFullRange;
    }

    public void setOmitFullRange(boolean omitFullRange)
    {
        _omitFullRange = omitFullRange;
    }

    public Object mapFromAttributes(Attributes attributes) throws NamingException
    {
        if (!_more)
            throw new IllegalStateException("No more attributes!");

        _more = false;

        NamingEnumeration<String> attributeNameEnum = attributes.getIDs();

        while (attributeNameEnum.hasMore())
        {
            String attributeName = attributeNameEnum.next();

            if (attributeName.equals(_attributeName))
            {
                processValues(attributes, _attributeName);
            }
            else if (attributeName.startsWith(_attributeName + ";"))
            {
                for (String option : attributeName.split(";"))
                {
                    RangeOption responseRange = RangeOption.parse(option);

                    if (responseRange != null)
                    {
                        _more = _requestRange.compareTo(responseRange) > 0;

                        if (_more)
                        {
                            _requestRange = responseRange.nextRange(_pageSize);
                        }

                        processValues(attributes, attributeName);
                    }
                }
            }
        }

        return this;
    }

    private void processValues(Attributes attributes, String attributeName) throws NamingException
    {
        Attribute attribute = attributes.get(attributeName);
        NamingEnumeration valueEnum = attribute.getAll();

        while (valueEnum.hasMore())
        {
            _valueProcessor.process(valueEnum.next());
        }
    }

    public boolean hasMore()
    {
        return _more;
    }

    public String [] getAttributesArray()
    {
        StringBuilder attributeBuilder = new StringBuilder(_attributeName);

        if (!(_omitFullRange && _requestRange.isFullRange()))
        {
            attributeBuilder.append(';');

            _requestRange.toString(attributeBuilder);
        }

        return new String[]{attributeBuilder.toString()};
    }
}
