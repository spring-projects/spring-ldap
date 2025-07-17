/*
 * Copyright 2005-2016 the original author or authors.
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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Adam Skogman
 */
public class EqualsFilterTests {

	@Test
	public void testEncode() {

		EqualsFilter eqq = new EqualsFilter("foo", "*bar(fie)");

		StringBuffer buff = new StringBuffer();
		eqq.encode(buff);

		assertThat(buff.toString()).isEqualTo("(foo=\\2abar\\28fie\\29)");

	}

	@Test
	public void testEncodeInt() {

		EqualsFilter eqq = new EqualsFilter("foo", 456);

		StringBuffer buff = new StringBuffer();
		eqq.encode(buff);

		assertThat(buff.toString()).isEqualTo("(foo=456)");

	}

	@Test
	public void testEquals() {
		EqualsFilter originalObject = new EqualsFilter("a", "b");
		EqualsFilter identicalObject = new EqualsFilter("a", "b");
		EqualsFilter differentObject = new EqualsFilter("b", "b");
		EqualsFilter subclassObject = new EqualsFilter("a", "b") {
		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
