/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.core;

import java.util.Enumeration;
import java.util.List;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.core.DistinguishedName;

import junit.framework.TestCase;

import com.gargoylesoftware.base.testing.EqualsTester;

/**
 * Unit tests for the {@link DistinguishedName} class.
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class DistinguishedNameTest extends TestCase {

	public void testDistinguishedName_CompositeWithSlash() throws Exception {
		Name testPath = new CompositeName("cn=foo\\/bar");
		DistinguishedName path = new DistinguishedName(testPath.toString());
		assertEquals("cn=foo/bar", path.toString());
	}

	public void testEmptyPathImmutable() throws Exception {
		DistinguishedName emptyPath = DistinguishedName.EMPTY_PATH;
		try {
			emptyPath.add("cn=John Doe");
			fail("UnsupportedOperationException expected");
		}
		catch (UnsupportedOperationException expected) {
			assertTrue(true);
		}
	}

	public void testDistinguishedName() {

		String testPath = "cn=foo\\,bar,OU=FOO\\,bar , OU=foo\\;bar;OU=foo\\;bar"
				+ " ; ou=foo\\,,ou=foo\\,;ou=foo\\;;ou=foo\\,;ou=bar\\,";
		System.out.println(testPath);

		DistinguishedName path = new DistinguishedName(testPath);

		assertEquals("cn", path.getLdapRdn(8).getComponent().getKey());
		assertEquals("foo,bar", path.getLdapRdn(8).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(7).getComponent().getKey());
		assertEquals("FOO,bar", path.getLdapRdn(7).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(6).getComponent().getKey());
		assertEquals("foo;bar", path.getLdapRdn(6).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(5).getComponent().getKey());
		assertEquals("foo;bar", path.getLdapRdn(5).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(4).getComponent().getKey());
		assertEquals("foo,", path.getLdapRdn(4).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(3).getComponent().getKey());
		assertEquals("foo,", path.getLdapRdn(3).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(2).getComponent().getKey());
		assertEquals("foo;", path.getLdapRdn(2).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(1).getComponent().getKey());
		assertEquals("foo,", path.getLdapRdn(1).getComponent().getValue());
		assertEquals("ou", path.getLdapRdn(0).getComponent().getKey());
		assertEquals("bar,", path.getLdapRdn(0).getComponent().getValue());
	}

	public void testRemove() throws InvalidNameException {

		String testPath = "cn=john.doe, OU=Users,OU=Some Company,OU=G,OU=I,OU=M";
		DistinguishedName path = new DistinguishedName(testPath);

		path.remove(1);
		path.remove(3);

		assertEquals("cn=john.doe, ou=Some Company, ou=G, ou=M", path.toString());
	}

	/**
	 * Tests parsing and toString.
	 */
	public void testContains() {

		DistinguishedName migpath = new DistinguishedName("OU=G,OU=I,OU=M");
		DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,OU=G,OU=I,OU=M");
		DistinguishedName path2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path3 = new DistinguishedName("ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path4 = new DistinguishedName("ou=G,OU=i,ou=m");

		DistinguishedName pathE1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=L,OU=M, ou=foo");
		DistinguishedName pathE2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE");

		assertTrue("Contains MIG", path1.contains(migpath));
		assertTrue("Contains MIG", path2.contains(migpath));
		assertTrue("Contains MIG", path3.contains(migpath));
		assertTrue("Contains MIG", path4.contains(migpath));

		assertFalse("Does not contain MIG", pathE1.contains(migpath));
		assertFalse("Does not contain MIG", pathE2.contains(migpath));
	}

	public void testAppend() {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.append(path2);

		assertEquals("Append failed", "ou=baz, ou=foo, ou=bar", path1.toString());
	}

	public void testPrepend() {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("cn=fie, OU=baz");

		path1.prepend(path2);

		assertEquals("Append failed", "ou=foo, ou=bar, cn=fie, ou=baz", path1.toString());
	}

	public void testEquals() throws Exception {

		// original object
		final Object originalObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE");

		// another object that has the same values as the original
		final Object identicalObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE");

		// another object with different values
		final Object differentObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some other company,C=SE");

		// a subclass with the same values as the original
		final Object subclassObject = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE") {
			private static final long serialVersionUID = 1L;
		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

	public void testClone() {

		DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=Some company,C=SE");

		DistinguishedName path2 = (DistinguishedName) path1.clone();

		assertEquals("Should be equal", path1, path2);

		path2.removeFirst();
		assertFalse("Should not be equal", path1.equals(path2));

	}

	public void testEndsWith_true() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending1 = new DistinguishedName("uid=mtah.test");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU");

		assertTrue(path1.endsWith(ending1));
		assertTrue(path2.endsWith(ending2));
	}

	public void testEndsWith_false() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending1 = new DistinguishedName("ou=people");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName ending2 = new DistinguishedName("ou=EU, o=example.com");

		assertFalse(path1.endsWith(ending1));
		assertFalse(path2.endsWith(ending2));
	}

	public void testGetAll() throws Exception {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Enumeration elements = path.getAll();

		String element = (String) elements.nextElement();
		assertEquals("o=example.com", element);

		element = (String) elements.nextElement();
		assertEquals("ou=EU", element);

		element = (String) elements.nextElement();
		assertEquals("ou=people", element);

		element = (String) elements.nextElement();
		assertEquals("uid=mtah.test", element);
	}

	public void testGet() throws Exception {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		String string = path.get(1);

		assertEquals("ou=EU", string);
	}

	public void testSize() {
		DistinguishedName path1 = new DistinguishedName();
		assertEquals(0, path1.size());

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		assertEquals(4, path2.size());
	}

	public void testGetPrefix() {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Name prefix = path.getPrefix(0);
		assertEquals(0, prefix.size());

		prefix = path.getPrefix(1);
		assertEquals(1, prefix.size());
		assertEquals("o=example.com", prefix.get(0));

		prefix = path.getPrefix(2);
		assertEquals(2, prefix.size());
		assertEquals("o=example.com", prefix.get(0));
		assertEquals("ou=EU", prefix.get(1));
	}

	public void testGetSuffix() {
		DistinguishedName path = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		Name suffix = path.getSuffix(0);
		assertEquals(4, suffix.size());

		suffix = path.getSuffix(2);
		assertEquals(2, suffix.size());
		assertEquals("ou=people", suffix.get(0));

		suffix = path.getSuffix(4);
		assertEquals(0, suffix.size());

		try {
			path.getSuffix(5);
			fail("ArrayIndexOutOfBoundsException expected");
		}
		catch (ArrayIndexOutOfBoundsException expected) {
			assertTrue(true);
		}
	}

	public void testStartsWith_true() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start1 = new DistinguishedName("o=example.com");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start2 = new DistinguishedName("ou=people, ou=EU, o=example.com");

		assertTrue(path1.startsWith(start1));
		assertTrue(path2.startsWith(start2));
	}

	public void testStartsWith_false() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start1 = new DistinguishedName("ou=people");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");
		DistinguishedName start2 = new DistinguishedName("uid=mtah.test, ou=EU, ou=people");

		assertFalse(path1.startsWith(start1));
		assertFalse(path2.startsWith(start2));
	}

	public void testStartsWith_Longer() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		DistinguishedName path2 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com, o=a.com");

		assertFalse(path1.startsWith(path2));
	}

	public void testStartsWith_EmptyPath() {
		DistinguishedName path1 = new DistinguishedName("uid=mtah.test, ou=people, ou=EU, o=example.com");

		DistinguishedName path2 = new DistinguishedName();

		assertFalse(path1.startsWith(path2));
	}

	public void testIsEmpty_True() {
		DistinguishedName path = new DistinguishedName();
		assertTrue(path.isEmpty());
	}

	public void testIsEmpty_False() {
		DistinguishedName path = new DistinguishedName("o=example.com");
		assertFalse(path.isEmpty());
	}

	public void testAddAll() throws Exception {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.addAll(path2);

		assertEquals("AddAll failed", "ou=baz, ou=foo, ou=bar", path1.toString());
	}

	public void testAddAll_Index() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, OU=bar");
		DistinguishedName path2 = new DistinguishedName("OU=baz");

		path1.addAll(1, path2);

		assertEquals("AddAll failed", "ou=foo, ou=baz, ou=bar", path1.toString());
	}

	public void testAdd() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, ou=bar");
		path1.add("ou=baz");

		assertEquals("Add failed", "ou=baz, ou=foo, ou=bar", path1.toString());
	}

	public void testAdd_Index() throws InvalidNameException {
		DistinguishedName path1 = new DistinguishedName("ou=foo, ou=bar");
		path1.add(1, "ou=baz");

		assertEquals("Add failed", "ou=foo, ou=baz, ou=bar", path1.toString());
	}

	public void testToUrl() {
		DistinguishedName path = new DistinguishedName("dc=jayway, dc=se");
		String url = path.toUrl();

		assertEquals("dc=jayway,dc=se", url);
	}

	public void testMultiValueRdn() throws Exception {
		DistinguishedName path = new DistinguishedName("firstName=Rod+lastName=Johnson,ou=UK,dc=interface21,dc=com");
		assertEquals(4, path.size());
		assertEquals("firstname=Rod+lastname=Johnson", path.get(3));
	}

	public void testCompareTo_Equals() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertEquals(0, result);
	}

	public void testCompareTo_Less() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=DK");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertTrue(result < 0);
	}

	public void testCompareTo_Less_MoreSignificant() throws Exception {
		DistinguishedName name1 = new DistinguishedName("an=john doe, ou=Some company, c=DK");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertTrue(result < 0);
	}

	public void testCompareTo_Greater() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=DK");

		int result = name1.compareTo(name2);
		assertTrue(result > 0);
	}

	public void testCompareTo_Longer() throws Exception {
		DistinguishedName name1 = new DistinguishedName("leaf=someleaf, cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertTrue(result > 0);
	}

	public void testCompareTo_Shorter() throws Exception {
		DistinguishedName name1 = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		DistinguishedName name2 = new DistinguishedName("leaf=someleaf, cn=john doe, ou=Some company, c=SE");

		int result = name1.compareTo(name2);
		assertTrue(result < 0);
	}

	public void testGetLdapRdnForKey() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		LdapRdn ldapRdn = dn.getLdapRdn("ou");
		assertEquals(new LdapRdn("ou=Some company"), ldapRdn);
	}

	public void testGetLdapRdnForKeyNoMatchingKeyThrowsException() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		try {
			dn.getLdapRdn("nosuchkey");
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertTrue(true);
		}
	}

	public void testGetValue() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		String value = dn.getValue("ou");
		assertEquals("Some company", value);
	}

	public void testGetValueNoMatchingKeyThrowsException() throws Exception {
		DistinguishedName dn = new DistinguishedName("cn=john doe, ou=Some company, c=SE");
		try {
			dn.getValue("nosuchkey");
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertTrue(true);
		}
	}

	public void test_longDN() throws InvalidNameException {
		DistinguishedName name = new DistinguishedName("");
		assertNotNull(name);
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	public void testParseAtSign() {
		DistinguishedName name = new DistinguishedName("cn=testname@example.com");
		assertNotNull(name);
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	public void testParseAtSign2() {
		DistinguishedName name = new DistinguishedName("cn=te\\+stname@example.com");
		assertNotNull(name);
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	public void testParseInvalidPlus() {
		try {
			new DistinguishedName("cn=te+stname@example.com");
			fail("BadLdapGrammarException expected");
		}
		catch (BadLdapGrammarException expected) {
			assertTrue(true);
		}
	}

	/**
	 * Test case to verify correct parsing for issue on forums.
	 */
	public void testParseValidQuotation() {
		DistinguishedName name = new DistinguishedName("cn=jo\"hn doe");
		assertNotNull(name);
	}
	
	public void testAppendChained() {
		DistinguishedName tested = new DistinguishedName("dc=mycompany,dc=com");
		tested.append("ou", "company1").append("cn", "john doe");

		assertEquals("cn=john doe, ou=company1, dc=mycompany, dc=com", tested.toString());
	}

	public void testUnmodifiableDistinguishedName() throws Exception {
		DistinguishedName name = new DistinguishedName("cn=john doe");

		DistinguishedName result = name.immutableDistinguishedName();
		List names = result.getNames();
		try {
			names.add(new LdapRdnComponent("somekey", "somevalue"));
			fail("UnsupportedOperationException expected");
		}
		catch (UnsupportedOperationException expected) {
			assertTrue(true);
		}
	}
}
