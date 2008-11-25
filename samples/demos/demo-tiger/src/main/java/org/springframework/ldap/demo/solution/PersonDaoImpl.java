/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.demo.solution;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.demo.dao.PersonDao;
import org.springframework.ldap.demo.domain.Person;

/**
 * Spring LDAP implementation of PersonDao. This implementation uses many Spring
 * LDAP features, such as the {@link DirContextAdapter},
 * {@link AbstractParameterizedContextMapper}, and {@link SimpleLdapTemplate}. The purpose is to
 * contrast this implementation with that of {@link TraditionalPersonDaoImpl}.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class PersonDaoImpl implements PersonDao {

	private static final class PersonContextMapper extends
			AbstractParameterizedContextMapper<Person> {
		@Override
		protected Person doMapFromContext(DirContextOperations ctx) {
			Person person = new Person();
			person.setFullName(ctx.getStringAttribute("cn"));
			person.setLastName(ctx.getStringAttribute("sn"));
			person.setDescription(ctx.getStringAttribute("description"));
			person.setPhone(ctx.getStringAttribute("telephoneNumber"));

			DistinguishedName dn = (DistinguishedName) ctx.getDn();
			person.setCountry(dn.getValue("c"));
			person.setCompany(dn.getValue("ou"));
			return person;
		}
	}

	private SimpleLdapTemplate ldapTemplate;

	public void setLdapTemplate(SimpleLdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/*
	 * @see PersonDao#create(Person)
	 */
	public void create(Person person) {
		DirContextOperations ctx = new DirContextAdapter(buildDn(person));
		mapToContext(person, ctx);
		ldapTemplate.bind(ctx);
	}

	/*
	 * @see PersonDao#update(Person)
	 */
	public void update(Person person) {
		DirContextOperations ctx = ldapTemplate.lookupContext(buildDn(person));
		mapToContext(person, ctx);
		ldapTemplate.modifyAttributes(ctx);
	}

	/*
	 * @see PersonDao#delete(Person)
	 */
	public void delete(Person person) {
		ldapTemplate.unbind(buildDn(person));
	}

	/*
	 * @see PersonDao#getAllPersonNames()
	 */
	public List<String> getAllPersonNames() {
		return ldapTemplate.search("", "(objectclass=person)",
				new AbstractParameterizedContextMapper<String>() {
					@Override
					protected String doMapFromContext(DirContextOperations ctx) {
						return ctx.getStringAttribute("cn");
					}
				});
	}

	/*
	 * @see PersonDao#findAll()
	 */
	public List<Person> findAll() {
		return ldapTemplate.search("", "(objectclass=person)",
				new PersonContextMapper());
	}

	/*
	 * @see PersonDao#findByPrimaryKey(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public Person findByPrimaryKey(String country, String company,
			String fullname) {
		Name dn = buildDn(country, company, fullname);
		return (Person) ldapTemplate.lookup(dn, new PersonContextMapper());
	}

	private Name buildDn(Person person) {
		return buildDn(person.getCountry(), person.getCompany(), person
				.getFullName());
	}

	private Name buildDn(String country, String company, String fullname) {
		DistinguishedName dn = new DistinguishedName();
		dn.append("c", country);
		dn.append("ou", company);
		dn.append("cn", fullname);
		return dn;
	}

	private void mapToContext(Person person, DirContextOperations ctx) {
		ctx.setAttributeValues("objectclass", new String[] { "top", "person" });
		ctx.setAttributeValue("cn", person.getFullName());
		ctx.setAttributeValue("sn", person.getLastName());
		ctx.setAttributeValue("description", person.getDescription());
		ctx.setAttributeValue("telephoneNumber", person.getPhone());
	}
}
