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
