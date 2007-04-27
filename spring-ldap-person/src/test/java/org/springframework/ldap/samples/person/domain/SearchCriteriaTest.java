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

import junit.framework.TestCase;

import com.gargoylesoftware.base.testing.EqualsTester;

public class SearchCriteriaTest extends TestCase {

    public void testEquals() {
        SearchCriteria original = new SearchCriteria();
        original.setName("some");
        SearchCriteria identical = new SearchCriteria();
        identical.setName("some");
        SearchCriteria different = new SearchCriteria();
        different.setName("other");
        SearchCriteria subclass = new SearchCriteria() {
            private static final long serialVersionUID = 1L;};
        subclass.setName("some");
        new EqualsTester(original, identical, different, subclass);
    }
}
