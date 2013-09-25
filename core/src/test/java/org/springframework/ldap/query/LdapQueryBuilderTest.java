package org.springframework.ldap.query;

import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapQueryBuilderTest {

    @Test
    public void buildSimpleWithDefaults() {
        LdapQuery result = query().where("cn").is("John Doe");

        assertEquals(LdapUtils.emptyLdapName(), result.base());
        assertNull(result.searchScope());
        assertNull(result.timeLimit());
        assertNull(result.countLimit());
        assertEquals("(cn=John Doe)", result.filter().encode());
    }

    @Test
    public void buildGreaterThanOrEquals() {
        LdapQuery result = query().where("cn").gte("John Doe");

        assertEquals("(cn>=John Doe)", result.filter().encode());
    }

    @Test
    public void buildLessThanOrEquals() {
        LdapQuery result = query().where("cn").lte("John Doe");

        assertEquals("(cn<=John Doe)", result.filter().encode());
    }

    @Test
    public void buildLike() {
        LdapQuery result = query().where("cn").like("J*hn Doe");

        assertEquals("(cn=J*hn Doe)", result.filter().encode());
    }


    @Test
    public void buildWhitespaceWildcards() {
        LdapQuery result = query().where("cn").whitespaceWildcardsLike("John Doe");

        assertEquals("(cn=*John*Doe*)", result.filter().encode());
    }

    @Test
    public void buildPresent() {
        LdapQuery result = query().where("cn").isPresent();

        assertEquals("(cn=*)", result.filter().encode());
    }

    @Test
    public void buildHardcodedFilter() {
        LdapQuery result = query().filter("(cn=Person*)");

        assertEquals("(cn=Person*)", result.filter().encode());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyThatHardcodedFilterFailsIfFilterAlreadySpecified() {
        LdapQueryBuilder query = query();
        query.where("sn").is("Doe");
        query.filter("(cn=Person*)");
    }

    @Test(expected = IllegalStateException.class)
    public void verifyThatFilterFormatFailsIfFilterAlreadySpecified() {
        LdapQueryBuilder query = query();
        query.where("sn").is("Doe");
        query.filter("(|(cn={0})(cn={1}))", "Person*", "Parson*");
    }

    @Test
    public void buildFilterFormat() {
        LdapQuery result = query().filter("(|(cn={0})(cn={1}))", "Person*", "Parson*");

        assertEquals("(|(cn=Person\\2a)(cn=Parson\\2a))", result.filter().encode());
    }

    @Test
    public void testBuildSimpleAnd() {
        LdapQuery query = query()
                .base("dc=261consulting, dc=com")
                .searchScope(SearchScope.ONELEVEL)
                .timeLimit(200)
                .countLimit(221)
                .where("objectclass").is("person").and("cn").is("John Doe");

        assertEquals(LdapUtils.newLdapName("dc=261consulting, dc=com"), query.base());
        assertEquals(SearchScope.ONELEVEL, query.searchScope());
        assertEquals(Integer.valueOf(200), query.timeLimit());
        assertEquals(Integer.valueOf(221), query.countLimit());
        assertEquals("(&(objectclass=person)(cn=John Doe))", query.filter().encode());
    }

    @Test
    public void buildSimpleOr() {
        LdapQuery result = query().where("objectclass").is("person").or("cn").is("John Doe");

        assertEquals("(|(objectclass=person)(cn=John Doe))", result.filter().encode());
    }

    @Test
    public void buildAndOrPrecedence() {
        LdapQuery result = query().where("objectclass").is("person")
                .and("cn").is("John Doe")
                .or(query().where("sn").is("Doe"));

        assertEquals("(|(&(objectclass=person)(cn=John Doe))(sn=Doe))", result.filter().encode());
    }

    @Test
    public void buildOrNegatedSubQueries() {
        LdapQuery result = query().where("objectclass").not().is("person").or("sn").not().is("Doe");
        assertEquals("(|(!(objectclass=person))(!(sn=Doe)))", result.filter().encode());
    }

    @Test
    public void buildNestedAnd() {
        LdapQuery result = query()
                .where("objectclass").is("person")
                .and(query()
                        .where("sn").is("Doe")
                        .or("sn").like("Die"));
        assertEquals("(&(objectclass=person)(|(sn=Doe)(sn=Die)))", result.filter().encode());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyEmptyFilterThrowsIllegalState() {
        query().filter();
    }

    @Test(expected = IllegalStateException.class)
    public void verifyThatNewAttemptToStartSpecifyingFilterThrowsIllegalState() {
        LdapQueryBuilder query = query();
        query.where("sn").is("Doe");
        query.where("cn").is("John Doe");
    }

    @Test(expected = IllegalStateException.class)
    public void verifyThatAttemptToStartSpecifyingBasePropertiesThrowsIllegalStateWhenFilterStarted() {
        LdapQueryBuilder query = query();
        query.where("sn").is("Doe");
        query.base("dc=261consulting,dc=com");
    }

    @Test(expected = IllegalStateException.class)
    public void verifyThatOperatorChangeIsIllegal() {
        query().where("cn").is("John Doe").and("sn").is("Doe").or("objectclass").is("person");
    }
}
