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

package org.springframework.ldap.itest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.springframework.LdapDataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapClient;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link LdapClient}'s lookup methods.
 *
 * @author Josh Cummings
 */
@ContextConfiguration(locations = { "/conf/ldapClientTestContext.xml" })
public class DefaultLdapClientLookupMultiRdnITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapClient tested;

	protected Resource getLdifFileResource() {
		return new ClassPathResource("/setup_data_multi_rdn.ldif");
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which means more
	 * than one attribute is part of the relative DN for the entry.
	 */
	@Test
	@Category(NoAdTests.class)
	public void testLookup_MultiValuedRdn() {
		AttributesMapper<Person> mapper = new PersonAttributesMapper();
		Person person = this.tested.search().name("cn=Some Person+sn=Person, ou=company1,ou=Norway").toObject(mapper);
		assertThat(person.getFullname()).isEqualTo("Some Person");
		assertThat(person.getLastname()).isEqualTo("Person");
		assertThat(person.getDescription()).isEqualTo("Norway, Company1, Some Person+Person");
	}

	/**
	 * Verifies that we can lookup an entry that has a multi-valued rdn, which means more
	 * than one attribute is part of the relative DN for the entry.
	 *
	 */
	@Test
	@Category(NoAdTests.class)
	public void testLookup_MultiValuedRdn_DirContextAdapter() {
		LdapDataEntry result = this.tested.search().name("cn=Some Person+sn=Person, ou=company1,ou=Norway").toEntry();
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person");
		assertThat(result.getStringAttribute("description")).isEqualTo("Norway, Company1, Some Person+Person");
	}

	@Test
	@Category(NoAdTests.class)
	public void testLookup_GetNameInNamespace_MultiRdn() {
		DirContextOperations result = this.tested.search().name("cn=Some Person+sn=Person,ou=company1,ou=Norway")
				.toEntry();
		assertThat(result.getDn().toString()).isEqualTo("cn=Some Person+sn=Person,ou=company1,ou=Norway");
		assertThat(result.getNameInNamespace()).isEqualTo("cn=Some Person+sn=Person,ou=company1,ou=Norway," + base);
	}

}
