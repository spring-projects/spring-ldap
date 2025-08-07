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

package org.springframework.ldap.filter;

import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LessThanOrEqualsFilterTests {

	@Test
	public void testEncode() {

		LessThanOrEqualsFilter eqq = new LessThanOrEqualsFilter("foo", "*bar(fie)");

		StringBuffer buff = new StringBuffer();
		eqq.encode(buff);

		assertThat(buff.toString()).isEqualTo("(foo<=\\2abar\\28fie\\29)");

	}

	@Test
	public void testEncodeInt() {

		LessThanOrEqualsFilter eqq = new LessThanOrEqualsFilter("foo", 456);

		StringBuffer buff = new StringBuffer();
		eqq.encode(buff);

		assertThat(buff.toString()).isEqualTo("(foo<=456)");

	}

	@Test
	public void testEquals() {
		String attribute = "a";
		String value = "b";
		LessThanOrEqualsFilter originalObject = new LessThanOrEqualsFilter(attribute, value);
		LessThanOrEqualsFilter identicalObject = new LessThanOrEqualsFilter(attribute, value);
		LessThanOrEqualsFilter differentObject = new LessThanOrEqualsFilter(attribute, "c");
		LessThanOrEqualsFilter subclassObject = new LessThanOrEqualsFilter(attribute, value) {
		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
