package org.springframework.ldap.query;

import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapQueryBuilderTest {

	@Test
	public void buildSimpleWithDefaults() {
		LdapQuery result = query().where("cn").is("John Doe");

		assertThat(result.base()).isEqualTo(LdapUtils.emptyLdapName());
		assertThat(result.searchScope()).isNull();
		assertThat(result.timeLimit()).isNull();
		assertThat(result.countLimit()).isNull();
		assertThat(result.filter().encode()).isEqualTo("(cn=John Doe)");
	}

	@Test
	public void buildGreaterThanOrEquals() {
		LdapQuery result = query().where("cn").gte("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn>=John Doe)");
	}

	@Test
	public void buildLessThanOrEquals() {
		LdapQuery result = query().where("cn").lte("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn<=John Doe)");
	}

	@Test
	public void buildLike() {
		LdapQuery result = query().where("cn").like("J*hn Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn=J*hn Doe)");
	}

	@Test
	public void buildWhitespaceWildcards() {
		LdapQuery result = query().where("cn").whitespaceWildcardsLike("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn=*John*Doe*)");
	}

	@Test
	public void buildPresent() {
		LdapQuery result = query().where("cn").isPresent();

		assertThat(result.filter().encode()).isEqualTo("(cn=*)");
	}

	@Test
	public void buildHardcodedFilter() {
		LdapQuery result = query().filter("(cn=Person*)");

		assertThat(result.filter().encode()).isEqualTo("(cn=Person*)");
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

		assertThat(result.filter().encode()).isEqualTo("(|(cn=Person\\2a)(cn=Parson\\2a))");
	}

	@Test
	public void testBuildSimpleAnd() {
		LdapQuery query = query().base("dc=261consulting, dc=com").searchScope(SearchScope.ONELEVEL).timeLimit(200)
				.countLimit(221).where("objectclass").is("person").and("cn").is("John Doe");

		assertThat(query.base()).isEqualTo(LdapUtils.newLdapName("dc=261consulting, dc=com"));
		assertThat(query.searchScope()).isEqualTo(SearchScope.ONELEVEL);
		assertThat(query.timeLimit()).isEqualTo(Integer.valueOf(200));
		assertThat(query.countLimit()).isEqualTo(Integer.valueOf(221));
		assertThat(query.filter().encode()).isEqualTo("(&(objectclass=person)(cn=John Doe))");
	}

	@Test
	public void buildSimpleOr() {
		LdapQuery result = query().where("objectclass").is("person").or("cn").is("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(|(objectclass=person)(cn=John Doe))");
	}

	@Test
	public void buildAndOrPrecedence() {
		LdapQuery result = query().where("objectclass").is("person").and("cn").is("John Doe")
				.or(query().where("sn").is("Doe"));

		assertThat(result.filter().encode()).isEqualTo("(|(&(objectclass=person)(cn=John Doe))(sn=Doe))");
	}

	@Test
	public void buildOrNegatedSubQueries() {
		LdapQuery result = query().where("objectclass").not().is("person").or("sn").not().is("Doe");
		assertThat(result.filter().encode()).isEqualTo("(|(!(objectclass=person))(!(sn=Doe)))");
	}

	@Test
	public void buildNestedAnd() {
		LdapQuery result = query().where("objectclass").is("person")
				.and(query().where("sn").is("Doe").or("sn").like("Die"));
		assertThat(result.filter().encode()).isEqualTo("(&(objectclass=person)(|(sn=Doe)(sn=Die)))");
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
