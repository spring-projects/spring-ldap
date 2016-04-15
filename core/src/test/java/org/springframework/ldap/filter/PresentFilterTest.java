/*
 * Copyright 2005-2016 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the PresentFilter class.
 *
 * @author Ulrik Sandberg
 */
public class PresentFilterTest {

    @Test
	public void testPresentFilter() {
		PresentFilter filter = new PresentFilter("foo");
		assertThat(filter.encode()).isEqualTo("(foo=*)");

		NotFilter notFilter = new NotFilter(new PresentFilter("foo"));
		assertThat(notFilter.encode()).isEqualTo("(!(foo=*))");

		AndFilter andFilter = new AndFilter();
		andFilter.and(new PresentFilter("foo"));
		andFilter.and(new PresentFilter("bar"));
		assertThat(andFilter.encode()).isEqualTo("(&(foo=*)(bar=*))");

		andFilter = new AndFilter();
		andFilter.and(new PresentFilter("foo"));
		andFilter.and(new NotFilter(new PresentFilter("bar")));
		assertThat(andFilter.encode()).isEqualTo("(&(foo=*)(!(bar=*)))");
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
