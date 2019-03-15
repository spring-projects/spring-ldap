/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.Person;
import org.springframework.ldap.PersonAttributesMapper;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the paged search result capability of LdapTemplate.
 * <p>
 * Note: Currently, ApacheDS does not support paged results controls, so this
 * test must be run under another directory server, for example OpenLdap. This
 * test will not run under ApacheDS, and the other integration tests assume
 * ApacheDS and will probably not run under OpenLdap.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplatePagedSearchITest extends
        AbstractDependencyInjectionSpringContextTests {

    private static final Name BASE = DistinguishedName.EMPTY_PATH;

    private static final String FILTER_STRING = "(&(objectclass=person))";

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

    public void testSearch_PagedResult() {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws NamingException {
                return ctx.search(BASE, FILTER_STRING, searchControls);
            }
        };
        Person person;
        List list;
        PagedResultsCookie cookie;
        PagedResultsRequestControl requestControl;

        // Prepare for first search
        requestControl = new PagedResultsRequestControl(3);
        tested.search(searchExecutor, callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertThat(cookie.getCookie()).as("Cookie should not be null yet").isNotNull();
        list = callbackHandler.getList();
        assertThat(list).hasSize(3);
        person = (Person) list.get(0);
        assertThat(person.getFullname()).isEqualTo("Some Person");
        assertThat(person.getPhone()).isEqualTo("+46 555-123456");
        person = (Person) list.get(1);
        assertThat(person.getFullname()).isEqualTo("Some Person2");
        assertThat(person.getPhone()).isEqualTo("+46 555-654321");
        person = (Person) list.get(2);
        assertThat(person.getFullname()).isEqualTo("Some Person3");
        assertThat(person.getPhone()).isEqualTo("+46 555-123654");

        // Prepare for second and last search
        requestControl = new PagedResultsRequestControl(3, cookie);
        tested.search(searchExecutor, callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertThat(cookie.getCookie()).as("Cookie should be null now").isNull();
        assertThat(list).hasSize(5);
        person = (Person) list.get(3);
        assertThat(person.getFullname()).isEqualTo("Some Person");
        assertThat(person.getPhone()).isEqualTo("+46 555-456321");
        person = (Person) list.get(4);
        assertThat(person.getFullname()).isEqualTo("Some Person");
        assertThat(person.getPhone()).isEqualTo("+45 555-654123");
    }

    public void testSearch_PagedResult_ConvenienceMethod() {
        Person person;
        List list;
        PagedResultsCookie cookie;
        PagedResultsRequestControl requestControl;

        // Prepare for first search
        requestControl = new PagedResultsRequestControl(3);
        tested.search(BASE, FILTER_STRING, searchControls,
                callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertThat(cookie.getCookie()).as("Cookie should not be null yet").isNotNull();
        list = callbackHandler.getList();
        assertThat(list).hasSize(3);
        person = (Person) list.get(0);
        assertThat(person.getFullname()).isEqualTo("Some Person");
        person = (Person) list.get(1);
        assertThat(person.getFullname()).isEqualTo("Some Person2");
        person = (Person) list.get(2);
        assertThat(person.getFullname()).isEqualTo("Some Person3");

        // Prepare for second and last search
        requestControl = new PagedResultsRequestControl(3, cookie);
        tested.search(BASE, FILTER_STRING, searchControls,
                callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertThat(cookie.getCookie()).as("Cookie should be null now").isNull();
        assertThat(list).hasSize(5);
        person = (Person) list.get(3);
        assertThat(person.getFullname()).isEqualTo("Some Person");
        person = (Person) list.get(4);
        assertThat(person.getFullname()).isEqualTo("Some Person");
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
