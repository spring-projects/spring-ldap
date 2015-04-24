package org.springframework.ldap.repository.query;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.core.impl.BaseUnitTestPerson;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration("classpath:/query-test.xml")
public class PartTreeLdapRepositoryQueryTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private LdapTemplate ldapTemplate;
    private Class<?> targetClass;
    private Class<?> entityClass;
    private DefaultRepositoryMetadata repositoryMetadata;

    @Before
    public void prepareTest() {
        entityClass = UnitTestPerson.class;
        targetClass = UnitTestPersonRepository.class;
        repositoryMetadata = new DefaultRepositoryMetadata(targetClass);
    }

    @Test
    public void testFindByFullName() throws NoSuchMethodException {
        assertFilterForMethod(
                targetClass.getMethod("findByFullName", String.class),
                "(cn=John Doe)",
                "John Doe");
    }

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
        LdapQueryMethod queryMethod = new LdapQueryMethod(targetMethod, repositoryMetadata);
        PartTreeLdapRepositoryQuery tested = new PartTreeLdapRepositoryQuery(queryMethod, entityClass, ldapTemplate);

        LdapQuery query = tested.createQuery(expectedParams);
        String base = query.base().toString();
        assertEquals(expectedBase, base);
        assertEquals(expectedFilter, query.filter().encode());
    }
}
