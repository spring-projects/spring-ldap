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

import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.Test;
import org.springframework.ldap.BadLdapGrammarException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the LdapRdn class.
 *
 * @author Adam Skogman
 */
public class LdapRdnTests {

	@Test
	public void testLdapRdn_parse_simple() {

		LdapRdn rdn = new LdapRdn("foo=bar");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar");
		assertThat(rdn.getKey()).isEqualTo("foo");
		assertThat(rdn.getValue()).isEqualTo("bar");
	}

	@Test
	public void testLdapRdn_parse_spaces() {

		LdapRdn rdn = new LdapRdn(" foo = bar ");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar");
	}

	@Test
	public void testLdapRdn_parse_escape() {

		LdapRdn rdn = new LdapRdn("foo=bar\\=fum");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar=fum");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar\\=fum");
	}

	@Test
	public void testLdapRdn_parse_hexEscape() {

		LdapRdn rdn = new LdapRdn("foo=bar\\0dfum");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar\rfum");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar\\0Dfum");
	}

	@Test(expected = BadLdapGrammarException.class)
	public void testLdapRdn_parse_trailingBackslash() {
		new LdapRdn("foo=bar\\");
	}

	@Test
	public void testLdapRdn_parse_spaces_escape() {
		LdapRdn rdn = new LdapRdn(" foo = \\ bar\\20 \\  ");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo(" bar   ");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=\\ bar  \\ ");
	}

	@Test(expected = BadLdapGrammarException.class)
	public void testLdapRdn_parse_tooMuchTrim() {
		new LdapRdn("foo=bar\\");
	}

	@Test
	public void testLdapRdn_parse_slash() {
		LdapRdn rdn = new LdapRdn("ou=Clerical / Secretarial Staff");

		assertThat(rdn.getComponent().getKey()).isEqualTo("ou");
		assertThat(rdn.getComponent().getValue()).isEqualTo("Clerical / Secretarial Staff");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("ou=Clerical / Secretarial Staff");
	}

	@Test(expected = BadLdapGrammarException.class)
	public void testLdapRdn_parse_quoteInKey() {
		new LdapRdn("\"umanroleid=2583");
	}

	@Test
	public void testLdapRdn_KeyValue_simple() {
		LdapRdn rdn = new LdapRdn("foo", "bar");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar");
	}

	@Test
	public void testLdapRdn_KeyValue_valueNeedsEscape() {
		LdapRdn rdn = new LdapRdn("foo", "bar\\");

		assertThat(rdn.getComponent().getKey()).isEqualTo("foo");
		assertThat(rdn.getComponent().getValue()).isEqualTo("bar\\");
		assertThat(rdn.getComponent().getLdapEncoded()).isEqualTo("foo=bar\\\\");
	}

	@Test
	public void testEncodeUrl() {
		LdapRdn rdn = new LdapRdn("o = example.com ");
		assertThat(rdn.encodeUrl()).isEqualTo("o=example.com");
	}

	@Test
	public void testEncodeUrl_SpacesInValue() {
		LdapRdn rdn = new LdapRdn("o = my organization ");
		assertThat(rdn.encodeUrl()).isEqualTo("o=my%20organization");
	}

	@Test
	public void testLdapRdn_Parse_MultipleComponents() {
		LdapRdn rdn = new LdapRdn("cn=John Doe+sn=Doe");
		assertThat(rdn.getComponent(0).encodeLdap()).isEqualTo("cn=John Doe");
		assertThat(rdn.getComponent(1).encodeLdap()).isEqualTo("sn=Doe");
		assertThat(rdn.getLdapEncoded()).isEqualTo("cn=John Doe+sn=Doe");
		assertThat(rdn.getKey()).isEqualTo("cn");
		assertThat(rdn.getValue()).isEqualTo("John Doe");
		assertThat(rdn.getValue("cn")).isEqualTo("John Doe");
		assertThat(rdn.getValue("sn")).isEqualTo("Doe");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetValueNoKeyWithCorrectValue() {
		LdapRdn tested = new LdapRdn("cn=john doe");
		tested.getValue("sn");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetValueNoComponents() {
		LdapRdn tested = new LdapRdn();
		tested.getValue("sn");
	}

	@Test
	public void testEquals() throws Exception {
		// original object
		final Object originalObject = new LdapRdn("cn", "john.doe");

		// another object that has the same values as the original
		final Object identicalObject = new LdapRdn("cn", "john.doe");

		// another object with different values
		final Object differentObject = new LdapRdn("cn", "john.svensson");

		// a subclass with the same values as the original
		final Object subclassObject = new LdapRdn("cn", "john.doe") {
			private static final long serialVersionUID = 1L;

		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

	@Test
	public void testCompareTo_Equals() throws Exception {
		LdapRdn rdn1 = new LdapRdn("cn=john doe");
		LdapRdn rdn2 = new LdapRdn("cn=john doe");

		int result = rdn1.compareTo(rdn2);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void verifyThatEqualsDisregardsOrder_Ldap260() throws Exception {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("sn=doe+cn=john doe");

		assertThat(rdn2).as("Should be equal").isEqualTo(rdn1);
	}

	@Test
	public void verifyThatHashcodeDisregardsOrder_Ldap260() throws Exception {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("sn=doe+cn=john doe");

		assertThat(rdn2.hashCode()).as("Should be equal").isEqualTo(rdn1.hashCode());
	}

	@Test
	public void testCompareTo_EqualsComplex() throws Exception {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+sn=doe");

		int result = rdn1.compareTo(rdn2);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testCompareTo_LessWithMissingKey() {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+tn=doe");

		int result = rdn1.compareTo(rdn2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_LessWithExistingKey() {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doa");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+sn=doe");

		int result = rdn1.compareTo(rdn2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_Greater() {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+sn=doa");

		int result = rdn1.compareTo(rdn2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompareTo_Shorter() {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+sn=doe+description=tjo");

		int result = rdn1.compareTo(rdn2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_Longer() {
		LdapRdn rdn1 = new LdapRdn("cn=john doe+sn=doe+description=tjo");
		LdapRdn rdn2 = new LdapRdn("cn=john doe+sn=doe");

		int result = rdn1.compareTo(rdn2);
		assertThat(result > 0).isTrue();
	}

}
