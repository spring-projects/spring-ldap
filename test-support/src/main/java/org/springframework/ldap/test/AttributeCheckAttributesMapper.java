/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.test;

import java.util.Arrays;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.util.Assert;

/**
 * Dummy AttributesMapper for testing purposes to check that the received Attributes are
 * the expected ones.
 *
 * @author Mattias Hellborg Arthursson
 */
public class AttributeCheckAttributesMapper implements AttributesMapper<Object> {

	private String[] expectedAttributes = new String[0];

	private String[] expectedValues = new String[0];

	private String[] absentAttributes = new String[0];

	public Object mapFromAttributes(Attributes attributes) throws NamingException {
		Assert.isTrue(this.expectedAttributes.length == this.expectedValues.length,
				"Values and attributes need to have the same length");
		for (int i = 0; i < this.expectedAttributes.length; i++) {
			Attribute attribute = attributes.get(this.expectedAttributes[i]);
			Assert.notNull(attribute, "Attribute " + this.expectedAttributes[i] + " was not present");
			Object value = attribute.get();
			Assert.isTrue(this.expectedValues[i].equals(value),
					"Expected '" + this.expectedValues[i] + "' but got '" + value + "'");
		}

		for (String absentAttribute : this.absentAttributes) {
			Assert.isNull(attributes.get(absentAttribute), "Attribute '" + absentAttribute + "' should not be present");
		}

		return new Object();
	}

	public void setAbsentAttributes(String[] absentAttributes) {
		this.absentAttributes = Arrays.copyOf(absentAttributes, absentAttributes.length);
	}

	public void setExpectedAttributes(String[] expectedAttributes) {
		this.expectedAttributes = Arrays.copyOf(expectedAttributes, expectedAttributes.length);
	}

	public void setExpectedValues(String[] expectedValues) {
		this.expectedValues = Arrays.copyOf(expectedValues, expectedValues.length);
	}

}
