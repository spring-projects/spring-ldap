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

package org.springframework.ldap.support;

import java.util.LinkedList;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.NoSuchAttributeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LdapUtilsTests {

	private static final String EXPECTED_DN_STRING = "cn=john.doe, OU=Users,OU=SE,OU=G,OU=I,OU=M";

	private static final String EXPECTED_MULTIVALUE_DN_STRING = "cn=john.doe, OU=Users,OU=SE,OU=G+O=GR,OU=I,OU=M";

	private AttributeValueCallbackHandler handlerMock;

	@Before
	public void setUp() throws Exception {
		this.handlerMock = mock(AttributeValueCallbackHandler.class);
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

		assertThat(list).hasSize(2);
		assertThat(list.get(0)).isEqualTo("value1");
		assertThat(list.get(1)).isEqualTo("value2");
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
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testIterateAttributeValues() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);
		expectedAttribute.add("value1");
		expectedAttribute.add("value2");

		LdapUtils.iterateAttributeValues(expectedAttribute, this.handlerMock);

		verify(this.handlerMock).handleAttributeValue(expectedAttributeName, "value1", 0);
		verify(this.handlerMock).handleAttributeValue(expectedAttributeName, "value2", 1);
	}

	@Test
	public void testIterateAttributeValuesWithEmptyAttribute() {
		String expectedAttributeName = "someAttribute";

		BasicAttribute expectedAttribute = new BasicAttribute(expectedAttributeName);

		LdapUtils.iterateAttributeValues(expectedAttribute, this.handlerMock);
	}

	/**
	 * Example SID from "https://www.pcreview.co.uk/forums/thread-1458615.php".
	 */
	@Test
	public void testConvertBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x05, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe9, (byte) 0x67, (byte) 0xbb,
				(byte) 0x98, (byte) 0xd6, (byte) 0xb7, (byte) 0xd7, (byte) 0xbf, (byte) 0x82, (byte) 0x05, (byte) 0x1e,
				(byte) 0x6c, (byte) 0x28, (byte) 0x06, (byte) 0x00, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertThat(result).isEqualTo("S-1-5-21-2562418665-3218585558-1813906818-1576");
	}

	/**
	 * Example SID from
	 * "https://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx".
	 */
	@Test
	public void testConvertAnotherBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x05, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xa0, (byte) 0x65, (byte) 0xcf,
				(byte) 0x7e, (byte) 0x78, (byte) 0x4b, (byte) 0x9b, (byte) 0x5f, (byte) 0xe7, (byte) 0x7c, (byte) 0x87,
				(byte) 0x70, (byte) 0x09, (byte) 0x1c, (byte) 0x01, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertThat(result).isEqualTo("S-1-5-21-2127521184-1604012920-1887927527-72713");
	}

	/**
	 * Hand-crafted SID.
	 */
	@Test
	public void testConvertHandCraftedBinarySidToString() throws Exception {
		byte[] sid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x05, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		String result = LdapUtils.convertBinarySidToString(sid);
		assertThat(result).isEqualTo("S-1-5-21-1-2-3-4");
	}

	@Test
	public void testSmallNumberToBytesBigEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("5", 6, true);
		assertThat(result.length).isEqualTo(6);
		assertThat(result[0]).isEqualTo((byte) 0);
		assertThat(result[1]).isEqualTo((byte) 0);
		assertThat(result[2]).isEqualTo((byte) 0);
		assertThat(result[3]).isEqualTo((byte) 0);
		assertThat(result[4]).isEqualTo((byte) 0);
		assertThat(result[5]).isEqualTo((byte) 5);
	}

	@Test
	public void testLargeNumberToBytesBigEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("1183728", 6, true);
		assertThat(result.length).isEqualTo(6);
		assertThat(result[0]).isEqualTo((byte) 0);
		assertThat(result[1]).isEqualTo((byte) 0);
		assertThat(result[2]).isEqualTo((byte) 0);
		assertThat(result[3]).isEqualTo((byte) 18);
		assertThat(result[4]).isEqualTo((byte) 15);
		assertThat(result[5]).isEqualTo((byte) -16);
	}

	@Test
	public void testSmallNumberToBytesLittleEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("21", 4, false);
		assertThat(result.length).isEqualTo(4);
		assertThat(result[0]).isEqualTo((byte) 21);
		assertThat(result[1]).isEqualTo((byte) 0);
		assertThat(result[2]).isEqualTo((byte) 0);
		assertThat(result[3]).isEqualTo((byte) 0);
	}

	@Test
	public void testLargeNumberToBytesLittleEndian() throws Exception {
		byte[] result = LdapUtils.numberToBytes("2127521184", 4, false);
		assertThat(result.length).isEqualTo(4);
		assertThat(result[0]).isEqualTo((byte) -96);
		assertThat(result[1]).isEqualTo((byte) 101);
		assertThat(result[2]).isEqualTo((byte) -49);
		assertThat(result[3]).isEqualTo((byte) 126);
	}

	/**
	 * Hand-crafted SID.
	 */
	@Test
	public void testConvertHandCraftedStringSidToBinary() throws Exception {
		byte[] expectedSid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x05, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] result = LdapUtils.convertStringSidToBinary("S-1-5-21-1-2-3-4");
		assertThat(ArrayUtils.isSameLength(expectedSid, result)).isTrue();
		for (int i = 0; i < result.length; i++) {
			assertThat(expectedSid[i]).isEqualTo(result[i]);
		}
	}

	/**
	 * Example SID from "https://www.pcreview.co.uk/forums/thread-1458615.php".
	 */
	@Test
	public void testConvertStringSidToBinary() throws Exception {
		byte[] expectedSid = { (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x05, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe9, (byte) 0x67,
				(byte) 0xbb, (byte) 0x98, (byte) 0xd6, (byte) 0xb7, (byte) 0xd7, (byte) 0xbf, (byte) 0x82, (byte) 0x05,
				(byte) 0x1e, (byte) 0x6c, (byte) 0x28, (byte) 0x06, (byte) 0x00, (byte) 0x00 };
		byte[] result = LdapUtils.convertStringSidToBinary("S-1-5-21-2562418665-3218585558-1813906818-1576");
		assertThat(ArrayUtils.isSameLength(expectedSid, result)).as("incorrect length of array").isTrue();
		for (int i = 0; i < result.length; i++) {
			assertThat(expectedSid[i]).isEqualTo(result[i]);
		}
	}

	@Test
	public void testNewLdapNameFromLdapName() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);

		LdapName result = LdapUtils.newLdapName(ldapName);
		assertThat(result).isEqualTo(ldapName);
	}

	@Test
	public void testNewLdapNameFromCompositeName() throws InvalidNameException {
		LdapName result = LdapUtils.newLdapName(new CompositeName(EXPECTED_DN_STRING));
		assertThat(result).isEqualTo(new LdapName(EXPECTED_DN_STRING));
	}

	@Test
	public void testEmptyLdapName() {
		LdapName ldapName = LdapUtils.emptyLdapName();
		assertThat(ldapName.toString()).isEqualTo("");
	}

	@Test
	public void testRemoveFirst() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		LdapName result = LdapUtils.removeFirst(ldapName, new LdapName("OU=I,OU=M"));

		assertThat(result).isNotSameAs(ldapName);
		assertThat(result).isEqualTo(new LdapName("cn=john.doe, OU=Users,OU=SE,OU=G"));
	}

	@Test
	public void testRemoveFirstNoMatch() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		LdapName result = LdapUtils.removeFirst(ldapName, new LdapName("OU=oooooo,OU=M"));

		assertThat(result).isNotSameAs(ldapName);
		assertThat(result).isEqualTo(ldapName);
	}

	@Test
	public void testRemoveFirstEmptyBase() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		LdapName result = LdapUtils.removeFirst(ldapName, LdapUtils.emptyLdapName());

		assertThat(result).isNotSameAs(ldapName);
		assertThat(result).isEqualTo(ldapName);
	}

	@Test
	public void testGetValueNamed() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		assertThat("john.doe").isEqualTo(LdapUtils.getValue(ldapName, "cn"));
	}

	@Test
	public void testGetValueNamedReturnesFirstFound() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		assertThat("M").isEqualTo(LdapUtils.getValue(ldapName, "ou"));
	}

	@Test
	public void testGetValueNamedWithMultivalue() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_MULTIVALUE_DN_STRING);
		assertThat("GR").isEqualTo(LdapUtils.getValue(ldapName, "o"));
	}

	@Test
	public void testGetValueIndexed() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		assertThat("G").isEqualTo(LdapUtils.getValue(ldapName, 2));
	}

	@Test
	public void testGetStringValueIndexed() throws InvalidNameException {
		LdapName ldapName = new LdapName(EXPECTED_DN_STRING);
		assertThat("I").isEqualTo(LdapUtils.getValue(ldapName, 1));
	}

	@Test
	public void testConvertLdapExceptions() {

		// Test the Exceptions in the javax.naming package
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.AttributeInUseException()).getClass())
			.isEqualTo(org.springframework.ldap.AttributeInUseException.class);
		assertThat(
				LdapUtils.convertLdapException(new javax.naming.directory.AttributeModificationException()).getClass())
			.isEqualTo(org.springframework.ldap.AttributeModificationException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.CannotProceedException()).getClass())
			.isEqualTo(org.springframework.ldap.CannotProceedException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.CommunicationException()).getClass())
			.isEqualTo(org.springframework.ldap.CommunicationException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.ConfigurationException()).getClass())
			.isEqualTo(org.springframework.ldap.ConfigurationException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.ContextNotEmptyException()).getClass())
			.isEqualTo(org.springframework.ldap.ContextNotEmptyException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.InsufficientResourcesException()).getClass())
			.isEqualTo(org.springframework.ldap.InsufficientResourcesException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.InterruptedNamingException()).getClass())
			.isEqualTo(org.springframework.ldap.InterruptedNamingException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.InvalidAttributeIdentifierException())
			.getClass()).isEqualTo(org.springframework.ldap.InvalidAttributeIdentifierException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.InvalidAttributesException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidAttributesException.class);
		assertThat(
				LdapUtils.convertLdapException(new javax.naming.directory.InvalidAttributeValueException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidAttributeValueException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.InvalidNameException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidNameException.class);
		assertThat(
				LdapUtils.convertLdapException(new javax.naming.directory.InvalidSearchControlsException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidSearchControlsException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.InvalidSearchFilterException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidSearchFilterException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.SizeLimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.SizeLimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.TimeLimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.TimeLimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.LimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.LimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.LinkLoopException()).getClass())
			.isEqualTo(org.springframework.ldap.LinkLoopException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.MalformedLinkException()).getClass())
			.isEqualTo(org.springframework.ldap.MalformedLinkException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.LinkException()).getClass())
			.isEqualTo(org.springframework.ldap.LinkException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.NameAlreadyBoundException()).getClass())
			.isEqualTo(org.springframework.ldap.NameAlreadyBoundException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.NameNotFoundException()).getClass())
			.isEqualTo(org.springframework.ldap.NameNotFoundException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.NoPermissionException()).getClass())
			.isEqualTo(org.springframework.ldap.NoPermissionException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.AuthenticationException()).getClass())
			.isEqualTo(org.springframework.ldap.AuthenticationException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.AuthenticationNotSupportedException()).getClass())
			.isEqualTo(org.springframework.ldap.AuthenticationNotSupportedException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.NoInitialContextException()).getClass())
			.isEqualTo(org.springframework.ldap.NoInitialContextException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.NoSuchAttributeException()).getClass())
			.isEqualTo(org.springframework.ldap.NoSuchAttributeException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.NotContextException()).getClass())
			.isEqualTo(org.springframework.ldap.NotContextException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.OperationNotSupportedException()).getClass())
			.isEqualTo(org.springframework.ldap.OperationNotSupportedException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.PartialResultException()).getClass())
			.isEqualTo(org.springframework.ldap.PartialResultException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.directory.SchemaViolationException()).getClass())
			.isEqualTo(org.springframework.ldap.SchemaViolationException.class);
		assertThat(LdapUtils.convertLdapException(new javax.naming.ServiceUnavailableException()).getClass())
			.isEqualTo(org.springframework.ldap.ServiceUnavailableException.class);

		// Test Exceptions that extend javax.naming packaage extensions
		assertThat(LdapUtils.convertLdapException(new MockAttributeInUseException()).getClass())
			.isEqualTo(org.springframework.ldap.AttributeInUseException.class);
		assertThat(LdapUtils.convertLdapException(new MockAttributeModificationException()).getClass())
			.isEqualTo(org.springframework.ldap.AttributeModificationException.class);
		assertThat(LdapUtils.convertLdapException(new MockCannotProceedException()).getClass())
			.isEqualTo(org.springframework.ldap.CannotProceedException.class);
		assertThat(LdapUtils.convertLdapException(new MockCommunicationException()).getClass())
			.isEqualTo(org.springframework.ldap.CommunicationException.class);
		assertThat(LdapUtils.convertLdapException(new MockConfigurationException()).getClass())
			.isEqualTo(org.springframework.ldap.ConfigurationException.class);
		assertThat(LdapUtils.convertLdapException(new MockContextNotEmptyException()).getClass())
			.isEqualTo(org.springframework.ldap.ContextNotEmptyException.class);
		assertThat(LdapUtils.convertLdapException(new MockInsufficientResourcesException()).getClass())
			.isEqualTo(org.springframework.ldap.InsufficientResourcesException.class);
		assertThat(LdapUtils.convertLdapException(new MockInterruptedNamingException()).getClass())
			.isEqualTo(org.springframework.ldap.InterruptedNamingException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidAttributeIdentifierException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidAttributeIdentifierException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidAttributesException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidAttributesException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidAttributeValueException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidAttributeValueException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidNameException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidNameException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidSearchControlsException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidSearchControlsException.class);
		assertThat(LdapUtils.convertLdapException(new MockInvalidSearchFilterException()).getClass())
			.isEqualTo(org.springframework.ldap.InvalidSearchFilterException.class);
		assertThat(LdapUtils.convertLdapException(new MockSizeLimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.SizeLimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new MockTimeLimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.TimeLimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new MockLimitExceededException()).getClass())
			.isEqualTo(org.springframework.ldap.LimitExceededException.class);
		assertThat(LdapUtils.convertLdapException(new MockLinkLoopException()).getClass())
			.isEqualTo(org.springframework.ldap.LinkLoopException.class);
		assertThat(LdapUtils.convertLdapException(new MockMalformedLinkException()).getClass())
			.isEqualTo(org.springframework.ldap.MalformedLinkException.class);
		assertThat(LdapUtils.convertLdapException(new MockLinkException()).getClass())
			.isEqualTo(org.springframework.ldap.LinkException.class);
		assertThat(LdapUtils.convertLdapException(new MockNameAlreadyBoundException()).getClass())
			.isEqualTo(org.springframework.ldap.NameAlreadyBoundException.class);
		assertThat(LdapUtils.convertLdapException(new MockNameNotFoundException()).getClass())
			.isEqualTo(org.springframework.ldap.NameNotFoundException.class);
		assertThat(LdapUtils.convertLdapException(new MockNoPermissionException()).getClass())
			.isEqualTo(org.springframework.ldap.NoPermissionException.class);
		assertThat(LdapUtils.convertLdapException(new MockAuthenticationException()).getClass())
			.isEqualTo(org.springframework.ldap.AuthenticationException.class);
		assertThat(LdapUtils.convertLdapException(new MockAuthenticationNotSupportedException()).getClass())
			.isEqualTo(org.springframework.ldap.AuthenticationNotSupportedException.class);
		assertThat(LdapUtils.convertLdapException(new MockNoInitialContextException()).getClass())
			.isEqualTo(org.springframework.ldap.NoInitialContextException.class);
		assertThat(LdapUtils.convertLdapException(new MockNoSuchAttributeException()).getClass())
			.isEqualTo(org.springframework.ldap.NoSuchAttributeException.class);
		assertThat(LdapUtils.convertLdapException(new MockNotContextException()).getClass())
			.isEqualTo(org.springframework.ldap.NotContextException.class);
		assertThat(LdapUtils.convertLdapException(new MockOperationNotSupportedException()).getClass())
			.isEqualTo(org.springframework.ldap.OperationNotSupportedException.class);
		assertThat(LdapUtils.convertLdapException(new MockPartialResultException()).getClass())
			.isEqualTo(org.springframework.ldap.PartialResultException.class);
		assertThat(LdapUtils.convertLdapException(new MockSchemaViolationException()).getClass())
			.isEqualTo(org.springframework.ldap.SchemaViolationException.class);
		assertThat(LdapUtils.convertLdapException(new MockServiceUnavailableException()).getClass())
			.isEqualTo(org.springframework.ldap.ServiceUnavailableException.class);
	}

	public class MockAttributeInUseException extends javax.naming.directory.AttributeInUseException {

	}

	public class MockAttributeModificationException extends javax.naming.directory.AttributeModificationException {

	}

	public class MockCannotProceedException extends javax.naming.CannotProceedException {

	}

	public class MockCommunicationException extends javax.naming.CommunicationException {

	}

	public class MockConfigurationException extends javax.naming.ConfigurationException {

	}

	public class MockContextNotEmptyException extends javax.naming.ContextNotEmptyException {

	}

	public class MockInsufficientResourcesException extends javax.naming.InsufficientResourcesException {

	}

	public class MockInterruptedNamingException extends javax.naming.InterruptedNamingException {

	}

	public class MockInvalidAttributeIdentifierException
			extends javax.naming.directory.InvalidAttributeIdentifierException {

	}

	public class MockInvalidAttributesException extends javax.naming.directory.InvalidAttributesException {

	}

	public class MockInvalidAttributeValueException extends javax.naming.directory.InvalidAttributeValueException {

	}

	public class MockInvalidNameException extends javax.naming.InvalidNameException {

	}

	public class MockInvalidSearchControlsException extends javax.naming.directory.InvalidSearchControlsException {

	}

	public class MockInvalidSearchFilterException extends javax.naming.directory.InvalidSearchFilterException {

	}

	public class MockSizeLimitExceededException extends javax.naming.SizeLimitExceededException {

	}

	public class MockTimeLimitExceededException extends javax.naming.TimeLimitExceededException {

	}

	public class MockLimitExceededException extends javax.naming.LimitExceededException {

	}

	public class MockLinkLoopException extends javax.naming.LinkLoopException {

	}

	public class MockMalformedLinkException extends javax.naming.MalformedLinkException {

	}

	public class MockLinkException extends javax.naming.LinkException {

	}

	public class MockNameAlreadyBoundException extends javax.naming.NameAlreadyBoundException {

	}

	public class MockNameNotFoundException extends javax.naming.NameNotFoundException {

	}

	public class MockNoPermissionException extends javax.naming.NoPermissionException {

	}

	public class MockAuthenticationException extends javax.naming.AuthenticationException {

	}

	public class MockAuthenticationNotSupportedException extends javax.naming.AuthenticationNotSupportedException {

	}

	public class MockNoInitialContextException extends javax.naming.NoInitialContextException {

	}

	public class MockNoSuchAttributeException extends javax.naming.directory.NoSuchAttributeException {

	}

	public class MockNotContextException extends javax.naming.NotContextException {

	}

	public class MockOperationNotSupportedException extends javax.naming.OperationNotSupportedException {

	}

	public class MockPartialResultException extends javax.naming.PartialResultException {

	}

	public class MockSchemaViolationException extends javax.naming.directory.SchemaViolationException {

	}

	public class MockServiceUnavailableException extends javax.naming.ServiceUnavailableException {

	}

}
