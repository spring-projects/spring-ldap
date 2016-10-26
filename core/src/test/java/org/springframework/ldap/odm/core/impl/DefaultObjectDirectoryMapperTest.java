package org.springframework.ldap.odm.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.naming.Name;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.SpringVersion;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

/**
 * @author Mattias Hellborg Arthursson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SpringVersion.class)
public class DefaultObjectDirectoryMapperTest {

    private DefaultObjectDirectoryMapper tested;

    @Before
    public void prepareTestedInstance() {
        tested = new DefaultObjectDirectoryMapper();
    }

    // LDAP-295
    @Test
    public void springVersionIsNull() {
        spy(SpringVersion.class);
        when(SpringVersion.getVersion()).thenReturn(null);

        DefaultObjectDirectoryMapper mapper = new DefaultObjectDirectoryMapper();

        // LDAP-300
        assertThat(Whitebox.getInternalState(mapper,"converterManager")).isNotNull();
    }

    @Test
    public void testMapping() {
        assertThat(tested.manageClass(UnitTestPerson.class))
                .containsOnlyElementsOf(Arrays.asList("dn", "cn", "sn", "description", "telephoneNumber", "entryUUID", "objectclass"));

        DefaultObjectDirectoryMapper.EntityData entityData = tested.getMetaDataMap().get(UnitTestPerson.class);

        assertThat(entityData).isNotNull();
        assertThat(entityData.ocFilter).isEqualTo(query().
                where("objectclass").is("inetOrgPerson")
                .and("objectclass").is("organizationalPerson")
                .and("objectclass").is("person")
                .and("objectclass").is("top")
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
            tested.manageClass(UnitTestPersonWithInvalidFieldType.class);
        } catch (InvalidEntryException expected) {
            assertThat(expected.getMessage()).contains("Missing converter from");
        }
    }

    @Test
    public void testIndexedDnAttributes() {
        tested.manageClass(UnitTestPersonWithIndexedDnAttributes.class);

        UnitTestPersonWithIndexedDnAttributes testPerson = new UnitTestPersonWithIndexedDnAttributes();
        testPerson.setFullName("Some Person");
        testPerson.setCompany("Some Company");
        testPerson.setCountry("Sweden");


        Name calculatedId = tested.getCalculatedId(testPerson);
        assertThat(calculatedId).isEqualTo(LdapUtils.newLdapName("cn=Some Person, ou=Some Company, c=Sweden"));
    }

    @Test(expected = MetaDataException.class)
    public void testIndexedDnAttributesRequiresThatAllAreIndexed() {
        tested.manageClass(UnitTestPersonWithIndexedAndUnindexedDnAttributes.class);
    }

    private void assertField(DefaultObjectDirectoryMapper.EntityData entityData,
                             String fieldName,
                             String expectedAttributeName,
                             String expectedDnAttributeName,
                             boolean expectedBinary,
                             boolean expectedTransient,
            boolean expectedList,
            boolean expectedReadOnly) {

        for (Field field : entityData.metaData) {
            if (fieldName.equals(field.getName())) {
                AttributeMetaData attribute = entityData.metaData.getAttribute(field);
                if (StringUtils.hasLength(expectedAttributeName)) {
                    assertThat(attribute.getName().toString()).isEqualTo(expectedAttributeName);
                } else {
                    assertThat(attribute.getName()).isNull();
                }

                if (StringUtils.hasLength(expectedDnAttributeName)) {
                    assertThat(attribute.isDnAttribute()).isTrue();
                    assertThat(attribute.getDnAttribute().value()).isEqualTo(expectedDnAttributeName);
                } else {
                    assertThat(attribute.isDnAttribute()).isFalse();
                }

                assertThat(attribute.isBinary()).isEqualTo(expectedBinary);
                assertThat(attribute.isTransient()).isEqualTo(expectedTransient);
                assertThat(attribute.isCollection()).isEqualTo(expectedList);
                assertThat(attribute.isReadOnly()).isEqualTo(expectedReadOnly);
            }
        }
    }
}
