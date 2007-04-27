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
package org.springframework.ldap;

import org.ddsteps.spring.DDStepsSpringTestCase;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;

/**
 * Tests for LdapTemplate's search methods. This test class tests the generic
 * search methods - the ones that take a SearchResultCallbackHandler.
 * 
 * NOTE: The tests in this class expects an LDAP Server to be running with
 * correct data in it. See Readme.TXT under /src/iutest/ for information on how
 * to set up LDAP Server and data.
 * 
 * Use DDSteps in order to get data-driven tests - use the same method over and
 * over with data from Excel. The instance variables base, filter and
 * expectedResults are automatically filled with new data for each row specified
 * in LdapTemplateSearchCountITest.xls. See ddsteps.org for more information.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateSearchCountITest extends DDStepsSpringTestCase {

    private static final boolean DONT_RETURN_OBJECTS = false;

    private String base;

    private String filter;

    private int expectedResults;

    private int searchScope;

    private LdapTemplate tested;

    private LdapServerManager manager;

    public void setManager(LdapServerManager manager) {
        this.manager = manager;
    }

    public void setUpMethod() throws Exception {
        manager.cleanAndSetup("setup_data.ldif");
    }

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateNoBaseSuffixTestContext.xml" };
    }

    public void testSearch_Count() {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.search(base, filter, handler);
        assertEquals(expectedResults, handler.getNoOfRows());
    }

    public void testSearch_Count_DN() {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.search(new DistinguishedName(base), filter, handler);
        assertEquals(expectedResults, handler.getNoOfRows());
    }

    public void testSearch_Count_SearchScope() {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.search(base, filter, searchScope, DONT_RETURN_OBJECTS, handler);
        assertEquals(expectedResults, handler.getNoOfRows());
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setExpectedResults(int expectedResults) {
        this.expectedResults = expectedResults;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setSearchScope(int searchScope) {
        this.searchScope = searchScope;
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
