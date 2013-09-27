/*
 * Copyright 2005-2010 the original author or authors.
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
package org.springframework.ldap.samples.plain.dao;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.samples.plain.domain.Person;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Default implementation of PersonDao. This implementation uses
 * DirContextAdapter for managing attribute values. We use a ContextMapper
 * to map from the found contexts to our domain objects. This is especially useful
 * since we in this case have properties in our domain objects that depend on parts of the DN.
 * 
 * We could have worked with Attributes and an AttributesMapper implementation
 * instead, but working with Attributes is a bore and also, working with
 * AttributesMapper objects (or, indeed Attributes) does not give us access to
 * the distinguished name. However, we do use it in one method that only needs a
 * single attribute: {@link #getAllPersonNames()}.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class PersonDaoImpl implements PersonDao {

	private LdapTemplate ldapTemplate;

    @Override
	public void create(Person person) {
		person.setDn(buildDn(person));
		ldapTemplate.create(person);
	}

    @Override
	public void update(Person person) {
        person.setDn(buildDn(person));
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

        // TODO: This needs to happen automatically
        person.setCountry(country);
        person.setCompany(company);
        person.setFullName(fullname);

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
