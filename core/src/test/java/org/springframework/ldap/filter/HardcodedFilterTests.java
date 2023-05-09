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
 * Unit tests for the HardcodedFilter class.
 *
 * @author Ulrik Sandberg
 */
public class HardcodedFilterTests {

	@Test
	public void testHardcodedFilter() {
		HardcodedFilter filter = new HardcodedFilter("(foo=a*b)");
		assertThat(filter.encode()).isEqualTo("(foo=a*b)");

		NotFilter notFilter = new NotFilter(new HardcodedFilter("(foo=a*b)"));
		assertThat(notFilter.encode()).isEqualTo("(!(foo=a*b))");

		AndFilter andFilter = new AndFilter();
		andFilter.and(new HardcodedFilter("(foo=a*b)"));
		andFilter.and(new HardcodedFilter("(bar=a*b)"));
		assertThat(andFilter.encode()).isEqualTo("(&(foo=a*b)(bar=a*b))");

		andFilter = new AndFilter();
		andFilter.and(new HardcodedFilter("(foo=a*b)"));
		andFilter.and(new NotFilter(new HardcodedFilter("(bar=a*b)")));
		assertThat(andFilter.encode()).isEqualTo("(&(foo=a*b)(!(bar=a*b)))");
	}

	@Test
	public void testEquals() {
		String attribute = "(foo=a*b)";
		HardcodedFilter originalObject = new HardcodedFilter(attribute);
		HardcodedFilter identicalObject = new HardcodedFilter(attribute);
		HardcodedFilter differentObject = new HardcodedFilter("(bar=a*b)");
		HardcodedFilter subclassObject = new HardcodedFilter(attribute) {
		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
