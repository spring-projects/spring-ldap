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
package org.springframework.ldap.control;

import java.util.LinkedList;
import java.util.List;

import org.springframework.ldap.control.PagedResult;
import org.springframework.ldap.control.PagedResultsCookie;

import com.gargoylesoftware.base.testing.EqualsTester;

import junit.framework.TestCase;

public class PagedResultTest extends TestCase {
    public void testEquals() throws Exception {
        List expectedList = new LinkedList();
        expectedList.add("dummy");
        List otherList = new LinkedList();
        otherList.add("different");

        PagedResult originalObject = new PagedResult(expectedList,
                new PagedResultsCookie(null));
        PagedResult identicalObject = new PagedResult(expectedList,
                new PagedResultsCookie(null));
        PagedResult differentObject = new PagedResult(otherList,
                new PagedResultsCookie(null));
        PagedResult subclassObject = new PagedResult(expectedList,
                new PagedResultsCookie(null)) {

        };

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}
