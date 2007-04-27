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

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import com.gargoylesoftware.base.testing.EqualsTester;

/**
 * Unit tests for the Group class.
 *
 * @author Ulrik Sandberg
 */
public class GroupTest extends TestCase {

    public void testEquals() {
        Set members = Collections.singleton("some dn");
        Set differentMembers = Collections.singleton("other dn");
        Group original = new Group();
        original.setName("some name");
        original.setMembers(members);
        Group identical = new Group();
        identical.setName("some name");
        identical.setMembers(members);
        Group different = new Group();
        different.setName("some name");
        different.setMembers(differentMembers);
        Group subclass = new Group() {
            private static final long serialVersionUID = 1L;
        };
        subclass.setName("some name");
        subclass.setMembers(members);
        new EqualsTester(original, identical, different, subclass);
    }
}
