/*
 * Copyright 2005-2013 the original author or authors.
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

import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the PresentFilter class.
 *
 * @author Ulrik Sandberg
 */
public class PresentFilterTest {

    @Test
	public void testPresentFilter() {
		PresentFilter filter = new PresentFilter("foo");
		assertEquals("(foo=*)", filter.encode());

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

    @Test
    public void testEquals() {
		String attribute = "foo";
		PresentFilter originalObject = new PresentFilter(attribute);
		PresentFilter identicalObject = new PresentFilter(attribute);
		PresentFilter differentObject = new PresentFilter("bar");
		PresentFilter subclassObject = new PresentFilter(attribute) {
        };

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}