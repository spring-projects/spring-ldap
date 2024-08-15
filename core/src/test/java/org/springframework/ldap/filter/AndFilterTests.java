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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Adam Skogman
 */
public class AndFilterTests {

	@Test
	public void testZero() {
		AndFilter aq = new AndFilter();

		assertThat(aq.encode()).isEqualTo("");
	}

	@Test
	public void testOne() {
		AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b"));

		assertThat(aq.encode()).isEqualTo("(a=b)");
	}

	@Test
	public void testTwo() {
		AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b")).and(new EqualsFilter("c", "d"));

		assertThat(aq.encode()).isEqualTo("(&(a=b)(c=d))");
	}

	@Test
	public void testThree() {
		AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b"))
			.and(new EqualsFilter("c", "d"))
			.and(new EqualsFilter("e", "f"));

		assertThat(aq.encode()).isEqualTo("(&(a=b)(c=d)(e=f))");
	}

	@Test
	public void testEquals() {
		EqualsFilter filter = new EqualsFilter("a", "b");
		AndFilter originalObject = new AndFilter().and(filter);
		AndFilter identicalObject = new AndFilter().and(filter);
		AndFilter differentObject = new AndFilter().and(new EqualsFilter("b", "b"));
		AndFilter subclassObject = new AndFilter() {
		}.and(filter);

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
