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

package org.springframework.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirObjectFactory;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextSource;

/**
 * Tests the lookup methods of LdapTemplate.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplateLookupITest extends AbstractLdapTemplateIntegrationTest {

	private LdapTemplate tested;

	protected String[] getConfigLocations() {
		return new String[] { "/conf/ldapTemplateTestContext.xml" };
	}

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	public void testLookup_Plain() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person2, ou=company1,c=Sweden");

		assertEquals("Some Person2", result.getStringAttribute("cn"));
		assertEquals("Person2", result.getStringAttribute("sn"));
		assertEquals("Sweden, Company1, Some Person2", result.getStringAttribute("description"));
	}

	public void testLookup_AttributesMapper() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,c=Sweden", mapper);

		assertEquals("Some Person2", person.getFullname());
		assertEquals("Person2", person.getLastname());
		assertEquals("Sweden, Company1, Some Person2", person.getDescription());
	}

	public void testLookup_AttributesMapper_DistinguishedName() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup(new DistinguishedName("cn=Some Person2, ou=company1,c=Sweden"), mapper);

		assertEquals("Some Person2", person.getFullname());
		assertEquals("Person2", person.getLastname());
		assertEquals("Sweden, Company1, Some Person2", person.getDescription());
	}

	/**
	 * An {@link AttributesMapper} that only maps a subset of the full
	 * attributes list. Used in tests where the return attributes list has been
	 * limited.
	 * 
	 * @author Ulrik Sandberg
	 */
	private final class SubsetPersonAttributesMapper implements AttributesMapper {
		/**
		 * Maps the <code>cn</code> attribute into a {@link Person} object.
		 * Also verifies that the other attributes haven't been set.
		 * 
		 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
		 */
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			Person person = new Person();
			person.setFullname((String) attributes.get("cn").get());
			assertNull("sn should be null", attributes.get("sn"));
			assertNull("description should be null", attributes.get("description"));
			return person;
		}
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes.
	 */
	public void testLookup_ReturnAttributes_AttributesMapper() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();

		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,c=Sweden", new String[] { "cn" }, mapper);

		assertEquals("Some Person2", person.getFullname());
		assertNull("lastName should not be set", person.getLastname());
		assertNull("description should not be set", person.getDescription());
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes. Uses DistinguishedName instead
	 * of plain string as name.
	 */
	public void testLookup_ReturnAttributes_AttributesMapper_DistinguishedName() {
		AttributesMapper mapper = new SubsetPersonAttributesMapper();
		Person person = (Person) tested.lookup(new DistinguishedName("cn=Some Person2, ou=company1,c=Sweden"),
				new String[] { "cn" }, mapper);

		assertEquals("Some Person2", person.getFullname());
		assertNull("lastName should not be set", person.getLastname());
		assertNull("description should not be set", person.getDescription());
	}

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	public void testLookup_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();
		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,c=Sweden", mapper);

		assertEquals("Some Person2", person.getFullname());
		assertEquals("Person2", person.getLastname());
		assertEquals("Sweden, Company1, Some Person2", person.getDescription());
	}

	/**
	 * Verifies that only the subset is used when specifying a subset of the
	 * available attributes as return attributes.
	 */
	public void testLookup_ReturnAttributes_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();

		Person person = (Person) tested.lookup("cn=Some Person2, ou=company1,c=Sweden", new String[] { "cn" }, mapper);

		assertEquals("Some Person2", person.getFullname());
		assertNull("lastName should not be set", person.getLastname());
		assertNull("description should not be set", person.getDescription());
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which
	 * means more than one attribute is part of the relative DN for the entry.
	 * 
	 * TODO Enable test when ApacheDS supports multi-valued rdns.
	 */
	public void DISABLED_testLookup_MultiValuedRdn() {
		AttributesMapper mapper = new PersonAttributesMapper();
		Person person = (Person) tested.lookup("cn=Some Person+sn=Person, ou=company1,c=Norway", mapper);

		assertEquals("Some Person", person.getFullname());
		assertEquals("Person", person.getLastname());
		assertEquals("Norway, Company1, Some Person2", person.getDescription());
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which
	 * means more than one attribute is part of the relative DN for the entry.
	 * 
	 * TODO Enable test when ApacheDS supports multi-valued rdns.
	 */
	public void DISABLED_testLookup_MultiValuedRdn_DirContextAdapter() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person+sn=Person, ou=company1,c=Norway");

		assertEquals("Some Person", result.getStringAttribute("cn"));
		assertEquals("Person", result.getStringAttribute("sn"));
		assertEquals("Norway, Company1, Some Person", result.getStringAttribute("description"));
	}

	public void testLookup_GetNameInNamespace_Plain() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person2, ou=company1,c=Sweden");

		assertEquals("cn=Some Person2, ou=company1, c=Sweden", result.getDn().toString());
		assertEquals("cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se", result.getNameInNamespace());
	}

	public void testLookup_GetNameInNamespace_MultiRdn() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup("cn=Some Person+sn=Person, ou=company1,c=Norway");

		assertEquals("cn=Some Person+sn=Person, ou=company1, c=Norway", result.getDn().toString());
		assertEquals("cn=Some Person+sn=Person, ou=company1, c=Norway, dc=jayway, dc=se", result.getNameInNamespace());
	}

	/**
	 * Tests bind and lookup with Java objects where the ContextSource already
	 * has a DirObjectFactory configured.
	 */
	public void testBindJavaObject() throws Exception {
		AbstractContextSource contextSource = (AbstractContextSource) tested.getContextSource();
		Class<? extends DirObjectFactory> originalObjectFactory = contextSource.getDirObjectFactory();
		try {
			contextSource.setDirObjectFactory(null);
			contextSource.afterPropertiesSet();
			Integer i = new Integer(54321);
			tested.bind("cn=myRandomInt", i, null);
			i = new Integer(12345);
			i = (Integer) tested.lookup("cn=myRandomInt");
			assertEquals(54321, i.intValue());
		}
		finally {
			// reset the DirObjectFactory so as not to disturb other tests
			contextSource.setDirObjectFactory(originalObjectFactory);
			contextSource.afterPropertiesSet();
		}
	}

	public void setTested(LdapTemplate tested) {
		this.tested = tested;
	}
}
