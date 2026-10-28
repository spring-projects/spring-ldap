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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the WhitespaceWildcardsFilter class.
 *
 * @author Adam Skogman
 */
public class WhitespaceWildcardsFilterTests {

	@Test
	public void testEncodeValue_blank() {

		// blank
		assertThat("*").isEqualTo(new WhitespaceWildcardsFilter("cn", null).getEncodedValue());
		assertThat("*").isEqualTo(new WhitespaceWildcardsFilter("cn", " ").getEncodedValue());
		assertThat("*").isEqualTo(new WhitespaceWildcardsFilter("cn", "  ").getEncodedValue());
		assertThat("*").isEqualTo(new WhitespaceWildcardsFilter("cn", "\t").getEncodedValue());

	}

	@Test
	public void testEncodeValue_normal() {

		assertThat("*foo*").isEqualTo(new WhitespaceWildcardsFilter("cn", "foo").getEncodedValue());
		assertThat("*foo*bar*").isEqualTo(new WhitespaceWildcardsFilter("cn", "foo bar").getEncodedValue());
		assertThat(new WhitespaceWildcardsFilter("cn", " foo bar ").getEncodedValue()).isEqualTo("*foo*bar*");
		assertThat(new WhitespaceWildcardsFilter("cn", " \t foo \n bar \r ").getEncodedValue()).isEqualTo("*foo*bar*");
	}

	@Test
	public void testEncodeValue_escape() {

		assertThat("*\\28\\2a\\29*").isEqualTo(new WhitespaceWildcardsFilter("cn", "(*)").getEncodedValue());
		assertThat("*\\2a*").isEqualTo(new WhitespaceWildcardsFilter("cn", "*").getEncodedValue());
		assertThat("*\\5c*").isEqualTo(new WhitespaceWildcardsFilter("cn", " \\ ").getEncodedValue());

	}

}
