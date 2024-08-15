/*
 * Copyright 2005-2023 the original author or authors.
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

package org.springframework.ldap.query;

import org.junit.Test;

import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapQueryBuilderTests {

	@Test
	public void buildSimpleWithDefaults() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").is("John Doe");

		assertThat(result.base()).isEqualTo(LdapUtils.emptyLdapName());
		assertThat(result.searchScope()).isNull();
		assertThat(result.timeLimit()).isNull();
		assertThat(result.countLimit()).isNull();
		assertThat(result.filter().encode()).isEqualTo("(cn=John Doe)");
	}

	@Test
	public void buildGreaterThanOrEquals() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").gte("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn>=John Doe)");
	}

	@Test
	public void buildLessThanOrEquals() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").lte("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn<=John Doe)");
	}

	@Test
	public void buildLike() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").like("J*hn Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn=J*hn Doe)");
	}

	@Test
	public void buildWhitespaceWildcards() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").whitespaceWildcardsLike("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(cn=*John*Doe*)");
	}

	@Test
	public void buildPresent() {
		LdapQuery result = LdapQueryBuilder.query().where("cn").isPresent();

		assertThat(result.filter().encode()).isEqualTo("(cn=*)");
	}

	@Test
	public void buildHardcodedFilter() {
		LdapQuery result = LdapQueryBuilder.query().filter("(cn=Person*)");

		assertThat(result.filter().encode()).isEqualTo("(cn=Person*)");
	}

	@Test(expected = IllegalStateException.class)
	public void verifyThatHardcodedFilterFailsIfFilterAlreadySpecified() {
		LdapQueryBuilder query = LdapQueryBuilder.query();
		query.where("sn").is("Doe");
		query.filter("(cn=Person*)");
	}

	@Test(expected = IllegalStateException.class)
	public void verifyThatFilterFormatFailsIfFilterAlreadySpecified() {
		LdapQueryBuilder query = LdapQueryBuilder.query();
		query.where("sn").is("Doe");
		query.filter("(|(cn={0})(cn={1}))", "Person*", "Parson*");
	}

	@Test
	public void buildFilterFormat() {
		LdapQuery result = LdapQueryBuilder.query().filter("(|(cn={0})(cn={1}))", "Person*", "Parson*");

		assertThat(result.filter().encode()).isEqualTo("(|(cn=Person\\2a)(cn=Parson\\2a))");
	}

	@Test
	public void testBuildSimpleAnd() {
		LdapQuery query = LdapQueryBuilder.query()
			.base("dc=261consulting, dc=com")
			.searchScope(SearchScope.ONELEVEL)
			.timeLimit(200)
			.countLimit(221)
			.where("objectclass")
			.is("person")
			.and("cn")
			.is("John Doe");

		assertThat(query.base()).isEqualTo(LdapUtils.newLdapName("dc=261consulting, dc=com"));
		assertThat(query.searchScope()).isEqualTo(SearchScope.ONELEVEL);
		assertThat(query.timeLimit()).isEqualTo(Integer.valueOf(200));
		assertThat(query.countLimit()).isEqualTo(Integer.valueOf(221));
		assertThat(query.filter().encode()).isEqualTo("(&(objectclass=person)(cn=John Doe))");
	}

	@Test
	public void buildSimpleOr() {
		LdapQuery result = LdapQueryBuilder.query().where("objectclass").is("person").or("cn").is("John Doe");

		assertThat(result.filter().encode()).isEqualTo("(|(objectclass=person)(cn=John Doe))");
	}

	@Test
	public void buildAndOrPrecedence() {
		LdapQuery result = LdapQueryBuilder.query()
			.where("objectclass")
			.is("person")
			.and("cn")
			.is("John Doe")
			.or(LdapQueryBuilder.query().where("sn").is("Doe"));

		assertThat(result.filter().encode()).isEqualTo("(|(&(objectclass=person)(cn=John Doe))(sn=Doe))");
	}

	@Test
	public void buildOrNegatedSubQueries() {
		LdapQuery result = LdapQueryBuilder.query().where("objectclass").not().is("person").or("sn").not().is("Doe");
		assertThat(result.filter().encode()).isEqualTo("(|(!(objectclass=person))(!(sn=Doe)))");
	}

	@Test
	public void buildNestedAnd() {
		LdapQuery result = LdapQueryBuilder.query()
			.where("objectclass")
			.is("person")
			.and(LdapQueryBuilder.query().where("sn").is("Doe").or("sn").like("Die"));
		assertThat(result.filter().encode()).isEqualTo("(&(objectclass=person)(|(sn=Doe)(sn=Die)))");
	}

	@Test(expected = IllegalStateException.class)
	public void verifyEmptyFilterThrowsIllegalState() {
		LdapQueryBuilder.query().filter();
	}

	@Test(expected = IllegalStateException.class)
	public void verifyThatNewAttemptToStartSpecifyingFilterThrowsIllegalState() {
		LdapQueryBuilder query = LdapQueryBuilder.query();
		query.where("sn").is("Doe");
		query.where("cn").is("John Doe");
	}

	@Test(expected = IllegalStateException.class)
	public void verifyThatAttemptToStartSpecifyingBasePropertiesThrowsIllegalStateWhenFilterStarted() {
		LdapQueryBuilder query = LdapQueryBuilder.query();
		query.where("sn").is("Doe");
		query.base("dc=261consulting,dc=com");
	}

	@Test(expected = IllegalStateException.class)
	public void verifyThatOperatorChangeIsIllegal() {
		LdapQueryBuilder.query().where("cn").is("John Doe").and("sn").is("Doe").or("objectclass").is("person");
	}

}
