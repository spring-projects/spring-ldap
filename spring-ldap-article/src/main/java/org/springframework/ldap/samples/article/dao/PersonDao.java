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

import org.springframework.ldap.samples.article.domain.Person;


/**
 * Data Access Object interface for the Person entity.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public interface PersonDao {
   void create(Person person);

   void update(Person person);

   void delete(Person person);

   List getAllPersonNames();

   List findAll();

   Person findByPrimaryKey(String country,
      String company, String fullname);
}
