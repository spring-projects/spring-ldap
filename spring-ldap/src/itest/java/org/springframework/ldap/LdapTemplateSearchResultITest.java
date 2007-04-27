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

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Tests for LdapTemplate's search methods. This test class tests all the
 * different versions of the search methods except the generic ones covered in
 * other tests.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateSearchResultITest extends
        AbstractLdapTemplateIntegrationTest {

    private LdapTemplate tested;

    private AttributeCheckAttributesMapper attributesMapper;

    private AttributeCheckContextMapper contextMapper;

    private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description",
            "telephoneNumber" };

    private static final String[] CN_SN_ATTRS = { "cn", "sn" };

    private static final String[] ABSENT_ATTRIBUTES = { "description",
            "telephoneNumber" };

    private static final String[] CN_SN_VALUES = { "Some Person2", "Person2" };

    private static final String[] ALL_VALUES = { "Some Person2", "Person2",
            "Sweden, Company1, Some Person2", "+46 555-654321" };

    private static final String BASE_STRING = "";

    private static final String FILTER_STRING = "(&(objectclass=person)(sn=Person2))";

    private static final Name BASE_NAME = new DistinguishedName(BASE_STRING);

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();

        attributesMapper = new AttributeCheckAttributesMapper();
        contextMapper = new AttributeCheckContextMapper();
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();

        attributesMapper = null;
        contextMapper = null;
    }

    public void testSearch_AttributesMapper() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_STRING, FILTER_STRING, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_AttributesMapper() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_STRING, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_LimitedAttrs_AttributesMapper() {
        attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
        attributesMapper.setExpectedValues(CN_SN_VALUES);
        attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
        List list = tested.search(BASE_STRING, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_AttributesMapper_Name() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_NAME, FILTER_STRING, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_AttributesMapper_Name() {
        attributesMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        attributesMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_NAME, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_LimitedAttrs_AttributesMapper_Name() {
        attributesMapper.setExpectedAttributes(CN_SN_ATTRS);
        attributesMapper.setExpectedValues(CN_SN_VALUES);
        attributesMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
        List list = tested.search(BASE_NAME, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, attributesMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_ContextMapper() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_STRING, FILTER_STRING, contextMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_ContextMapper() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_STRING, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, contextMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_LimitedAttrs_ContextMapper() {
        contextMapper.setExpectedAttributes(CN_SN_ATTRS);
        contextMapper.setExpectedValues(CN_SN_VALUES);
        contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
        List list = tested.search(BASE_STRING, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, contextMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_ContextMapper_Name() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_NAME, FILTER_STRING, contextMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_ContextMapper_Name() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List list = tested.search(BASE_NAME, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, contextMapper);
        assertEquals(1, list.size());
    }

    public void testSearch_SearchScope_LimitedAttrs_ContextMapper_Name() {
        contextMapper.setExpectedAttributes(CN_SN_ATTRS);
        contextMapper.setExpectedValues(CN_SN_VALUES);
        contextMapper.setAbsentAttributes(ABSENT_ATTRIBUTES);
        List list = tested.search(BASE_NAME, FILTER_STRING,
                SearchControls.SUBTREE_SCOPE, CN_SN_ATTRS, contextMapper);
        assertEquals(1, list.size());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
