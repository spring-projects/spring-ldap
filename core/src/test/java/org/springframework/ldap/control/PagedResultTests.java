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

package org.springframework.ldap.control;

import java.util.LinkedList;
import java.util.List;

import javax.naming.ldap.PagedResultsControl;

import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.Test;

/**
 * Unit tests for the PagedResult class. {@link PagedResultsControl}
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultTests {

	@Test
	public void testEquals() throws Exception {
		List expectedList = new LinkedList();
		expectedList.add("dummy");
		List otherList = new LinkedList();
		otherList.add("different");

		PagedResult originalObject = new PagedResult(expectedList, new PagedResultsCookie(null));
		PagedResult identicalObject = new PagedResult(expectedList, new PagedResultsCookie(null));
		PagedResult differentObject = new PagedResult(otherList, new PagedResultsCookie(null));
		PagedResult subclassObject = new PagedResult(expectedList, new PagedResultsCookie(null)) {

		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
