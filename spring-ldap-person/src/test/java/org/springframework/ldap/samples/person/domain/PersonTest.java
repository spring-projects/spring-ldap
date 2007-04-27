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
package org.springframework.ldap.samples.person.domain;

import com.gargoylesoftware.base.testing.EqualsTester;

import junit.framework.TestCase;

public class PersonTest extends TestCase {

    public void testEquals() {
        Person original = new Person();
        original.setFullName("some name");
        original.setCompany("some company");
        Person identical = new Person();
        identical.setFullName("some name");
        identical.setCompany("some company");
        Person different = new Person();
        different.setFullName("other name");
        different.setCompany("some company");
        Person subclass = new Person() {
            private static final long serialVersionUID = 1L;};
        subclass.setFullName("some name");
        subclass.setCompany("some company");
        new EqualsTester(original, identical, different, subclass);
    }
}
