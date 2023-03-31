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
package org.springframework.ldap.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for LdapRdnComponent.
 *
 * @author Mattias Hellborg Arthursson
 */
public class LdapRdnComponentTests {

	@Test
	public void testCompareTo_Less() {
		LdapRdnComponent component1 = new LdapRdnComponent("cn", "john doe");
		LdapRdnComponent component2 = new LdapRdnComponent("sn", "doe");
		int result = component1.compareTo(component2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_Greater() {
		LdapRdnComponent component1 = new LdapRdnComponent("sn", "doe");
		LdapRdnComponent component2 = new LdapRdnComponent("cn", "john doe");
		int result = component1.compareTo(component2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompareTo_Equal() {
		LdapRdnComponent component1 = new LdapRdnComponent("cn", "john doe");
		LdapRdnComponent component2 = new LdapRdnComponent("cn", "john doe");
		int result = component1.compareTo(component2);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testCompareTo_DifferentCase_LDAP259() {
		LdapRdnComponent component1 = new LdapRdnComponent("cn", "john doe");
		LdapRdnComponent component2 = new LdapRdnComponent("CN", "John Doe");

		assertThat(component2).as("Should be equal").isEqualTo(component1);
		assertThat(component1.compareTo(component2) == 0).as("0 should be returned by compareTo").isTrue();
	}

	@Test
	public void verifyThatHashCodeDisregardsCase_LDAP259() {
		LdapRdnComponent component1 = new LdapRdnComponent("cn", "john doe");
		LdapRdnComponent component2 = new LdapRdnComponent("CN", "John Doe");

		assertThat(component2.hashCode()).as("Should be equal").isEqualTo(component1.hashCode());
	}

}
