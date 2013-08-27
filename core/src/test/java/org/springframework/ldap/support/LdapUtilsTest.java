/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.support;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.NoSuchAttributeException;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LdapUtilsTest {

	private AttributeValueCallbackHandler handlerMock;

    @Before
	public void setUp() throws Exception {
		handlerMock = mock(AttributeValueCallbackHandler.class);
	}

    @Test
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

    @Test
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

    @Test
	public void testIterateAttributeValues() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);
		expectedAttribute.add("value1");
		expectedAttribute.add("value2");

		LdapUtils.iterateAttributeValues(expectedAttribute, handlerMock);

        verify(handlerMock).handleAttributeValue(expectedAttributeName, "value1", 0);
		verify(handlerMock).handleAttributeValue(expectedAttributeName, "value2", 1);
	}

    @Test
	public void testIterateAttributeValuesWithEmptyAttribute() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);

		LdapUtils.iterateAttributeValues(expectedAttribute, handlerMock);
	}
	
	/**
	 * Example SID from "http://www.pcreview.co.uk/forums/thread-1458615.php".
	 */
    @Test
	public void testConvertBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
				(byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0xe9, (byte) 0x67, (byte) 0xbb, (byte) 0x98,
				(byte) 0xd6, (byte) 0xb7, (byte) 0xd7, (byte) 0xbf,
				(byte) 0x82, (byte) 0x05, (byte) 0x1e, (byte) 0x6c,
				(byte) 0x28, (byte) 0x06, (byte) 0x00, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertEquals("S-1-5-21-2562418665-3218585558-1813906818-1576", result);
	}
	
	/**
	 * Example SID from "http://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx".
	 */
    @Test
	public void testConvertAnotherBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
				(byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0xa0, (byte) 0x65, (byte) 0xcf, (byte) 0x7e,
				(byte) 0x78, (byte) 0x4b, (byte) 0x9b, (byte) 0x5f,
				(byte) 0xe7, (byte) 0x7c, (byte) 0x87, (byte) 0x70,
				(byte) 0x09, (byte) 0x1c, (byte) 0x01, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertEquals("S-1-5-21-2127521184-1604012920-1887927527-72713", result);
	}
	
	/**
	 * Hand-crafted SID.
	 */
    @Test
	public void testConvertHandCraftedBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
				(byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertEquals("S-1-5-21-1-2-3-4", result);
	}

    @Test
	public void testSmallNumberToBytesBigEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("5", 6, true);
		assertEquals(6, result.length);
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
		assertEquals(0, result[4]);
		assertEquals(5, result[5]);
	}

    @Test
	public void testLargeNumberToBytesBigEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("1183728", 6, true);
		assertEquals(6, result.length);
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(18, result[3]);
		assertEquals(15, result[4]);
		assertEquals(-16, result[5]);
	}

    @Test
	public void testSmallNumberToBytesLittleEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("21", 4, false);
		assertEquals(4, result.length);
		assertEquals(21, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
	}

    @Test
	public void testLargeNumberToBytesLittleEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("2127521184", 4, false);
		assertEquals(4, result.length);
		assertEquals(-96, result[0]);
		assertEquals(101, result[1]);
		assertEquals(-49, result[2]);
		assertEquals(126, result[3]);
	}
	
	/**
	 * Hand-crafted SID.
	 */
    @Test
	public void testConvertHandCraftedStringSidToBinary() throws Exception {
		byte[] expectedSid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
				(byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] result = LdapUtils.convertStringSidToBinary("S-1-5-21-1-2-3-4");
		assertTrue("incorrect length of array", ArrayUtils.isSameLength(expectedSid, result));
		for (int i = 0; i < result.length; i++) {
			assertEquals("i=" + i + ",", expectedSid[i], result[i]);
		}
	}

	/**
	 * Example SID from "http://www.pcreview.co.uk/forums/thread-1458615.php".
	 */
    @Test
	public void testConvertStringSidToBinary() throws Exception {
		byte[] expectedSid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
				(byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0xe9, (byte) 0x67, (byte) 0xbb, (byte) 0x98,
				(byte) 0xd6, (byte) 0xb7, (byte) 0xd7, (byte) 0xbf,
				(byte) 0x82, (byte) 0x05, (byte) 0x1e, (byte) 0x6c,
				(byte) 0x28, (byte) 0x06, (byte) 0x00, (byte) 0x00 };
		byte[] result = LdapUtils.convertStringSidToBinary("S-1-5-21-2562418665-3218585558-1813906818-1576");
		assertTrue("incorrect length of array", ArrayUtils.isSameLength(expectedSid, result));
		for (int i = 0; i < result.length; i++) {
			assertEquals("i=" + i + ",", expectedSid[i], result[i]);
		}
	}
}
