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

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for the {@link DistinguishedName} class.
 *
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 */
public class DistinguishedNameTest {

	@Test
	public void testDistinguishedName_CompositeWithSlash() throws Exception {
		Name testPath = new CompositeName("cn=foo\\/bar");
		DistinguishedName path = new DistinguishedName(testPath);
		assertThat(path.toString()).isEqualTo("cn=foo/bar");
	}

	@Test
	public void testDistinguishedName_CompositeWithSlashAsString() throws Exception {
		Name testPath = new CompositeName("cn=foo\\/bar");
		DistinguishedName path = new DistinguishedName(testPath.toString());
		assertThat(path.toString()).isEqualTo("cn=foo/bar");
	}

	@Test
	public void testDistinguishedName_Ldap237_NotDestroyedByCompositeName() throws InvalidNameException {
		DistinguishedName path = new DistinguishedName("ou=Roger \\\"Bunny\\\" Rabbit,dc=somecompany,dc=com");
		assertThat(path.toString()).isEqualTo("ou=Roger \\\"Bunny\\\" Rabbit,dc=somecompany,dc=com");
	}

	/**
	 * CompositeName screws up distinguished names when there are double qoutes, as
	 * described in Ldap237.
	 * @throws InvalidNameException
	 */
	@Test
	public void testDistinguishedName_Ldap237_DestroyedByCompositeName() throws InvalidNameException {
		DistinguishedName path = new DistinguishedName("ou=Roger \\\\\"Bunny\\\\\" Rabbit,dc=somecompany,dc=com");
		assertThat(path.toString()).isEqualTo("ou=Roger \\\"Bunny\\\" Rabbit,dc=somecompany,dc=com");
	}

	@Test
	public void testEmptyPathImmutable() throws Exception {
		DistinguishedName emptyPath = DistinguishedName.EMPTY_PATH;
		try {
			emptyPath.add("cn=John Doe");
			fail("UnsupportedOperationException expected");
		}
		catch (UnsupportedOperationException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testDistinguishedName() {

		String testPath = "cn=foo\\,bar,OU=FOO\\,bar , OU=foo\\;bar;OU=foo\\;bar"
				+ " ; ou=foo\\,,ou=foo\\,;ou=foo\\;;ou=foo\\,;ou=bar\\,";
		System.out.println(testPath);

		DistinguishedName path = new DistinguishedName(testPath);

		assertThat(path.getLdapRdn(8).getComponent().getKey()).isEqualTo("cn");
		assertThat(path.getLdapRdn(8).getComponent().getValue()).isEqualTo("foo,bar");
		assertThat(path.getLdapRdn(7).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(7).getComponent().getValue()).isEqualTo("FOO,bar");
		assertThat(path.getLdapRdn(6).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(6).getComponent().getValue()).isEqualTo("foo;bar");
		assertThat(path.getLdapRdn(5).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(5).getComponent().getValue()).isEqualTo("foo;bar");
		assertThat(path.getLdapRdn(4).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(4).getComponent().getValue()).isEqualTo("foo,");
		assertThat(path.getLdapRdn(3).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(3).getComponent().getValue()).isEqualTo("foo,");
		assertThat(path.getLdapRdn(2).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(2).getComponent().getValue()).isEqualTo("foo;");
		assertThat(path.getLdapRdn(1).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(1).getComponent().getValue()).isEqualTo("foo,");
		assertThat(path.getLdapRdn(0).getComponent().getKey()).isEqualTo("ou");
		assertThat(path.getLdapRdn(0).getComponent().getValue()).isEqualTo("bar,");
	}

	@Test
	public void testRemove() throws InvalidNameException {

		String testPath = "cn=john.doe, OU=Users,OU=Some Company,OU=G,OU=I,OU=M";
		DistinguishedName path = new DistinguishedName(testPath);

		path.remove(1);
		path.remove(3);

		assertThat(path.toString()).isEqualTo("cn=john.doe,ou=Some Company,ou=G,ou=M");
	}

	/**
	 * Tests parsing and toString.
	 */
	@Test
	public void testContains() {

		DistinguishedName migpath = new DistinguishedName("OU=G,OU=I,OU=M");
		DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,OU=G,OU=I,OU=M");
		DistinguishedName path2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path3 = new DistinguishedName("ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path4 = new DistinguishedName("ou=G,OU=i,ou=m");

		DistinguishedName pathE1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=L,OU=M, ou=foo");
		DistinguishedName pathE2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE");

		assertThat(path1.contains(migpath)).isTrue();
		assertThat(path2.contains(migpath)).isTrue();
		assertThat(path3.contains(migpath)).isTrue();
		assertThat(path4.contains(migpath)).isTrue();

		assertThat(pathE1.contains(migpath)).isFalse();
		assertThat(pathE2.contains(migpath)).isFalse();
	}

	@Test
	public void testAppend() {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.append(path2);

		assertThat(path1.toString()).isEqualTo("ou=baz,ou=foo,ou=bar");
	}

	@Test
	public void testPrepend() {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("cn=fie, OU=baz");

		path1.prepend(path2);

		assertThat(path1.toString()).isEqualTo("ou=foo,ou=bar,cn=fie,ou=baz");
	}

	@Test
	public void testEquals() throws Exception {

		// original object
		final Object originalObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE");

		// another object that has the same values as the original (case is
		// ignored)
		final Object identicalObject = new DistinguishedName("cn=john.doe, OU=Users,OU=SOME COMPANY,C=SE");

		// another object with different values
		final Object differentObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some other company,C=SE");

		// a subclass with the same values as the original
		final Object subclassObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE") {
			private static final long serialVersionUID = 1L;

		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

	@Test
	public void testClone() {

		DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE");

		DistinguishedName path2 = (DistinguishedName) path1.clone();

		assertThat(path2).as("Should be equal").isEqualTo(path1);

		path2.removeFirst();
		assertThat(path1.equals(path2)).isFalse();

	}

	@Test
	public void testEndsWith_true() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending1 = new DistinguishedName("uid=mtah.test");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU");

		assertThat(path1.endsWith(ending1)).isTrue();
		assertThat(path2.endsWith(ending2)).isTrue();
	}

	@Test
	public void testEndsWith_false() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending1 = new DistinguishedName("ou=people");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending2 = new DistinguishedName("ou=EU, o=example.com");

		assertThat(path1.endsWith(ending1)).isFalse();
		assertThat(path2.endsWith(ending2)).isFalse();
	}

	@Test
	public void testGetAll() throws Exception {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Enumeration elements = path.getAll();

		String element = (String) elements.nextElement();
		assertThat(element).isEqualTo("o=example.com");

		element = (String) elements.nextElement();
		assertThat(element).isEqualTo("ou=EU");

		element = (String) elements.nextElement();
		assertThat(element).isEqualTo("ou=people");

		element = (String) elements.nextElement();
		assertThat(element).isEqualTo("uid=mtah.test");
	}

	@Test
	public void testGet() throws Exception {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		String string = path.get(1);

		assertThat(string).isEqualTo("ou=EU");
	}

	@Test
	public void testSize() {
		DistinguishedName path1 = new DistinguishedName();
		assertThat(path1.size()).isEqualTo(0);

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		assertThat(path2.size()).isEqualTo(4);
	}

	@Test
	public void testGetPrefix() {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Name prefix = path.getPrefix(0);
		assertThat(prefix.size()).isEqualTo(0);

		prefix = path.getPrefix(1);
		assertThat(prefix.size()).isEqualTo(1);
		assertThat(prefix.get(0)).isEqualTo("o=example.com");

		prefix = path.getPrefix(2);
		assertThat(prefix.size()).isEqualTo(2);
		assertThat(prefix.get(0)).isEqualTo("o=example.com");
		assertThat(prefix.get(1)).isEqualTo("ou=EU");
	}

	@Test
	public void testGetSuffix() {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Name suffix = path.getSuffix(0);
		assertThat(suffix.size()).isEqualTo(4);

		suffix = path.getSuffix(2);
		assertThat(suffix.size()).isEqualTo(2);
		assertThat(suffix.get(0)).isEqualTo("ou=people");

		suffix = path.getSuffix(4);
		assertThat(suffix.size()).isEqualTo(0);

		try {
			path.getSuffix(5);
			fail("ArrayIndexOutOfBoundsException expected");
		}
		catch (ArrayIndexOutOfBoundsException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testStartsWith_true() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start1 = new DistinguishedName("o=example.com");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start2 = new DistinguishedName("ou=people, ou=EU, o=example.com");

		assertThat(path1.startsWith(start1)).isTrue();
		assertThat(path2.startsWith(start2)).isTrue();
	}

	@Test
	public void testStartsWith_false() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start1 = new DistinguishedName("ou=people");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start2 = new DistinguishedName("uid=mtah.test, ou=EU, ou=people");

		assertThat(path1.startsWith(start1)).isFalse();
		assertThat(path2.startsWith(start2)).isFalse();
	}

	@Test
	public void testStartsWith_Longer() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com, o=a.com");

		assertThat(path1.startsWith(path2)).isFalse();
	}

	@Test
	public void testStartsWith_EmptyPath() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		DistinguishedName path2 = new DistinguishedName();

		assertThat(path1.startsWith(path2)).isFalse();
	}

	@Test
	public void testIsEmpty_True() {
		DistinguishedName path = new DistinguishedName();
		assertThat(path.isEmpty()).isTrue();
	}

	@Test
	public void testIsEmpty_False() {
		DistinguishedName path = new DistinguishedName("o=example.com");
		assertThat(path.isEmpty()).isFalse();
	}

	@Test
	public void testAddAll() throws Exception {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.addAll(path2);

		assertThat(path1.toString()).isEqualTo("ou=baz,ou=foo,ou=bar");
	}

	@Test
	public void testAddAll_Index() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.addAll(1, path2);

		assertThat(path1.toString()).isEqualTo("ou=foo,ou=baz,ou=bar");
	}

	@Test
	public void testAdd() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, ou=bar");
		path1.add("ou=baz");

		assertThat(path1.toString()).isEqualTo("ou=baz,ou=foo,ou=bar");
	}

	@Test
	public void testAdd_Index() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, ou=bar");
		path1.add(1, "ou=baz");

		assertThat(path1.toString()).isEqualTo("ou=foo,ou=baz,ou=bar");
	}

	@Test
	public void testToUrl() {
		DistinguishedName path = new DistinguishedName("dc=jayway, dc=se");
		String url = path.toUrl();

		assertThat(url).isEqualTo("dc=jayway,dc=se");
	}

	@Test
	public void testMultiValueRdn() throws Exception {
		DistinguishedName path = new DistinguishedName("firstName=Rod+lastName=Johnson,ou=UK,dc=interface21,dc=com");
		assertThat(path.size()).isEqualTo(4);
		assertThat(path.get(3)).isEqualTo("firstname=Rod+lastname=Johnson");
	}

	@Test
	public void testCompareTo_Equals() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testCompareTo_Less() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=DK");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_Less_MoreSignificant() throws Exception {
		DistinguishedName name1 = new DistinguishedName("an=john doe, ou=Some company, c=DK");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testCompareTo_Greater() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=DK");

		int result = name1.compareTo(name2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompareTo_Longer() throws Exception {
		DistinguishedName name1 = new DistinguishedName("leaf=someleaf, cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertThat(result > 0).isTrue();
	}

	@Test
	public void testCompareTo_Shorter() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("leaf=someleaf, cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertThat(result < 0).isTrue();
	}

	@Test
	public void testGetLdapRdnForKey() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		LdapRdn ldapRdn = dn.getLdapRdn("ou");
		assertThat(ldapRdn).isEqualTo(new LdapRdn("ou=Some company"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLdapRdnForKeyNoMatchingKeyThrowsException() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		dn.getLdapRdn("nosuchkey");
	}

	@Test
	public void testGetValue() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		String value = dn.getValue("ou");
		assertThat(value).isEqualTo("Some company");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetValueNoMatchingKeyThrowsException() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		dn.getValue("nosuchkey");
	}

	@Test
	public void test_longDN() throws InvalidNameException {
		DistinguishedName name = new DistinguishedName("");
		assertThat(name).isNotNull();
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	@Test
	public void testParseAtSign() {
		DistinguishedName name = new DistinguishedName("cn=testname@example.com");
		assertThat(name).isNotNull();
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	@Test
	public void testParseAtSign2() {
		DistinguishedName name = new DistinguishedName("cn=te\\+stname@example.com");
		assertThat(name).isNotNull();
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	@Test(expected = BadLdapGrammarException.class)
	public void testParseInvalidPlus() {
		new DistinguishedName("cn=te+stname@example.com");
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	@Test
	public void testParseValidQuotation() {
		DistinguishedName name = new DistinguishedName("cn=jo\"hn doe");
		assertThat(name).isNotNull();
	}

	@Test
	public void testAppendChained() {
		DistinguishedName tested = new DistinguishedName("dc=mycompany,dc=com");
		tested.append("ou", "company1").append("cn", "john doe");

		assertThat(tested.toString()).isEqualTo("cn=john doe,ou=company1,dc=mycompany,dc=com");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableDistinguishedNameFailsToAddRdn() throws Exception {
		DistinguishedName result = DistinguishedName.immutableDistinguishedName("cn=john doe");
		result.add(new LdapRdn("somekey", "somevalue"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableDistinguishedNameFailsToModifyRdn() throws Exception {
		DistinguishedName result = DistinguishedName.immutableDistinguishedName("cn=john doe");
		LdapRdn ldapRdn = result.getLdapRdn(0);

		ldapRdn.addComponent(new LdapRdnComponent("somekey", "somevalue"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableDistinguishedNameFailsToModifyRdnComponentKey() throws Exception {
		DistinguishedName result = DistinguishedName.immutableDistinguishedName("cn=john doe");
		LdapRdnComponent component = result.getLdapRdn(0).getComponent();

		component.setKey("somekey");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableDistinguishedNameFailsToModifyRdnComponentValue() throws Exception {
		DistinguishedName result = DistinguishedName.immutableDistinguishedName("cn=john doe");
		LdapRdnComponent component = result.getLdapRdn(0).getComponent();

		component.setValue("somevalue");
	}

	@Test
	public void testUnmodifiableDistinguishedNameEqualsIdenticalMutableOne() throws Exception {
		DistinguishedName immutable = DistinguishedName.immutableDistinguishedName("cn=john doe");
		DistinguishedName mutable = new DistinguishedName("cn=john doe");
		assertThat(immutable.equals(mutable)).isTrue();
	}

	/**
	 * Test for LDAP-97.
	 */
	@Test
	public void testDistinguishedNameWithCRParsesProperly() {
		DistinguishedName name = new DistinguishedName("cn=foo \r bar");
		assertThat(name).isNotNull();
	}

	/**
	 * Test for https://forum.spring.io/showthread.php?t=86640.
	 */
	@Test
	public void testDistinguishedNameWithDotParsesProperly() {
		DistinguishedName name = new DistinguishedName("cn=first.last,OU=DevTest Users,DC=xyz,DC=com");
		assertThat(name.toCompactString()).isEqualTo("cn=first.last,ou=DevTest Users,dc=xyz,dc=com");
		DistinguishedName dn = new DistinguishedName();
		dn.parse("cn=first.last,OU=DevTest Users,DC=xyz,DC=com");
		assertThat(dn.getValue("cn")).isEqualTo("first.last");
		assertThat(dn.getValue("ou")).isEqualTo("DevTest Users");
		assertThat(dn.getLdapRdn(1).getValue()).isEqualTo("xyz");
		assertThat(dn.getLdapRdn(0).getValue()).isEqualTo("com");
	}

	@Test
	public void testToStringCompact() {
		try {
			DistinguishedName name = new DistinguishedName("cn=john doe, ou=company");
			// First check the default
			assertThat(name.toString()).isEqualTo("cn=john doe,ou=company");
			System.setProperty(DistinguishedName.SPACED_DN_FORMAT_PROPERTY, "true");
			assertThat(name.toString()).isEqualTo("cn=john doe, ou=company");
		}
		finally {
			// Always restore the system setting
			System.setProperty(DistinguishedName.SPACED_DN_FORMAT_PROPERTY, "");
		}
	}

	@Test
	public void testKeyCaseFoldNoneShouldEqualOriginalCasedKeys() throws Exception {
		try {
			String dnString = "ou=foo,Ou=bar,oU=baz,OU=bim";
			DistinguishedName name = new DistinguishedName(dnString);

			// First check the default
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");

			System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, DistinguishedName.KEY_CASE_FOLD_NONE);
			name = new DistinguishedName(dnString);
			System.out.println(dnString + " folded as \"" + DistinguishedName.KEY_CASE_FOLD_NONE + "\": " + name);
			assertThat(name.toString()).isEqualTo(dnString);
		}
		finally {
			// Always restore the system setting
			System.clearProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		}
	}

	@Test
	public void testKeyCaseFoldUpperShouldEqualUpperCasedKeys() throws Exception {
		try {
			String dnString = "ou=foo,Ou=bar,oU=baz,OU=bim";
			DistinguishedName name = new DistinguishedName(dnString);

			// First check the default
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");

			System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, DistinguishedName.KEY_CASE_FOLD_UPPER);
			name = new DistinguishedName(dnString);
			System.out.println(dnString + " folded as \"" + DistinguishedName.KEY_CASE_FOLD_UPPER + "\": " + name);
			assertThat(name.toString()).isEqualTo("OU=foo,OU=bar,OU=baz,OU=bim");
		}
		finally {
			// Always restore the system setting
			System.clearProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		}
	}

	@Test
	public void testKeyCaseFoldLowerShouldEqualLowerCasedKeys() throws Exception {
		try {
			String dnString = "ou=foo,Ou=bar,oU=baz,OU=bim";
			DistinguishedName name = new DistinguishedName(dnString);

			// First check the default
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");

			System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, DistinguishedName.KEY_CASE_FOLD_LOWER);
			name = new DistinguishedName(dnString);
			System.out.println(dnString + " folded as \"" + DistinguishedName.KEY_CASE_FOLD_LOWER + "\": " + name);
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");
		}
		finally {
			// Always restore the system setting
			System.clearProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		}
	}

	@Test
	public void testKeyCaseFoldNonsenseShoulddefaultToLowerCasedKeysAndLogWarning() throws Exception {
		try {
			String dnString = "ou=foo,Ou=bar,oU=baz,OU=bim";
			DistinguishedName name = new DistinguishedName(dnString);

			// First check the default
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");

			System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, "whatever");
			name = new DistinguishedName(dnString);
			System.out.println(dnString + " folded as \"whatever\": " + name);
			assertThat(name.toString()).isEqualTo("ou=foo,ou=bar,ou=baz,ou=bim");
		}
		finally {
			// Always restore the system setting
			System.clearProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		}
	}

	@Test
	public void testHashSignLdap229() {
		assertThat(new DistinguishedName("cn=Foo\\#Bar")).isEqualTo(new DistinguishedName("cn=Foo#Bar"));
	}

	@Test
	public void testEqualsSignLdap229() {
		assertThat(new DistinguishedName("cn=Foo\\=Bar")).isEqualTo(new DistinguishedName("cn=Foo=Bar"));
	}

	@Test
	public void testSpaceSignLdap229() {
		assertThat(new DistinguishedName("cn=Foo\\ Bar")).isEqualTo(new DistinguishedName("cn=Foo Bar"));
	}

}
