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
