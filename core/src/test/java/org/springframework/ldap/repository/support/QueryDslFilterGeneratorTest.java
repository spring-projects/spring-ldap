package org.springframework.ldap.repository.support;

import com.querydsl.core.types.Expression;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;

import static org.junit.Assert.assertEquals;

/**
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 */
public class QueryDslFilterGeneratorTest {

    private LdapSerializer tested;
    private QPerson person;

    @Before
    public void prepareTestedInstance() {
        ObjectDirectoryMapper odm = new DefaultObjectDirectoryMapper();
        tested = new LdapSerializer(odm, UnitTestPerson.class);
        person = QPerson.person;
    }

    @Test
    public void testEqualsFilter() {
        Expression<?> expression = person.fullName.eq("John Doe");
        Filter result = tested.handle(expression);
        assertEquals("(cn=John Doe)", result.toString());
    }

    @Test
    public void testAndFilter() {
        Expression<?> expression = person.fullName.eq("John Doe").and(person.lastName.eq("Doe"));
        Filter result = tested.handle(expression);
        assertEquals("(&(cn=John Doe)(sn=Doe))", result.toString());
    }

    @Test
    public void testOrFilter() {
        Expression<?> expression = person.fullName.eq("John Doe").or(person.lastName.eq("Doe"));
        Filter result = tested.handle(expression);
        assertEquals("(|(cn=John Doe)(sn=Doe))", result.toString());
    }

    @Test
    public void testOr() {
        Expression<?> expression = person.fullName.eq("John Doe")
                .and(person.lastName.eq("Doe").or(person.lastName.eq("Die")));

        Filter result = tested.handle(expression);
        assertEquals("(&(cn=John Doe)(|(sn=Doe)(sn=Die)))", result.toString());
    }

    @Test
    public void testNot() {
        Expression<?> expression = person.fullName.eq("John Doe").not();
        Filter result = tested.handle(expression);
        assertEquals("(!(cn=John Doe))", result.toString());
    }

    @Test
    public void testIsLike() {
        Expression<?> expression = person.fullName.like("kalle*");
        Filter result = tested.handle(expression);
        assertEquals("(cn=kalle*)", result.toString());
    }

    @Test
    public void testStartsWith() {
        Expression<?> expression = person.fullName.startsWith("kalle");
        Filter result = tested.handle(expression);
        assertEquals("(cn=kalle*)", result.toString());
    }

    @Test
    public void testEndsWith() {
        Expression<?> expression = person.fullName.endsWith("kalle");
        Filter result = tested.handle(expression);
        assertEquals("(cn=*kalle)", result.toString());
    }

    @Test
    public void testContains() {
        Expression<?> expression = person.fullName.contains("kalle");
        Filter result = tested.handle(expression);
        assertEquals("(cn=*kalle*)", result.toString());
    }

    @Test
    public void testNotNull() {
        Expression<?> expression = person.fullName.isNotNull();
        Filter result = tested.handle(expression);
        assertEquals("(cn=*)", result.toString());
    }

    @Test
    public void testNull() {
        Expression<?> expression = person.fullName.isNull();
        Filter result = tested.handle(expression);
        assertEquals("(!(cn=*))", result.toString());
    }
}
