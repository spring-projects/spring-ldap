/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.repository.query;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.core.impl.BaseUnitTestPerson;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 */
@ContextConfiguration("classpath:/query-test.xml")
public class PartTreeLdapRepositoryQueryTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private LdapTemplate ldapTemplate;
    private Class<?> targetClass;
    private Class<?> entityClass;
    private DefaultRepositoryMetadata repositoryMetadata;
    private ProjectionFactory factory;

    @Before
    public void prepareTest() {
        entityClass = UnitTestPerson.class;
        targetClass = UnitTestPersonRepository.class;
        repositoryMetadata = new DefaultRepositoryMetadata(targetClass);
        factory = new SpelAwareProxyProjectionFactory();
    }

    @Test
    public void testFindByFullName() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullName", String.class),
                "(cn=John Doe)",
                "John Doe");
    }
    
    // LDAP-314
    @Test
    public void testFindByFullNameWithBase() throws NoSuchMethodException {
        entityClass = BaseUnitTestPerson.class;
        targetClass = BaseTestPersonRepository.class;
        repositoryMetadata = new DefaultRepositoryMetadata(targetClass);
        assertFilterAndBaseForMethod(
                targetClass.getMethod("findByFullName", String.class),
                "(cn=John Doe)",
                "ou=someOu",
                "John Doe");
    }

    @Test
    public void testFindByFullNameLike() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameLike", String.class),
                "(cn=*John*)",
                "*John*");
    }

    @Test
    public void testFindByFullNameStartsWith() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameStartsWith", String.class),
                "(cn=John*)",
                "John");
    }

    @Test
    public void testFindByFullNameEndsWith() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameEndsWith", String.class),
                "(cn=*John)",
                "John");
    }

    @Test
    public void testFindByFullNameContains() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameContains", String.class),
                "(cn=*John*)",
                "John");
    }

    @Test
    public void testFindByFullNameGreaterThanEqual() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameGreaterThanEqual", String.class),
                "(cn>=John)",
                "John");
    }

    @Test
    public void testFindByFullNameLessThanEqual() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameLessThanEqual", String.class),
                "(cn<=John)",
                "John");
    }

    @Test
    public void testFindByFullNameIsNotNull() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameIsNotNull"),
                "(cn=*)");
    }

    @Test
    public void testFindByFullNameIsNull() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameIsNull"),
                "(!(cn=*))");
    }

    @Test
    public void testFindByFullNameNot() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameNot", String.class),
                "(!(cn=John Doe))",
                "John Doe");
    }

    @Test
    public void testFindByFullNameNotLike() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameNotLike", String.class),
                "(!(cn=*John*))",
                "*John*");
    }

    @Test
    public void testFindByFullNameAndLastName() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameAndLastName", String.class, String.class),
                "(&(cn=John Doe)(sn=Doe))",
                "John Doe", "Doe");
    }

    @Test
    public void testFindByFullNameAndLastNameNot() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullNameAndLastNameNot", String.class, String.class),
                "(&(cn=John Doe)(!(sn=Doe)))",
                "John Doe", "Doe");
    }

    private void assertFilterForMethod(Method targetMethod, String expectedFilter, Object... expectedParams) {
            assertFilterAndBaseForMethod(targetMethod, expectedFilter, "",
                    expectedParams);
    }

    private void assertFilterAndBaseForMethod(Method targetMethod, String expectedFilter, String expectedBase, Object... expectedParams) {
        LdapQueryMethod queryMethod = new LdapQueryMethod(targetMethod, repositoryMetadata, factory);
        PartTreeLdapRepositoryQuery tested = new PartTreeLdapRepositoryQuery(queryMethod, entityClass, ldapTemplate);

        LdapQuery query = tested.createQuery(expectedParams);
        String base = query.base().toString();
        assertThat(base).isEqualTo(expectedBase);
        assertThat(query.filter().encode()).isEqualTo(expectedFilter);
    }
}
