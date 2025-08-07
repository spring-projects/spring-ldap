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

package org.springframework.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the lookup methods of LdapTemplate on OpenLdap.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplateLookupOpenLdapITests extends
		AbstractDependencyInjectionSpringContextTests {

	private LdapTemplate tested;

	protected String[] getConfigLocations() {
		return new String[] { "/conf/ldapTemplateTestContext-openldap.xml" };
	}

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	public void testLookup_Plain() {
		DirContextAdapter result = (DirContextAdapter) tested
				.lookup("cn=Some Person2, ou=company1,c=Sweden");

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person2");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
		assertEquals("Sweden, Company1, Some Person2", result
				.getStringAttribute("description"));
	}

	public void testLookup_AttributesMapper() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup(
				"cn=Some Person2, ou=company1,c=Sweden", mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	public void testLookup_AttributesMapper_DistinguishedName() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup(new DistinguishedName(
				"cn=Some Person2, ou=company1,c=Sweden"), mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * An {@link AttributesMapper} that only maps a subset of the full
	 * attributes list. Used in tests where the return attributes list has been
	 * limited.
	 * 
	 * @author Ulrik Sandberg
	 */
	private final class SubsetPersonAttributesMapper implements
			AttributesMapper {
		/**
		 * Maps the <code>cn</code> attribute into a {@link Person} object.
		 * Also verifies that the other attributes haven't been set.
		 * 
		 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
		 */
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			Person person = new Person();
			person.setFullname((String) attributes.get("cn").get());
			assertThat(attributes.get("sn")).as("sn should be null").isNull();
			assertNull("description should be null", attributes
					.get("description"));
			return person;
		}
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes.
	 */
	public void testLookup_ReturnAttributes_AttributesMapper() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();

		Person person = (Person) tested.lookup(
				"cn=Some Person2, ou=company1,c=Sweden", new String[] { "cn" },
				mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes. Uses DistinguishedName instead
	 * of plain string as name.
	 */
	public void testLookup_ReturnAttributes_AttributesMapper_DistinguishedName() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();
		Person person = (Person) tested.lookup(new DistinguishedName(
				"cn=Some Person2, ou=company1,c=Sweden"),
				new String[] { "cn" }, mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	public void testLookup_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();
		Person person = (Person) tested.lookup(
				"cn=Some Person2, ou=company1,c=Sweden", mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes.
	 */
	public void testLookup_ReturnAttributes_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();

		Person person = (Person) tested.lookup(
				"cn=Some Person2, ou=company1,c=Sweden", new String[] { "cn" },
				mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which
	 * means more than one attribute is part of the relative DN for the entry.
	 */
	public void testLookup_MultiValuedRdn() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup(
				"cn=Some Person+sn=Person, ou=company1,c=Norway", mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person");
		assertThat(person.getLastname()).isEqualTo("Person");
		assertEquals("Norway, Company1, Some Person+Person", person
				.getDescription());
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which
	 * means more than one attribute is part of the relative DN for the entry.
	 */
	public void testLookup_MultiValuedRdn_DirContextAdapter() {
		DirContextAdapter result = (DirContextAdapter) tested
				.lookup("cn=Some Person+sn=Person, ou=company1,c=Norway");

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person");
		assertEquals("Norway, Company1, Some Person+Person", result
				.getStringAttribute("description"));
	}

	public void testLookup_GetNameInNamespace_Plain() {
		DirContextAdapter result = (DirContextAdapter) tested
				.lookup("cn=Some Person2, ou=company1,c=Sweden");

		assertThat(result.getDn().isEqualTo("cn=Some Person2, ou=company1, c=Sweden")
				.toString());
		assertEquals(
				"cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se",
				result.getNameInNamespace());
	}

	public void testLookup_GetNameInNamespace_MultiRdn() {
		DirContextAdapter result = (DirContextAdapter) tested
				.lookup("cn=Some Person+sn=Person, ou=company1,c=Norway");

		assertEquals("cn=Some Person+sn=Person, ou=company1, c=Norway", result
				.getDn().toString());
		assertEquals(
				"cn=Some Person+sn=Person, ou=company1, c=Norway, dc=jayway, dc=se",
				result.getNameInNamespace());
	}

	public void setTested(LdapTemplate tested) {
		this.tested = tested;
	}
}
