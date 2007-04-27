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
package org.springframework.ldap.samples.article.dao;

import java.util.List;


import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.ldap.samples.article.dao.PersonDao;
import org.springframework.ldap.samples.article.domain.Person;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Abstract base class for PersonDao integration tests.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public abstract class AbstractPersonDaoIntegrationTest
         extends
         AbstractDependencyInjectionSpringContextTests {

   protected Person person;

   protected PersonDao personDao;

   protected String[] getConfigLocations() {
      return new String[] { "config/testContext.xml" };
   }

   protected void onSetUp() throws Exception {
      super.onSetUp();
      person = new Person();
      person.setCountry("Sweden");
      person.setCompany("company1");
      person.setFullName("Some Person");
      person.setLastName("Person");
      person
         .setDescription("Sweden, Company1, Some Person");
      person.setPhone("+46 555-123456");
   }

   protected void onTearDown() throws Exception {
      super.onTearDown();
      person = null;
      personDao = null;
   }

   /**
    * Having a single test method test create, update and delete is not exactly
    * the ideal way of testing, since they depend on each other. A better way
    * would be to separate the tests and load a test fixture before each
    * operation, in order to guarantee the expected state every time. See the
    * ldaptemplate-person sample for the correct way to do this.
    */
   public void testCreateUpdateDelete() {
      try {
         person.setFullName("Another Person");
         personDao.create(person);
         personDao.findByPrimaryKey(
            "Sweden", "company1",
            "Another Person");
         // if we got here, create succeeded

         person
            .setDescription("Another description");
         personDao.update(person);
         Person result = personDao
            .findByPrimaryKey(
               "Sweden", "company1",
               "Another Person");
         assertEquals(
            "Another description", result
               .getDescription());
      } finally {
         personDao.delete(person);
         try {
            personDao.findByPrimaryKey(
               "Sweden", "company1",
               "Another Person");
            fail("DataRetrievalFailureException expected");
         } catch (DataRetrievalFailureException expected) {
            // expected
         }
      }
   }

   public void testGetAllPersonNames() {
      List result = personDao.getAllPersonNames();
      assertEquals(5, result.size());
      String first = (String) result.get(0);
      assertEquals("Some Person", first);
   }

   public void testFindAll() {
      List result = personDao.findAll();
      assertEquals(5, result.size());
      Person first = (Person) result.get(0);
      assertEquals("Some Person", first
         .getFullName());
   }

   public void testFindByPrimaryKey() {
      Person result = personDao.findByPrimaryKey(
         "Sweden", "company1", "Some Person");
      assertEquals(person, result);
   }
}
