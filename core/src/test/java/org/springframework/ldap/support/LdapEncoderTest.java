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

package org.springframework.ldap.support;

import org.junit.Test;
import org.springframework.ldap.BadLdapGrammarException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the LdapEncode class.
 * 
 * @author Adam Skogman
 */
public class LdapEncoderTest  {

	@Test
	public void testFilterEncode() {
		String correct = "\\2aa\\2ab\\28c\\29d\\2a\\5c";
		assertThat(LdapEncoder.filterEncode("*a*b(c)d*\\")).isEqualTo(correct);

	}

	@Test
	public void testNameEncode() {

		String res = LdapEncoder.nameEncode("# foo ,+\"\\<>; ");

		assertThat(res).isEqualTo("\\# foo \\,\\+\\\"\\\\\\<\\>\\;\\ ");
	}

	@Test
	public void testNameDecode() {

		String res = LdapEncoder
				.nameDecode("\\# foo \\,\\+\\\"\\\\\\<\\>\\;\\ ");

		assertThat(res).isEqualTo("# foo ,+\"\\<>; ");
	}

	@Test(expected = BadLdapGrammarException.class)
	public void testNameDecode_slashlast() {
		LdapEncoder.nameDecode("\\");
	}

	// gh-413
	@Test
	public void printBase64WhenReallyLongThenNewLineStartsWithSpace() throws Exception {
		String toBase64Encode = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String expected = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXpBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWjAxMjM0\n NTY3ODk=";

		String actual = LdapEncoder.printBase64Binary(toBase64Encode.getBytes("UTF-8"));

		assertThat(actual).isEqualTo(expected);
	}

	// gh-413
	@Test
	public void parseBase64BinaryWhenReallyLongThenRemovesNewlineAndSpace() {
		String toParse = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXpBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWjAxMjM0\n NTY3ODk=";
		String expected = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		String actual = new String(LdapEncoder.parseBase64Binary(toParse));

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void parseBase64BinaryWhenReallyLongThenRemovesNewlineWithNoSpaceForPassivity() {
		String toParse = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXpBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWjAxMjM0\nNTY3ODk=";
		String expected = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		String actual = new String(LdapEncoder.parseBase64Binary(toParse));

		assertThat(actual).isEqualTo(expected);
	}
}
