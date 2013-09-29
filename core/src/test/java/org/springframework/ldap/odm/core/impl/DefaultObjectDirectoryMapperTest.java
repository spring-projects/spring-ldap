package org.springframework.ldap.odm.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
public class DefaultObjectDirectoryMapperTest {

    private DefaultObjectDirectoryMapper tested;

    @Before
    public void prepareTestedInstance() {
        tested = new DefaultObjectDirectoryMapper();
    }

    @Test
    public void testMapping() {
        tested.manageClass(UnitTestPerson.class);

        DefaultObjectDirectoryMapper.EntityData entityData = tested.getMetaDataMap().get(UnitTestPerson.class);

        assertNotNull(entityData);
        assertEquals(query().
                where("objectclass").is("inetOrgPerson")
                .and("objectclass").is("organizationalPerson")
                .and("objectclass").is("person")
                .and("objectclass").is("top")
                .filter(), entityData.ocFilter);

        assertEquals(7, entityData.metaData.size());

        AttributeMetaData idAttribute = entityData.metaData.getIdAttribute();
        assertEquals("dn", idAttribute.getField().getName());
        assertTrue(idAttribute.isId());
        assertFalse(idAttribute.isBinary());
        assertFalse(idAttribute.isDnAttribute());
        assertFalse(idAttribute.isTransient());
        assertFalse(idAttribute.isList());

        assertField(entityData, "fullName", "cn", "cn", false, false, false);
        assertField(entityData, "lastName", "sn", null, false, false, false);
        assertField(entityData, "description", "description", null, false, false, true);
        assertField(entityData, "country", null, "c", false, true, false);
        assertField(entityData, "company", null, "ou", false, true, false);
        assertField(entityData, "telephoneNumber", "telephoneNumber", null, false, false, false);
    }

    @Test
    public void testInvalidType() {
        try {
            tested.manageClass(UnitTestPersonWithInvalidFieldType.class);
        } catch (InvalidEntryException expected) {
            assertThat(expected.getMessage(), JUnitMatchers.containsString("Missing converter from"));
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
        assertEquals(LdapUtils.newLdapName("cn=Some Person, ou=Some Company, c=Sweden"), calculatedId);
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
                             boolean expectedList) {

        for (Field field : entityData.metaData) {
            if (fieldName.equals(field.getName())) {
                AttributeMetaData attribute = entityData.metaData.getAttribute(field);
                if (StringUtils.hasLength(expectedAttributeName)) {
                    assertEquals(expectedAttributeName, attribute.getName().toString());
                } else {
                    assertNull(attribute.getName());
                }

                if (StringUtils.hasLength(expectedDnAttributeName)) {
                    assertTrue(attribute.isDnAttribute());
                    assertEquals(expectedDnAttributeName, attribute.getDnAttribute().value());
                } else {
                    assertFalse(attribute.isDnAttribute());
                }

                assertEquals(expectedBinary, attribute.isBinary());
                assertEquals(expectedTransient, attribute.isTransient());
                assertEquals(expectedList, attribute.isList());
            }
        }
    }
}
