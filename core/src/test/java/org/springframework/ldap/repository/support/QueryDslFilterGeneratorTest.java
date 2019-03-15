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

package org.springframework.ldap.repository.support;

import com.querydsl.core.types.Expression;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(result.toString()).isEqualTo("(cn=John Doe)");
    }

    @Test
    public void testAndFilter() {
        Expression<?> expression = person.fullName.eq("John Doe").and(person.lastName.eq("Doe"));
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(&(cn=John Doe)(sn=Doe))");
    }

    @Test
    public void testOrFilter() {
        Expression<?> expression = person.fullName.eq("John Doe").or(person.lastName.eq("Doe"));
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(|(cn=John Doe)(sn=Doe))");
    }

    @Test
    public void testOr() {
        Expression<?> expression = person.fullName.eq("John Doe")
                .and(person.lastName.eq("Doe").or(person.lastName.eq("Die")));

        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(&(cn=John Doe)(|(sn=Doe)(sn=Die)))");
    }

    @Test
    public void testNot() {
        Expression<?> expression = person.fullName.eq("John Doe").not();
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(!(cn=John Doe))");
    }

    @Test
    public void testIsLike() {
        Expression<?> expression = person.fullName.like("kalle*");
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(cn=kalle*)");
    }

    @Test
    public void testStartsWith() {
        Expression<?> expression = person.fullName.startsWith("kalle");
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(cn=kalle*)");
    }

    @Test
    public void testEndsWith() {
        Expression<?> expression = person.fullName.endsWith("kalle");
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(cn=*kalle)");
    }

    @Test
    public void testContains() {
        Expression<?> expression = person.fullName.contains("kalle");
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(cn=*kalle*)");
    }

    @Test
    public void testNotNull() {
        Expression<?> expression = person.fullName.isNotNull();
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(cn=*)");
    }

    @Test
    public void testNull() {
        Expression<?> expression = person.fullName.isNull();
        Filter result = tested.handle(expression);
        assertThat(result.toString()).isEqualTo("(!(cn=*))");
    }
}
