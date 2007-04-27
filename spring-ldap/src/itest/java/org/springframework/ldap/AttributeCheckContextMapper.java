/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap;

import junit.framework.Assert;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * Dummy ContextMapper for testing purposes to check that the received
 * Attributes are the expected ones.
 * 
 * @author Mattias Arthursson
 */
class AttributeCheckContextMapper implements ContextMapper {
    private String[] expectedAttributes = new String[0];

    private String[] expectedValues = new String[0];

    private String[] absentAttributes = new String[0];

    public Object mapFromContext(Object ctx) {
        DirContextAdapter adapter = (DirContextAdapter) ctx;
        Assert.assertEquals("Values and attributes need to have the same length ",
                expectedAttributes.length, expectedValues.length);
        for (int i = 0; i < expectedAttributes.length; i++) {
            String attributeValue = adapter
                    .getStringAttribute(expectedAttributes[i]);
            Assert.assertNotNull("Attribute " + expectedAttributes[i]
                    + " was not present", attributeValue);
            Assert.assertEquals(expectedValues[i], attributeValue);
        }

        for (int i = 0; i < absentAttributes.length; i++) {
            Assert.assertNull(adapter.getStringAttribute(absentAttributes[i]));
        }

        return null;
    }

    public void setAbsentAttributes(String[] absentAttributes) {
        this.absentAttributes = absentAttributes;
    }

    public void setExpectedAttributes(String[] expectedAttributes) {
        this.expectedAttributes = expectedAttributes;
    }

    public void setExpectedValues(String[] expectedValues) {
        this.expectedValues = expectedValues;
    }
}