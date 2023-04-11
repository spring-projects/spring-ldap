/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.samples.odm.dao;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.samples.plain.dao.PersonDao;
import org.springframework.ldap.samples.plain.domain.Person;
import org.springframework.ldap.support.LdapNameBuilder;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Default implementation of PersonDao. This implementation uses the Object-Directory Mapping feature,
 * which requires the entity classes to be annotated, but relieves the programmer from the tedious
 * task of mapping to and from entity objects, using attribute or dn component values.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class OdmPersonDaoImpl implements PersonDao {

	private LdapTemplate ldapTemplate;

	@Override
	public void create(Person person) {
		ldapTemplate.create(person);
	}

	@Override
	public void update(Person person) {
		ldapTemplate.update(person);
	}

	@Override
	public void delete(Person person) {
		ldapTemplate.delete(ldapTemplate.findByDn(buildDn(person), Person.class));
	}

	@Override
	public List<String> getAllPersonNames() {
		return ldapTemplate.search(query()
				.attributes("cn")
				.where("objectclass").is("person"),
				new AttributesMapper<String>() {
					public String mapFromAttributes(Attributes attrs) throws NamingException {
						return attrs.get("cn").get().toString();
					}
				});
	}

	@Override
	public List<Person> findAll() {
		return ldapTemplate.findAll(Person.class);
	}

	@Override
	public Person findByPrimaryKey(String country, String company, String fullname) {
		LdapName dn = buildDn(country, company, fullname);
		Person person = ldapTemplate.findByDn(dn, Person.class);

		return person;
	}

	private LdapName buildDn(Person person) {
		return buildDn(person.getCountry(), person.getCompany(), person.getFullName());
	}

	private LdapName buildDn(String country, String company, String fullname) {
		return LdapNameBuilder.newInstance()
				.add("c", country)
				.add("ou", company)
				.add("cn", fullname)
				.build();
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
}
