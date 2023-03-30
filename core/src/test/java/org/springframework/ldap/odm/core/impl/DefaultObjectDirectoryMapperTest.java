package org.springframework.ldap.odm.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.naming.Name;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.SpringVersion;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Mattias Hellborg Arthursson
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultObjectDirectoryMapperTest {

	private DefaultObjectDirectoryMapper tested;

	@Before
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
		assertThat(entityData.ocFilter).isEqualTo(query().where("objectclass").is("inetOrgPerson").and("objectclass")
				.is("organizationalPerson").and("objectclass").is("person").and("objectclass").is("top").filter());

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

	@Test(expected = MetaDataException.class)
	public void testIndexedDnAttributesRequiresThatAllAreIndexed() {
		this.tested.manageClass(UnitTestPersonWithIndexedAndUnindexedDnAttributes.class);
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
					assertThat(attribute.getName()).isNull();
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
