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

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.Person;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the still experimental virtual list view search result capability of
 * LdapTemplate.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateVirtualListViewSearchITest extends
        AbstractDependencyInjectionSpringContextTests {

    private static final String BASE_STRING = "";

    private static final String FILTER_STRING = "(&(objectclass=ikeaperson))";

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

    public void testSearch_VirtualListView() {
        Person person;
        List list;
        VirtualListViewResultsCookie cookie;
        VirtualListViewControlDirContextProcessor requestControl;

        // Prepare for first search
        requestControl = new VirtualListViewControlDirContextProcessor(3);
        tested.search(BASE_STRING, FILTER_STRING, searchControls,
                callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        int listSize = requestControl.getListSize();
        // assertNotNull("Cookie should not be null yet", cookie.getCookie());
        list = callbackHandler.getList();
        assertEquals(3, list.size());
        person = (Person) list.get(0);
        assertEquals("A OPER", person.getFullname());
        person = (Person) list.get(1);
        assertEquals("aaaa aaaa", person.getFullname());
        person = (Person) list.get(2);
        assertEquals("aaab aaab", person.getFullname());

        // Prepare for position 6000
        requestControl = new VirtualListViewControlDirContextProcessor(3, 6000, listSize,
                cookie);
        tested.search(BASE_STRING, FILTER_STRING, searchControls,
                callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        // assertNull("Cookie should be null now", cookie.getCookie());
        assertEquals(6, list.size());
        person = (Person) list.get(3);
        assertEquals("Jessica Santiago Moreno", person.getFullname());
        person = (Person) list.get(4);
        assertEquals("Jessica stenfeldt", person.getFullname());
        person = (Person) list.get(5);
        assertEquals("Jessica Svensson", person.getFullname());

        // Prepare for position 93%
        requestControl = new VirtualListViewControlDirContextProcessor(3, 93, listSize,
                cookie);
        requestControl.setOffsetPercentage(true);
        tested.search(BASE_STRING, FILTER_STRING, searchControls,
                callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        // assertNull("Cookie should be null now", cookie.getCookie());
        assertEquals(9, list.size());
        person = (Person) list.get(6);
        assertEquals("Thomas Ronnblom", person.getFullname());
        person = (Person) list.get(7);
        assertEquals("Thomas Rosenthal", person.getFullname());
        person = (Person) list.get(8);
        assertEquals("Thomas Rydell", person.getFullname());
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
