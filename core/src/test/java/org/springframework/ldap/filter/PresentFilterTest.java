/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.filter;

import junit.framework.TestCase;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.NotFilter;

public class PresentFilterTest extends TestCase {

	public void testPresentFilter() {
		PresentFilter presentFilter = new PresentFilter("foo");
		assertEquals("(foo=*)", presentFilter.encode());

		NotFilter notFilter = new NotFilter(new PresentFilter("foo"));
		assertEquals("(!(foo=*))", notFilter.encode());

		AndFilter andFilter = new AndFilter();
		andFilter.and(new PresentFilter("foo"));
		andFilter.and(new PresentFilter("bar"));
		assertEquals("(&(foo=*)(bar=*))", andFilter.encode());

		andFilter = new AndFilter();
		andFilter.and(new PresentFilter("foo"));
		andFilter.and(new NotFilter(new PresentFilter("bar")));
		assertEquals("(&(foo=*)(!(bar=*)))", andFilter.encode());
	}
}