package org.springframework.ldap.support;

import java.util.LinkedList;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.NoSuchAttributeException;

public class LdapUtilsTest extends TestCase {

	private MockControl handlerControl;

	private AttributeValueCallbackHandler handlerMock;

	protected void setUp() throws Exception {
		super.setUp();

		handlerControl = MockControl.createControl(AttributeValueCallbackHandler.class);
		handlerMock = (AttributeValueCallbackHandler) handlerControl.getMock();
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		handlerControl = null;
		handlerMock = null;
	}

	public void testCollectAttributeValues() {
		String expectedAttributeName = "someAttribute";
		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);
		expectedAttribute.add("value1");
		expectedAttribute.add("value2");

		BasicAttributes attributes = new BasicAttributes();
		attributes.put(expectedAttribute);

		LinkedList list = new LinkedList();
		LdapUtils.collectAttributeValues(attributes, expectedAttributeName, list);

		assertEquals(2, list.size());
		assertEquals("value1", list.get(0));
		assertEquals("value2", list.get(1));
	}

	public void testCollectAttributeValuesThrowsExceptionWhenAttributeNotPresent() {
		String expectedAttributeName = "someAttribute";
		BasicAttributes attributes = new BasicAttributes();

		LinkedList list = new LinkedList();
		try {
			LdapUtils.collectAttributeValues(attributes, expectedAttributeName, list);
			fail("NoSuchAttributeException expected");
		}
		catch (NoSuchAttributeException expected) {
			assertTrue(true);
		}
	}

	public void testIterateAttributeValues() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);
		expectedAttribute.add("value1");
		expectedAttribute.add("value2");

		handlerMock.handleAttributeValue(expectedAttributeName, "value1", 0);
		handlerMock.handleAttributeValue(expectedAttributeName, "value2", 1);

		handlerControl.replay();

		LdapUtils.iterateAttributeValues(expectedAttribute, handlerMock);

		handlerControl.verify();
	}

	public void testIterateAttributeValuesWithEmptyAttribute() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);

		handlerControl.replay();

		LdapUtils.iterateAttributeValues(expectedAttribute, handlerMock);

		handlerControl.verify();
	}
}
