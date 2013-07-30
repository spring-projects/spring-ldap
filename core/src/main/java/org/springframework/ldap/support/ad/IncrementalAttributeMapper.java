/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.support.ad;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;

/**
 * Utility class that helps with reading all attribute values from Active Directory using <em>Incremental Retrieval of
 * Multi-valued Properties</em>.
 * <p/>
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
 * @since 1.3.2
 */
public class IncrementalAttributeMapper implements AttributesMapper {
    private final String attributeName;
    private final AttributeValueProcessor valueProcessor;

    private boolean more = true;
    private int pageSize;
    private RangeOption requestRange = new RangeOption(0, pageSize);
    private boolean omitFullRange = true;

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor) {
        this(attributeName, valueProcessor, RangeOption.TERMINAL_END_OF_RANGE);
    }

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor, int pageSize) {
        this.attributeName = attributeName;
        this.pageSize = pageSize;
        this.requestRange = new RangeOption(0, pageSize);
        this.valueProcessor = valueProcessor;
    }

    public boolean isOmitFullRange() {
        return omitFullRange;
    }

    public void setOmitFullRange(boolean omitFullRange) {
        this.omitFullRange = omitFullRange;
    }

    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        if (!more) {
            throw new IllegalStateException("No more attributes!");
        }

        more = false;

        NamingEnumeration attributeNameEnum = attributes.getIDs();

        while (attributeNameEnum.hasMore()) {
            String attributeName = (String) attributeNameEnum.next();

            if (attributeName.equals(this.attributeName)) {
                processValues(attributes, this.attributeName);
            } else if (attributeName.startsWith(this.attributeName + ";")) {
                String[] attributeNameSplit = attributeName.split(";");
                for (int i = 0; i < attributeNameSplit.length; i++) {
                    String option = attributeNameSplit[i];

                    RangeOption responseRange = RangeOption.parse(option);

                    if (responseRange != null) {
                        more = requestRange.compareTo(responseRange) > 0;

                        if (more) {
                            requestRange = responseRange.nextRange(pageSize);
                        }

                        processValues(attributes, attributeName);
                    }
                }
            }
        }

        return this;
    }

    private void processValues(Attributes attributes, String attributeName) throws NamingException {
        Attribute attribute = attributes.get(attributeName);
        NamingEnumeration valueEnum = attribute.getAll();

        while (valueEnum.hasMore()) {
            valueProcessor.process(valueEnum.next());
        }
    }

    public boolean hasMore() {
        return more;
    }

    public String[] getAttributesArray() {
        StringBuilder attributeBuilder = new StringBuilder(attributeName);

        if (!(omitFullRange && requestRange.isFullRange())) {
            attributeBuilder.append(';');

            requestRange.toString(attributeBuilder);
        }

        return new String[]{attributeBuilder.toString()};
    }
}
