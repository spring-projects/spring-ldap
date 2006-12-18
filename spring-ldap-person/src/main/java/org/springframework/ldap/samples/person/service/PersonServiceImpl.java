/*
 * Copyright 2005-2006 the original author or authors.
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
package org.springframework.ldap.samples.person.service;

import java.util.List;

import org.springframework.ldap.samples.person.dao.PersonDao;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Service implementation for managing the Person entity.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PersonServiceImpl implements PersonService {

    private PersonDao personDao;

    /*
     * @see org.springframework.ldap.samples.person.service.PersonService#create(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String[])
     */
    public void create(String country, String company, String fullname,
            String lastname, String[] description) {

        Person person = new Person();
        person.setCountry(country);
        person.setCompany(company);
        person.setFullName(fullname);
        person.setLastName(lastname);
        person.setDescription(description);

        personDao.create(person);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.PersonService#update(org.springframework.ldap.samples.person.domain.Person)
     */
    public void update(Person person) {
        personDao.update(person);
    }

    public void delete(Person person) {
        personDao.delete(person);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.PersonService#findByPrimaryKey(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Person findByPrimaryKey(String country, String company, String name) {
        return personDao.findByPrimaryKey(country, company, name);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.PersonService#findAll()
     */
    public List findAll() {
        return personDao.findAll();
    }

    /*
     * @see org.springframework.ldap.samples.person.service.PersonService#find(org.springframework.ldap.samples.person.domain.SearchCriteria)
     */
    public List find(SearchCriteria criteria) {
        return personDao.find(criteria);
    }

    public void setPersonDao(PersonDao personDao) {
        this.personDao = personDao;
    }
}
