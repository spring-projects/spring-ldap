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
import org.springframework.ldap.support.LdapUtils;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Mattias Hellborg Arthursson
 */
public class NameAwareAttributeTests {

	@Test
	public void testEqualsWithIdNotSame() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		NameAwareAttribute attr2 = new NameAwareAttribute("someOtherAttribute");

		assertThat(attr1.equals(attr2)).isFalse();
	}

	@Test
	public void testEqualsWithSameIdNoValues() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsUnorderedWithIdenticalAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add("value1");
		attr1.add("value2");
		attr1.add("value3");
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add("value1");
		attr2.add("value2");
		attr2.add("value3");

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsUnorderedWithIdenticalArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(new byte[] { 1, 2, 3 });
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsUnorderedWithDifferentOrderArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });
		attr2.add(new byte[] { 1, 2, 3 });

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsUnorderedWithDifferentArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 2 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(new byte[] { 1, 2, 3 });
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });

		assertThat(attr1.equals(attr2)).isFalse();
	}

	@Test
	public void testEqualsUnorderedWithDifferentNumberOfArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(new byte[] { 1, 2, 3 });
		attr2.add(new byte[] { 1 });

		assertThat(attr1.equals(attr2)).isFalse();
	}

	@Test
	public void testEqualsOrderedWithIdenticalArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
		attr2.add(new byte[] { 1, 2, 3 });
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsOrderedWithArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
		attr2.add(new byte[] { 1, 2, 3 });
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
	}

	@Test
	public void testEqualsOrderedWithDifferentOrderArrayAttributes() {
		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
		attr1.add(new byte[] { 1, 2, 3 });
		attr1.add(new byte[] { 3, 2, 1 });
		attr1.add(new byte[] { 1 });
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
		attr2.add(new byte[] { 3, 2, 1 });
		attr2.add(new byte[] { 1 });
		attr2.add(new byte[] { 1, 2, 3 });

		assertThat(attr1.equals(attr2)).isFalse();
	}

	@Test
	public void testSameDistinguishedNameValue() throws NamingException {
		String expectedName = "cn=John Doe,ou=People";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(LdapUtils.newLdapName(expectedName));
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(LdapUtils.newLdapName(expectedName));

		assertThat(attr2).isEqualTo(attr1);
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
		assertThat(attr1.get()).isEqualTo(expectedName);
		assertThat(attr2.get()).isEqualTo(expectedName);
	}

	@Test
	public void testEqualDistinguishedNameValue() throws NamingException {
		// The names here are syntactically equal, but differ in exact string
		// representation
		String expectedName1 = "cn=John Doe, OU=People";
		String expectedName2 = "cn=John Doe,ou=People";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(LdapUtils.newLdapName(expectedName1));
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(LdapUtils.newLdapName(expectedName2));

		assertThat(attr2).isEqualTo(attr1);
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
		assertThat(attr1.get()).isEqualTo(expectedName1);
		assertThat(attr2.get()).isEqualTo(expectedName2);
	}

	@Test
	public void testEqualDistinguishedNameValueUninitialized() throws NamingException {
		// The names here are syntactically equal, but differ in exact string
		// representation
		String expectedName1 = "cn=John Doe, OU=People";
		String expectedName2 = "cn=John Doe,ou=People";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(expectedName1);
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(LdapUtils.newLdapName(expectedName2));

		assertThat(attr1.equals(attr2)).isFalse();
		assertThat(attr1.get()).isEqualTo(expectedName1);
		assertThat(attr2.get()).isEqualTo(expectedName2);
	}

	@Test
	public void testEqualDistinguishedNameValueManuallyInitialized() throws NamingException {
		// The names here are syntactically equal, but differ in exact string
		// representation
		String expectedName1 = "cn=John Doe, OU=People";
		String expectedName2 = "cn=John Doe,ou=People";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(expectedName1);
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(LdapUtils.newLdapName(expectedName2));

		attr1.initValuesAsNames();

		assertThat(attr1.equals(attr2)).isTrue();
		assertThat(attr2.hashCode()).isEqualTo(attr1.hashCode());
		assertThat(attr1.get()).isEqualTo(expectedName1);
		assertThat(attr2.get()).isEqualTo(expectedName2);
	}

	@Test
	public void testUnequalDistinguishedNameValue() throws NamingException {
		// The names here are syntactically equal, but differ in exact string
		// representation
		String expectedName1 = "cn=Jane Doe,ou=People";
		String expectedName2 = "cn=John Doe,ou=People";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(LdapUtils.newLdapName(expectedName1));
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(LdapUtils.newLdapName(expectedName2));

		assertThat(attr1.equals(attr2)).isFalse();
		assertThat(attr1.get()).isEqualTo(expectedName1);
		assertThat(attr2.get()).isEqualTo(expectedName2);
	}

	@Test
	public void testComparingWDistinguishedNameValueWithInvalidName() throws NamingException {
		// The names here are syntactically equal, but differ in exact string
		// representation
		String expectedName1 = "cn=Jane Doe,ou=People";
		String expectedValue2 = "thisisnotavaliddn";

		NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
		attr1.add(LdapUtils.newLdapName(expectedName1));
		NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
		attr2.add(expectedValue2);

		assertThat(attr1.equals(attr2)).isFalse();
		assertThat(attr1.get()).isEqualTo(expectedName1);
		assertThat(attr2.get()).isEqualTo(expectedValue2);
	}

	@Test
	public void testRemoveByIndexUpdatesHashcodeAndEquals() throws InvalidNameException {
		// given
		Name a = new LdapName("cn=user1");
		Name b = new LdapName("cn=user2");
		final NameAwareAttribute attribute = new NameAwareAttribute("test attribute");
		attribute.add(a);
		attribute.add(b);
		// when
		attribute.remove(0);
		// then
		final NameAwareAttribute expectedAttribute = new NameAwareAttribute("test attribute");
		expectedAttribute.add(b);
		assertTrue(attribute.equals(expectedAttribute));
		assertTrue(attribute.hashCode() == expectedAttribute.hashCode());
	}

}
