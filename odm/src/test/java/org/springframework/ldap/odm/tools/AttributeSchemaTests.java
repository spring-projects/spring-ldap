/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.odm.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link AttributeSchema}
 */
class AttributeSchemaTests {

	@Test
	void getJavaNameStripsHyphens() {
		AttributeSchema attributeSchema = new AttributeSchema("some-attribute-name", "syntax", true, false, false,
				false, "String");

		assertThat(attributeSchema.getJavaName()).isEqualTo("someattributename");
	}

	@Test
	void constructorWhenNameContainsCharactersInvalidInAJavaIdentifierThenThrows() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new AttributeSchema("foo;bar", "syntax", true, false, false, false, "String"));
	}

	@Test
	void constructorWhenNameContainsBracesThenThrows() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new AttributeSchema("foo{bar}", "syntax", true, false, false, false, "String"));
	}

	@Test
	void constructorWhenNameStartsWithADigitThenThrows() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new AttributeSchema("1leadingDigit", "syntax", true, false, false, false, "String"));
	}

	@Test
	void constructorWhenNameContainsWhitespaceThenThrows() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new AttributeSchema("foo bar", "syntax", true, false, false, false, "String"));
	}

}
