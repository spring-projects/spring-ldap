/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.filter;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.query.LdapQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests that filters only accept attribute names that are valid RFC 4512 attribute
 * descriptions, so that an attribute name cannot alter the structure of a filter.
 *
 * @author Josh Cummings
 */
public class AttributeNameValidationTests {

	private static final List<String> VALID = List.of("cn", "objectClass", "uid", "x-custom-attr1", "cn;lang-en",
			"cn;lang-en;binary", "2.5.4.3", "2.5.4.3;binary", "0.9.2342.19200300.100.1.1");

	private static final List<String> INVALID = List.of("", " ", "cn=*)(uid", "cn)(objectClass=*", "cn=*", "cn(", "cn)",
			"cn\\", "cn*", "cn~", "cn>", "cn<", "cn:dn", "cn ", " cn", "cn\n", "cn\u0000", "1cn", "-cn", "c_n", "cn;",
			"cn;;binary", "cn;lang_en", "2.5.4.", ".2.5.4.3", "2..5.4.3", "02.5.4.3", "2.05.4.3", "2.5.4.a", "c\u00f1");

	@Test
	void filtersAcceptValidAttributeDescriptions() {
		VALID.forEach(this::assertFiltersAccept);
	}

	@Test
	void filtersRejectInvalidAttributeDescriptions() {
		INVALID.forEach(this::assertFiltersReject);
	}

	private void assertFiltersAccept(String attribute) {
		assertThat(new EqualsFilter(attribute, "v").encode()).isEqualTo("(" + attribute + "=v)");
		assertThat(new LikeFilter(attribute, "v").encode()).isEqualTo("(" + attribute + "=v)");
		assertThat(new WhitespaceWildcardsFilter(attribute, "v").encode()).isEqualTo("(" + attribute + "=*v*)");
		assertThat(new GreaterThanOrEqualsFilter(attribute, "v").encode()).isEqualTo("(" + attribute + ">=v)");
		assertThat(new LessThanOrEqualsFilter(attribute, "v").encode()).isEqualTo("(" + attribute + "<=v)");
		assertThat(new ProximityFilter(attribute, "v").encode()).isEqualTo("(" + attribute + "~=v)");
		assertThat(new PresentFilter(attribute).encode()).isEqualTo("(" + attribute + "=*)");
		assertThat(new NotPresentFilter(attribute).encode()).isEqualTo("(!(" + attribute + "=*))");
	}

	private void assertFiltersReject(String attribute) {
		assertRejected(() -> new EqualsFilter(attribute, "v"));
		assertRejected(() -> new LikeFilter(attribute, "v"));
		assertRejected(() -> new WhitespaceWildcardsFilter(attribute, "v"));
		assertRejected(() -> new GreaterThanOrEqualsFilter(attribute, "v"));
		assertRejected(() -> new LessThanOrEqualsFilter(attribute, "v"));
		assertRejected(() -> new ProximityFilter(attribute, "v"));
		assertRejected(() -> new PresentFilter(attribute));
		assertRejected(() -> new NotPresentFilter(attribute));
	}

	@Test
	void filtersRejectNullAttribute() {
		assertRejected(() -> new EqualsFilter(null, "v"));
		assertRejected(() -> new PresentFilter(null));
		assertRejected(() -> new NotPresentFilter(null));
	}

	@Test
	void queryBuilderRejectsAttributeThatWouldInjectFilterClauses() {
		String attribute = "cn=*)(userPassword";
		assertRejected(() -> LdapQueryBuilder.query().where(attribute).is("v"));
		assertRejected(() -> LdapQueryBuilder.query().where(attribute).isPresent());
		assertRejected(() -> LdapQueryBuilder.query().where("cn").is("v").and(attribute).is("v"));
		assertRejected(() -> LdapQueryBuilder.query().where("cn").is("v").or(attribute).isPresent());
		assertRejected(() -> LdapQueryBuilder.query().where("cn").is("v").and(attribute).not().is("v"));
	}

	private void assertRejected(Runnable creation) {
		assertThatIllegalArgumentException().isThrownBy(creation::run);
	}

}
