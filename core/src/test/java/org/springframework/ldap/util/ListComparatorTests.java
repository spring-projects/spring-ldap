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

package org.springframework.ldap.util;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.support.ListComparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ListComparator.
 *
 * @author Mattias Hellborg Arthursson
 */
public class ListComparatorTests {

	private ListComparator tested;

	@BeforeEach
	public void setUp() throws Exception {
		this.tested = new ListComparator();
	}

	@Test
	public void testCompare_Equals() {
		List<Integer> list1 = Arrays.asList(0, 0);
		List<Integer> list2 = Arrays.asList(0, 0);

		int result = this.tested.compare(list1, list2);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testCompare_Less() {
		List<Integer> list1 = Arrays.asList(0, 0);
		List<Integer> list2 = Arrays.asList(0, 1);

		int result = this.tested.compare(list1, list2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompare_Greater() {
		List<Integer> list1 = Arrays.asList(0, 1);
		List<Integer> list2 = Arrays.asList(0, 0);

		int result = this.tested.compare(list1, list2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompare_Longer() {
		List<Integer> list1 = Arrays.asList(0, 0, 0);
		List<Integer> list2 = Arrays.asList(0, 0);

		int result = this.tested.compare(list1, list2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompare_Shorter() {
		List<Integer> list1 = Arrays.asList(0, 0);
		List<Integer> list2 = Arrays.asList(0, 0, 0);

		int result = this.tested.compare(list1, list2);
		assertThat(result < 0).isTrue();
	}

}
