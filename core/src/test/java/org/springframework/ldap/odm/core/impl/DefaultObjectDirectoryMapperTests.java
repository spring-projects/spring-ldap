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

package org.springframework.ldap.odm.core.impl;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.naming.Name;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.core.SpringVersion;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Mattias Hellborg Arthursson
 */
@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class DefaultObjectDirectoryMapperTests {

	private DefaultObjectDirectoryMapper tested;

	@BeforeEach
	public void prepareTestedInstance() {
		this.tested = new DefaultObjectDirectoryMapper();
	}

	// LDAP-295
	@Test
	public void springVersionIsNull() {
		try (MockedStatic<SpringVersion> version = Mockito.mockStatic(SpringVersion.class)) {
			version.when(SpringVersion::getVersion).thenReturn(null);
			DefaultObjectDirectoryMapper mapper = new DefaultObjectDirectoryMapper();
			// LDAP-300
			assertThat((Object) getInternalState(mapper, "converterManager")).isNotNull();
		}
	}

	@Test
	public void testMapping() {
		assertThat(this.tested.manageClass(UnitTestPerson.class)).containsOnlyElementsOf(
				Arrays.asList("dn", "cn", "sn", "description", "telephoneNumber", "entryUUID", "objectclass"));

		DefaultObjectDirectoryMapper.EntityData entityData = this.tested.getMetaDataMap().get(UnitTestPerson.class);

		assertThat(entityData).isNotNull();
		assertThat(entityData.ocFilter).isEqualTo(LdapQueryBuilder.query()
			.where("objectclass")
			.is("inetOrgPerson")
			.and("objectclass")
			.is("organizationalPerson")
			.and("objectclass")
			.is("person")
			.and("objectclass")
			.is("top")
			.filter());

		assertThat(entityData.metaData).hasSize(8);

		AttributeMetaData idAttribute = entityData.metaData.getIdAttribute();
		assertThat(idAttribute.getField().getName()).isEqualTo("dn");
		assertThat(idAttribute.isId()).isTrue();
		assertThat(idAttribute.isBinary()).isFalse();
		assertThat(idAttribute.isDnAttribute()).isFalse();
		assertThat(idAttribute.isTransient()).isFalse();
		assertThat(idAttribute.isCollection()).isFalse();

		assertField(entityData, "fullName", "cn", "cn", false, false, false, false);
		assertField(entityData, "lastName", "sn", null, false, false, false, false);
		assertField(entityData, "description", "description", null, false, false, true, false);
		assertField(entityData, "country", null, "c", false, true, false, false);
		assertField(entityData, "company", null, "ou", false, true, false, false);
		assertField(entityData, "telephoneNumber", "telephoneNumber", null, false, false, false, false);
		assertField(entityData, "entryUUID", "entryUUID", null, false, false, false, true);
	}

	@Test
	public void testInvalidType() {
		try {
			this.tested.manageClass(UnitTestPersonWithInvalidFieldType.class);
		}
		catch (InvalidEntryException expected) {
			assertThat(expected.getMessage()).contains("Missing converter from");
		}
	}

	@Test
	public void testIndexedDnAttributes() {
		this.tested.manageClass(UnitTestPersonWithIndexedDnAttributes.class);

		UnitTestPersonWithIndexedDnAttributes testPerson = new UnitTestPersonWithIndexedDnAttributes();
		testPerson.setFullName("Some Person");
		testPerson.setCompany("Some Company");
		testPerson.setCountry("Sweden");

		Name calculatedId = this.tested.getCalculatedId(testPerson);
		assertThat(calculatedId).isEqualTo(LdapUtils.newLdapName("cn=Some Person, ou=Some Company, c=Sweden"));
	}

	@Test
	public void testIndexedDnAttributesRequiresThatAllAreIndexed() {
		assertThatExceptionOfType(MetaDataException.class)
			.isThrownBy(() -> this.tested.manageClass(UnitTestPersonWithIndexedAndUnindexedDnAttributes.class));
	}

	@Test
	public void mapToLdapDataEntryWhenCustomConversionServiceThenUses() {
		this.tested.manageClass(UnitTestPersonWithIndexedDnAttributes.class);
		UnitTestPersonWithIndexedDnAttributes testPerson = new UnitTestPersonWithIndexedDnAttributes();
		testPerson.setFullName("Some Person");
		ConversionService conversionService = spy(new GenericConversionService());
		this.tested.setConversionService(conversionService);
		this.tested.mapToLdapDataEntry(testPerson, new DirContextAdapter("cn=Some Person, ou=Some Company, c=Sweden"));
		verify(conversionService).convert(any(), any(Class.class));
	}

	// gh-1101
	@Test
	public void managerWhenEntityMapsLongThenConverts() {
		this.tested.manageClass(UnitTestPersonWithIndexedDnAttributes.class);
		UnitTestPersonWithIndexedDnAttributes testPerson = new UnitTestPersonWithIndexedDnAttributes();
		testPerson.setFullName("Some Person");
		testPerson.setAge(34L);
		DirContextAdapter adapter = new DirContextAdapter("cn=Some Person, ou=Some Company, c=Sweden");
		this.tested.mapToLdapDataEntry(testPerson, adapter);
		assertThat(adapter.getStringAttribute("age")).isEqualTo("34");
		testPerson = this.tested.mapFromLdapDataEntry(adapter, UnitTestPersonWithIndexedDnAttributes.class);
		assertThat(testPerson.getAge()).isEqualTo(34L);
	}

	private void assertField(DefaultObjectDirectoryMapper.EntityData entityData, String fieldName,
			String expectedAttributeName, String expectedDnAttributeName, boolean expectedBinary,
			boolean expectedTransient, boolean expectedList, boolean expectedReadOnly) {

		for (Field field : entityData.metaData) {
			if (fieldName.equals(field.getName())) {
				AttributeMetaData attribute = entityData.metaData.getAttribute(field);
				if (StringUtils.hasLength(expectedAttributeName)) {
					assertThat(attribute.getName().toString()).isEqualTo(expectedAttributeName);
				}
				else {
					assertThat(attribute.name).isNull();
				}

				if (StringUtils.hasLength(expectedDnAttributeName)) {
					assertThat(attribute.isDnAttribute()).isTrue();
					assertThat(attribute.getDnAttribute().value()).isEqualTo(expectedDnAttributeName);
				}
				else {
					assertThat(attribute.isDnAttribute()).isFalse();
				}

				assertThat(attribute.isBinary()).isEqualTo(expectedBinary);
				assertThat(attribute.isTransient()).isEqualTo(expectedTransient);
				assertThat(attribute.isCollection()).isEqualTo(expectedList);
				assertThat(attribute.isReadOnly()).isEqualTo(expectedReadOnly);
			}
		}
	}

	private <T> T getInternalState(Object target, String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		field.setAccessible(true);
		return (T) ReflectionUtils.getField(field, target);
	}

}
