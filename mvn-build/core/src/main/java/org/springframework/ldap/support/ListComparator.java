/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.support;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator for comparing lists of Comparable objects.
 * 
 * @author Mattias Arthursson
 */
public class ListComparator implements Comparator {

	/**
	 * Compare two lists of Comparable objects.
	 * 
	 * @param o1 the first object to be compared.
	 * @param o2 the second object to be compared.
	 * @throws ClassCastException if any of the lists contains an object that
	 * is not Comparable.
	 */
	public int compare(Object o1, Object o2) {
		List list1 = (List) o1;
		List list2 = (List) o2;

		for (int i = 0; i < list1.size(); i++) {
			if (list2.size() > i) {
				Comparable component1 = (Comparable) list1.get(i);
				Comparable component2 = (Comparable) list2.get(i);
				int componentsCompared = component1.compareTo(component2);
				if (componentsCompared != 0) {
					return componentsCompared;
				}
			}
			else {
				// First instance has more components, so that instance is
				// greater.
				return 1;
			}
		}

		// All components so far are equal - if the other instance has
		// more components it is greater otherwise they are equal.
		if (list2.size() > list1.size()) {
			return -1;
		}
		else {
			return 0;
		}
	}
}
