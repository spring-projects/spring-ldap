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

	// gh-634
	@Test
	public void encodeWhenBinaryThenEscapesEachByteOnce() {
		byte[] guid = { (byte) 0x90, (byte) 0xd1, (byte) 0xc6, (byte) 0x8e, 0x01, (byte) 0xbe, 0x44, (byte) 0x85 };
		EqualsFilter eqq = new EqualsFilter("objectGUID", guid);

		assertThat(eqq.encode()).isEqualTo("(objectGUID=\\90\\d1\\c6\\8e\\01\\be\\44\\85)");
	}

	@Test
	public void encodeWhenBinaryThenEscapesEveryByte() {
		byte[] value = { 0x00, 0x2a, 0x5c, 0x7f, (byte) 0x80, (byte) 0x90, (byte) 0xff, 0x41 };
		EqualsFilter eqq = new EqualsFilter("objectGUID", value);

		assertThat(eqq.encode()).isEqualTo("(objectGUID=\\00\\2a\\5c\\7f\\80\\90\\ff\\41)");
	}

	@Test
	public void encodeWhenEmptyBinaryThenEmptyValue() {
		EqualsFilter eqq = new EqualsFilter("objectGUID", new byte[0]);

		assertThat(eqq.encode()).isEqualTo("(objectGUID=)");
	}

	@Test
	public void equalsWhenBinaryAndStringWithSameTextThenNotEqual() {
		EqualsFilter binary = new EqualsFilter("objectGUID", new byte[] { (byte) 0x90, (byte) 0xd1 });
		EqualsFilter string = new EqualsFilter("objectGUID", "\\90\\d1");

		assertThat(string.encode()).isEqualTo("(objectGUID=\\5c90\\5cd1)");
		assertThat(binary).isNotEqualTo(string);
		assertThat(binary).isEqualTo(new EqualsFilter("objectGUID", new byte[] { (byte) 0x90, (byte) 0xd1 }));
		assertThat(binary).hasSameHashCodeAs(new EqualsFilter("objectGUID", new byte[] { (byte) 0x90, (byte) 0xd1 }));
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
