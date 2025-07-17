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

package org.springframework.ldap.core.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * IncrementalAttributesMapper Tester.
 *
 * @author Marius Scurtescu
 */
public class RangeOptionTests {

	@Test
	public void testConstructorInvalid() {
		try {
			new RangeOption(101, 100);

			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new RangeOption(-1, 100);

			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new RangeOption(-10, 100);

			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new RangeOption(0, -3);

			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testToString() throws Exception {
		RangeOption range = new RangeOption(0, 100);
		assertThat(range.toString()).isEqualTo("Range=0-100");

		range = new RangeOption(0, RangeOption.TERMINAL_END_OF_RANGE);
		assertThat(range.toString()).isEqualTo("Range=0-*");

		range = new RangeOption(0, RangeOption.TERMINAL_MISSING);
		assertThat(range.toString()).isEqualTo("Range=0");
	}

	@Test
	public void testParse() throws Exception {
		RangeOption range = RangeOption.parse("Range=0-100");
		assertThat(range.getInitial()).isEqualTo(0);
		assertThat(range.getTerminal()).isEqualTo(100);

		range = RangeOption.parse("range=0-100");
		assertThat(range.getInitial()).isEqualTo(0);
		assertThat(range.getTerminal()).isEqualTo(100);

		range = RangeOption.parse("RANGE=0-100");
		assertThat(range.getInitial()).isEqualTo(0);
		assertThat(range.getTerminal()).isEqualTo(100);

		range = RangeOption.parse("Range=0-*");
		assertThat(range.getInitial()).isEqualTo(0);
		assertThat(range.getTerminal()).isEqualTo(RangeOption.TERMINAL_END_OF_RANGE);

		range = RangeOption.parse("Range=10");
		assertThat(range.getInitial()).isEqualTo(10);
		assertThat(range.getTerminal()).isEqualTo(RangeOption.TERMINAL_MISSING);
	}

	@Test
	public void testParseInvalid() {
		assertThat(RangeOption.parse("Range=10-")).isNull();
		assertThat(RangeOption.parse("Range=10-a")).isNull();
		assertThat(RangeOption.parse("lang-en")).isNull();
		assertThat(RangeOption.parse("member;Range=10-100")).isNull();
		assertThat(RangeOption.parse(";Range=10-100")).isNull();
		assertThat(RangeOption.parse("Range=10-100;")).isNull();
		assertThat(RangeOption.parse("Range=10-100;lang-de")).isNull();
	}

	@Test
	public void testCompare() {
		RangeOption range1 = RangeOption.parse("Range=10-500");
		RangeOption range2 = RangeOption.parse("Range=10-500");
		assertThat(range1.compareTo(range2) == 0).isTrue();
		assertThat(range2.compareTo(range1) == 0).isTrue();

		range1 = RangeOption.parse("Range=0-*");
		range2 = RangeOption.parse("Range=0-*");
		assertThat(range1.compareTo(range2) == 0).isTrue();
		assertThat(range2.compareTo(range1) == 0).isTrue();

		range1 = RangeOption.parse("Range=0");
		range2 = RangeOption.parse("Range=0");
		assertThat(range1.compareTo(range2) == 0).isTrue();
		assertThat(range2.compareTo(range1) == 0).isTrue();

		range1 = RangeOption.parse("Range=0-101");
		range2 = RangeOption.parse("Range=0-100");
		assertThat(range1.compareTo(range2) > 0).isTrue();
		assertThat(range2.compareTo(range1) < 0).isTrue();

		range1 = RangeOption.parse("Range=0-*");
		range2 = RangeOption.parse("Range=0-100");
		assertThat(range1.compareTo(range2) > 0).isTrue();
		assertThat(range2.compareTo(range1) < 0).isTrue();
	}

	@Test
	public void testCompareInvalid() {
		RangeOption range1 = RangeOption.parse("Range=10-500");
		RangeOption range2 = RangeOption.parse("Range=11-500");

		try {
			assertThat(range1.compareTo(range2) == 0).isTrue();

			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertThat(true).isTrue();
		}

		range1 = RangeOption.parse("Range=10");
		range2 = RangeOption.parse("Range=10-500");

		try {
			assertThat(range1.compareTo(range2) == 0).isTrue();

			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertThat(true).isTrue();
		}

		range1 = RangeOption.parse("Range=10-500");
		range2 = RangeOption.parse("Range=10");

		try {
			assertThat(range1.compareTo(range2) == 0).isTrue();

			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testNext() {
		RangeOption range = RangeOption.parse("Range=0-100");

		range = range.nextRange(100);
		assertThat(range.getInitial()).isEqualTo(101);
		assertThat(range.getTerminal()).isEqualTo(200);

		range = range.nextRange(10);
		assertThat(range.getInitial()).isEqualTo(201);
		assertThat(range.getTerminal()).isEqualTo(210);

		range = range.nextRange(RangeOption.TERMINAL_END_OF_RANGE);
		assertThat(range.getInitial()).isEqualTo(211);
		assertThat(range.getTerminal()).isEqualTo(RangeOption.TERMINAL_END_OF_RANGE);
	}

}
