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

import java.util.List;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.Person;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the still experimental sorted search result capability of LdapTemplate.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateSortedSearchITest extends
        AbstractDependencyInjectionSpringContextTests {

    private static final Name BASE = DistinguishedName.EMPTY_PATH;

    private static final String FILTER_STRING = "(&(objectclass=ikeaperson)(cn=gor*))";

    private LdapTemplate tested;

    private CollectingNameClassPairCallbackHandler callbackHandler;

    private SearchControls searchControls;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext-openldap.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();
        PersonAttributesMapper mapper = new PersonAttributesMapper();
        callbackHandler = new AttributesMapperCallbackHandler(mapper);
        searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();
        callbackHandler = null;
        tested = null;
        searchControls = null;
    }

    public void testSearch_SortControl() {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws NamingException {
                return ctx.search(BASE, FILTER_STRING, searchControls);
            }
        };
        SortControlDirContextProcessor requestControl;

        // Prepare for first search
        requestControl = new SortControlDirContextProcessor("cn");
        tested.search(searchExecutor, callbackHandler, requestControl);
        int resultCode = requestControl.getResultCode();
        boolean sorted = requestControl.isSorted();
        assertTrue("Search result should have been sorted: " + resultCode, sorted);
        List list = callbackHandler.getList();
        assertSortedList(list);
    }

    public void testSearch_SortControl_ConvenienceMethod() {
        SortControlDirContextProcessor requestControl;

        // Prepare for first search
        requestControl = new SortControlDirContextProcessor("cn");
        tested.search(BASE, FILTER_STRING, searchControls, callbackHandler,
                requestControl);
        int resultCode = requestControl.getResultCode();
        boolean sorted = requestControl.isSorted();
        assertTrue("Search result should have been sorted: " + resultCode, sorted);
        List list = callbackHandler.getList();
        assertSortedList(list);
    }

    private void assertSortedList(List list) {
        Person person;
        assertEquals(6, list.size());
        person = (Person) list.get(0);
        assertEquals("Goran Milenkovic", person.getFullname());
        person = (Person) list.get(1);
        assertEquals("Goran Sundberg", person.getFullname());
        person = (Person) list.get(2);
        assertEquals("Goran Westerberg", person.getFullname());
        person = (Person) list.get(3);
        assertEquals("Gorana  Milicevic", person.getFullname());
        person = (Person) list.get(4);
        assertEquals("Gordana Canic", person.getFullname());
        person = (Person) list.get(5);
        assertEquals("Gordana  Russ", person.getFullname());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }

    private class PersonAttributesMapper implements AttributesMapper {

        /**
         * Maps the given attributes into a {@link Person} object.
         * 
         * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
         */
        public Object mapFromAttributes(Attributes attributes)
                throws NamingException {
            Person person = new Person();
            person.setFullname((String) attributes.get("cn").get());
            person.setLastname((String) attributes.get("sn").get());
            person.setDescription((String) attributes.get("givenName").get());
            return person;
        }
    }
}
