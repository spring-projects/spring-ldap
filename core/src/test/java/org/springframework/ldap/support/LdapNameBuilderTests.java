/*
 * Copyright 2005-2023 the original author or authors.
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapNameBuilderTests {

	@Test
	public void testAddComponentToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add("dc", "com").add("dc", "261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddComponentToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=com").add("dc", "261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddComponentToBaseName() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance(LdapUtils.newLdapName("dc=com"))
			.add("dc", "261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddStringNameToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com").add("ou=people");
		assertThat(tested.build().toString()).isEqualTo("ou=people,dc=261consulting,dc=com");
	}

	@Test
	public void testAddNameToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com")
			.add(LdapUtils.newLdapName("ou=people"));
		assertThat(tested.build().toString()).isEqualTo("ou=people,dc=261consulting,dc=com");
	}

	@Test
	public void testAddNameToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add(LdapUtils.newLdapName("ou=people"));
		assertThat(tested.build().toString()).isEqualTo("ou=people");
	}

	@Test
	public void testAddEmptyToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add("");
		assertThat(tested.build().toString()).isEqualTo("");
	}

}
