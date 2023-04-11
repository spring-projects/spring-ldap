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

package org.springframework.ldap.itest;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the lookup methods of LdapTemplate.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateLookupITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	/**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory}) being set in
	 * the ContextSource.
	 */
	@Test
	public void testLookup_Plain() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person2, ou=company1,ou=Sweden");

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person2");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
		assertThat(result.getStringAttribute("description")).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory}) being set in
	 * the ContextSource.
	 */
	@Test
	public void testLookupContextRoot() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("");

		assertThat(result.getDn().toString()).isEqualTo("");
		assertThat(result.getNameInNamespace()).isEqualTo(base);
	}

	@Test
	public void testLookup_AttributesMapper() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,ou=Sweden", mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	@Test
	public void testLookup_AttributesMapper_LdapName() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup(LdapUtils.newLdapName("cn=Some Person2, ou=company1,ou=Sweden"), mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * An {@link AttributesMapper} that only maps a subset of the full attributes list.
	 * Used in tests where the return attributes list has been limited.
	 *
	 * @author Ulrik Sandberg
	 */
	private final class SubsetPersonAttributesMapper implements AttributesMapper {

		/**
		 * Maps the <code>cn</code> attribute into a {@link Person} object. Also verifies
		 * that the other attributes haven't been set.
		 *
		 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
		 */
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			Person person = new Person();
			person.setFullname((String) attributes.get("cn").get());
			assertThat(attributes.get("sn")).as("sn should be null").isNull();
			assertThat(attributes.get("description")).as("description should be null").isNull();
			return person;
		}

	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the available
	 * attributes as return attributes.
	 */
	@Test
	public void testLookup_ReturnAttributes_AttributesMapper() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();

		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,ou=Sweden", new String[] { "cn" }, mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the available
	 * attributes as return attributes. Uses LdapName instead of plain string as name.
	 */
	@Test
	public void testLookup_ReturnAttributes_AttributesMapper_LdapName() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();
		Person person = (Person) tested.lookup(LdapUtils.newLdapName("cn=Some Person2, ou=company1,ou=Sweden"),
				new String[] { "cn" }, mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	/**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory}) being set in
	 * the ContextSource.
	 */
	@Test
	public void testLookup_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();
		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,ou=Sweden", mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the available
	 * attributes as return attributes.
	 */
	@Test
	public void testLookup_ReturnAttributes_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();

		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,ou=Sweden", new String[] { "cn" }, mapper);

		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).as("lastName should not be set").isNull();
		assertThat(person.getDescription()).as("description should not be set").isNull();
	}

	@Test
	public void testLookup_GetNameInNamespace_Plain() {
		String expectedDn = "cn=Some Person2, ou=company1,ou=Sweden";
		DirContextAdapter result = (DirContextAdapter) tested.lookup(expectedDn);

		LdapName expectedName = LdapUtils.newLdapName(expectedDn);
		assertThat(result.getDn()).isEqualTo(expectedName);
		assertThat(result.getNameInNamespace()).isEqualTo("cn=Some Person2,ou=company1,ou=Sweden," + base);
	}

}
