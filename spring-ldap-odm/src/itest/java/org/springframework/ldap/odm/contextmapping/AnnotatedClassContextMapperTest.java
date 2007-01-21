/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.contextmapping;

import java.util.Date;
import java.util.Map;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;
import org.springframework.ldap.odm.attributetypes.LdapTypeConverter;
import org.springframework.ldap.odm.attributetypes.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;
import org.springframework.ldap.odm.entity.MockEntity;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AnnotatedClassContextMapperTest extends
		AbstractDependencyInjectionSpringContextTests {
	private static final Log LOGGER = LogFactory
			.getLog(AnnotatedClassContextMapperTest.class);

	private LdapTypeConverter typeConverter;

	private ReferencedEntryEditorFactory refEditorFactory;

	public void setRefEditorFactory(
			ReferencedEntryEditorFactory refEditorFactory) {
		this.refEditorFactory = refEditorFactory;
	}

	public void setTypeConverter(LdapTypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "conf/beans.xml" };
	}

	public void testMapsDirectoryAttributesToBeanProperties() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockEntity.class, typeConverter, refEditorFactory);
			ContextMap contextMap = contextMapper.getContextMap();
			Assert.assertEquals("cn", contextMap.attributeNameFor("Name"),
					"Name mapping not as expected");
			Assert.assertEquals("mail", contextMap.attributeNameFor("Mail"),
					"Mail mapping not as expected");
			Assert.assertEquals("addr", contextMap.attributeNameFor("Address"),
					"Address mapping not as expected");
			Assert.assertEquals("userpassword", contextMap
					.attributeNameFor("Password"),
					"Password mapping not as expected");
			Assert.assertEquals("desc", contextMap
					.attributeNameFor("Description"),
					"Description mapping not as expected");
		} catch (ContextMapperException e) {
			LOGGER.debug(e.getMessage());
			Assert.fail(e.getMessage(), e);
		}
	}

	public void testReturnsCorrectNamingAttribute() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockEntity.class, typeConverter, refEditorFactory);
			Assert.assertEquals("cn", contextMapper.getNamingAttribute(),
					"Context mapper naming attribute not as expected");

		} catch (ContextMapperException e) {
			LOGGER.debug(e.getMessage());
			Assert.fail(e.getMessage(), e);
		}
	}

	public void testReturnsCorrectObjectClasses() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockEntity.class, typeConverter, refEditorFactory);
			Assert.assertEquals(new String[] { "top", "person",
					"organizationalPerson", "inetorgperson" }, contextMapper
					.getObjectClasses(),
					"Context mapper naming attribute not as expected");

		} catch (ContextMapperException e) {
			LOGGER.debug(e.getMessage());
			Assert.fail(e.getMessage());
		}
	}

	public void testBuildDn() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockEntity.class, typeConverter, refEditorFactory);
			MockEntity entity = new MockEntity();
			entity.setName("Mr Bean");
			entity.setAddress("Somewhere in england");
			entity.setDescription(new String[] { "The quick brown fox jumped",
					"over the lazy dog" });
			Name dn = contextMapper.buildDn(entity);
			Assert.assertEquals("cn=Mr Bean, ou=people, dc=example, dc=com", dn
					.toString(), "dn not as expected");

		} catch (ContextMapperException e) {
			LOGGER.debug(e.getMessage());
			Assert.fail(e.getMessage());
		}
	}

	public void testMapToContext() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockEntity.class, typeConverter, refEditorFactory);
			MockEntity entity = new MockEntity();
			entity.setName("Mr Bean");
			entity.setMail("bean@bean.com");
			entity.setAddress("Somewhere in england");
			entity.setPassword("fred".getBytes());
			entity.setDescription(new String[] { "The quick brown fox jumped",
					"over the lazy dog" });
			entity.setAcceptEmails(false);
			entity.setResetLogin(new Date(1L));
			entity.setCreator(new DistinguishedName(
					"uid=amAdmin,ou=people,dc=myretsu,dc=com"));
			entity.setFailedLogins(3);
			DirContextAdapter ctxAdapter = new DirContextAdapter();
			contextMapper.mapToContext(entity, ctxAdapter);

			Assert.assertEquals("Mr Bean", ctxAdapter.getStringAttribute("cn"),
					"Name mapping not as expected");
			Assert
					.assertEquals("bean@bean.com", ctxAdapter
							.getStringAttribute("mail"),
							"Mail mapping not as expected");
			Assert.assertEquals("Somewhere in england", ctxAdapter
					.getObjectAttribute("addr"),
					"Address mapping not as expected");
			Assert.assertEquals("fred", new String((byte[]) ctxAdapter
					.getObjectAttribute("userpassword")),
					"Password mapping not as expected");
			Assert.assertEquals("The quick brown fox jumped", ctxAdapter
					.getStringAttributes("desc")[0],
					"Description mapping not as expected");
			Assert.assertEquals("false", ctxAdapter
					.getStringAttribute("acceptemails"),
					"AcceptEmails mapping not as expected");
			Assert.assertEquals("19700101100000.1", ctxAdapter
					.getStringAttribute("loginresettime"),
					"ResetLogin mapping not as expected");
			Assert.assertEquals("3", ctxAdapter
					.getStringAttribute("failedlogins"),
					"FailedLogins mapping not as expected");
			Assert.assertEquals("uid=amAdmin, ou=people, dc=myretsu, dc=com",
					ctxAdapter.getStringAttribute("creatorname"),
					"Creator mapping not as expected");
		} catch (ContextMapperException e) {
			LOGGER.debug(e.getMessage());
			Assert.fail(e.getMessage(), e);
		}
	}

	public void testMapFromContext() throws ContextMapperException {
		AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
				MockEntity.class, typeConverter, refEditorFactory);
		DirContextAdapter ctxAdapter = new DirContextAdapter();
		ctxAdapter.setAttributeValue("cn", "Mr Bean");
		ctxAdapter.setAttributeValue("mail", "bean@bean.com");
		ctxAdapter.setAttributeValue("addr", "Somewhere in england");
		ctxAdapter.setAttributeValue("userpassword", "fred".getBytes());
		ctxAdapter
				.setAttributeValues("desc", new String[] { "elem1", "elem2" });
		ctxAdapter.setAttributeValue("userpassword", "fred".getBytes());
		ctxAdapter.setAttributeValue("acceptemails", "true");
		ctxAdapter.setAttributeValue("loginresettime", "19700101100000.1");
		ctxAdapter.setAttributeValue("failedlogins", "3");
		ctxAdapter.setAttributeValue("creatorname",
				"uid=amAdmin,ou=people,dc=myretsu,dc=com");

		MockEntity entity = (MockEntity) contextMapper
				.mapFromContext(ctxAdapter);
		Assert.assertEquals(entity.getMail(), "bean@bean.com",
				"The mocked entity should match what has been mapped");
		Assert.assertEquals("Mr Bean", entity.getName(),
				"Name mapping not as expected");
		Assert.assertEquals(entity.getMail(), "bean@bean.com",
				"Mail mapping not as expected");
		Assert.assertEquals(entity.getAddress(), "Somewhere in england",
				"Address mapping not as expected");
		Assert.assertEquals("fred", new String(entity.getPassword()),
				"Password mapping not as expected");
		Assert.assertEquals("elem1", entity.getDescription()[0],
				"Description mapping not as expected");
		Assert.assertEquals(Boolean.TRUE, entity.getAcceptEmails(),
				"AcceptEmails mapping not as expected");
		Assert.assertEquals(new Date(1L), entity.getResetLogin(),
				"ResetLogin mapping not as expected");
		Assert.assertEquals(new Integer(3), entity.getFailedLogins(),
				"FailedLogin mapping not as expected");
		Assert.assertEquals(new DistinguishedName(
				"uid=amAdmin,ou=people,dc=myretsu,dc=com"),
				entity.getCreator(), "Creator mapping not as expected");

	}

	@Test(groups = { "AnnotatedClassContextMapper" }, expectedExceptions = ContextMapperException.class)
	public void throwsExceptionWhenNamingSuffixHasSyntaxError()
			throws ContextMapperException {
		AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
				MockModelNamingSuffixSyntaxError.class, typeConverter,
				refEditorFactory);
		Assert
				.fail("AnnotatedClassContextMapper should throw an exception when naming path annotation syntax is "
						+ "incorrect.");
	}

	@Test(groups = { "AnnotatedClassContextMapper" }, expectedExceptions = ContextMapperException.class)
	public void throwsExceptionWhenNamingSuffixNotInSeparateElements()
			throws ContextMapperException {
		AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
				MockModelNamingSuffixNotInSeparateElements.class,
				typeConverter, refEditorFactory);
		Assert
				.fail("AnnotatedClassContextMapper should throw an exception when naming path annotation syntax is "
						+ "incorrect.");
	}

	@Test(groups = { "AnnotatedClassContextMapper" }, expectedExceptions = ContextMapperException.class)
	public void throwsExceptionWhenModelObjectMissingGetterOrSetter()
			throws ContextMapperException {
		AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
				MockModelMissingSetter.class, typeConverter, refEditorFactory);
		Assert.fail("Should've thrown exception due to missing getter/setter");
	}

	@Test(groups = { "AnnotatedClassContextMapper" })
	public void throwsExceptionWhenBeanPropertyNotAValidTranslationType() {
		try {
			AnnotatedClassContextMapper contextMapper = new AnnotatedClassContextMapper(
					MockModelInvalidTranslationType.class, typeConverter,
					refEditorFactory);
			Assert
					.fail("Should've thrown exception due to missing getter/setter");
		} catch (ContextMapperException e) {
			Assert
					.assertTrue(e
							.getMessage()
							.contains(
									"MockModelInvalidTranslationType.getMap() has invalid return type"));
		}

	}

	@NamingAttribute("cn")
	@NamingSuffix( { "ou=people", "dc=example", "dc=com" })
	@ObjectClasses( { "top", "person", "organizationalPerson", "inetorgperson" })
	public class MockModelMissingSetter {
		@DirAttribute("cn")
		private String name;

		@DirAttribute("addr")
		private String address;

		@DirAttribute("mail")
		private String mail;

		public String getName() {
			return name;
		}

		public String getAddress() {
			return address;
		}

		public String getMail() {
			return mail;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@NamingAttribute("cn")
	@NamingSuffix( { "ou=people", "dc=example", "dc=com" })
	@ObjectClasses( { "top", "person", "organizationalPerson", "inetorgperson" })
	public class MockModelBadMethodName {

		@DirAttribute("cn")
		private String name;

		@DirAttribute("addr")
		private String address;

		public String zzzName() {
			return name;
		}

		public String getAddress() {
			return address;
		}
	}

	@NamingAttribute("cn")
	@NamingSuffix( { "ou=people,dc=example ,dc=com" })
	@ObjectClasses( { "top", "person", "organizationalPerson", "inetorgperson" })
	public class MockModelNamingSuffixNotInSeparateElements {
	}

	@NamingAttribute("cn")
	@NamingSuffix( { "ou=people", "dc<example", "dc=com" })
	@ObjectClasses( { "top", "person", "organizationalPerson", "inetorgperson" })
	public class MockModelNamingSuffixSyntaxError {

	}

	@NamingAttribute("cn")
	@NamingSuffix( { "ou=people", "dc=example", "dc=com" })
	@ObjectClasses( { "top", "person", "organizationalPerson", "inetorgperson" })
	public class MockModelInvalidTranslationType {
		@DirAttribute("fred")
		private Map map;

		public Map getMap() {
			return map;
		}

		public void setMap(Map map) {
			this.map = map;
		}

	}

}
