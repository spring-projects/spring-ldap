/*
 * Copyright 2005-2016 the original author or authors.
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
package org.springframework.ldap.control;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.BerEncoder;
import com.sun.jndi.ldap.ctl.VirtualListViewControl;
import com.sun.jndi.ldap.ctl.VirtualListViewResponseControl;
import junit.framework.AssertionFailedError;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.OperationNotSupportedException;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the VirtualListViewControlDirContextProcessor class.
 *
 * @author Ulrik Sandberg
 */
public class VirtualListViewControlDirContextProcessorTest {

	private static final String OID_REQUEST = "2.16.840.1.113730.3.4.9";

	private static final String OID_RESPONSE = "2.16.840.1.113730.3.4.10";

	private LdapContext ldapContextMock;

	@Before
	public void setUp() throws Exception {
		// Create ldapContext mock
		ldapContextMock = mock(LdapContext.class);
	}

	@Test
	public void testCreateRequestControlWithTargetAsOffset() throws Exception {
		int pageSize = 5;
		int targetOffset = 25;
		int listSize = 1000;
		VirtualListViewControlDirContextProcessor tested = new VirtualListViewControlDirContextProcessor(pageSize,
				targetOffset, listSize, new VirtualListViewResultsCookie(new byte[0], 0, 0));
		VirtualListViewControl result = (VirtualListViewControl) tested.createRequestControl();
		assertThat(result).isNotNull();
		assertThat(result.getID()).isEqualTo(OID_REQUEST);

		// verify that the values have been encoded as we expect
		int expectedBeforeCount = 0;
		int expectedAfterCount = 4;
		int expectedOffset = 25;
		int expectedContentCount = listSize;
		assertEncodedRequest(result.getEncodedValue(), expectedBeforeCount, expectedAfterCount, expectedOffset,
				expectedContentCount, new byte[0]);
	}

	@Test
	public void testCreateRequestControlWithTargetAsPercentage() throws Exception {
		int pageSize = 5;
		int targetPercentage = 25;
		int listSize = 1000;
		VirtualListViewControlDirContextProcessor tested = new VirtualListViewControlDirContextProcessor(pageSize,
				targetPercentage, listSize, new VirtualListViewResultsCookie(new byte[0], 0, 0));
		tested.setOffsetPercentage(true);
		VirtualListViewControl result = (VirtualListViewControl) tested.createRequestControl();
		assertThat(result).isNotNull();
		assertThat(result.getID()).isEqualTo(OID_REQUEST);

		int expectedBeforeCount = 2;
		int expectedAfterCount = 2;
		// interestingly, it seems rather than calculate what 25% of 1000 is,
		// the VLVControl requests 25 out of an expected 100
		int expectedOffset = 25;
		int expectedContentCount = 100;
		assertEncodedRequest(result.getEncodedValue(), expectedBeforeCount, expectedAfterCount, expectedOffset,
				expectedContentCount, new byte[0]);
	}

	@Test
	public void testPostProcess() throws Exception {
		int pageSize = 5;
		int targetOffset = 25;
		int listSize = 1000;
		VirtualListViewControlDirContextProcessor tested = new VirtualListViewControlDirContextProcessor(pageSize,
				targetOffset, listSize, new VirtualListViewResultsCookie(new byte[0], 0, 0));

		int virtualListViewResult = 53; // unwilling to perform
		byte[] encoded = encodeResponseValue(10, listSize, virtualListViewResult);
		VirtualListViewResponseControl control = new VirtualListViewResponseControl(OID_RESPONSE, false, encoded);
		when(ldapContextMock.getResponseControls()).thenReturn(new Control[] { control });

		try {
			tested.postProcess(ldapContextMock);
			fail("OperationNotSupportedException expected");
		}
		catch (OperationNotSupportedException expected) {
			Throwable cause = expected.getCause();
			assertThat(cause.getClass()).isEqualTo(javax.naming.OperationNotSupportedException.class);
			assertThat(cause.getMessage()).isEqualTo("[LDAP: error code 53 - Unwilling To Perform]");
		}

		assertThat(tested.getCookie()).isNotNull();
		assertThat(tested.getCookie().getCookie().length).isEqualTo(0);
	}

	@Test
	public void testBerDecoding() throws Exception {
		int virtualListViewResult = 53; // unwilling to perform
		byte[] encoded = encodeResponseValue(10, 1000, virtualListViewResult);

		int expectedLength = 14;
		assertEncodedResponse(encoded, expectedLength, 10, 1000, 53, new byte[0]);
	}

	private byte[] encodeResponseValue(int targetPosition, int contentCount, int virtualListViewResult)
			throws IOException {

		// build the ASN.1 encoding
		BerEncoder ber = new BerEncoder(10);

		ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		ber.encodeInt(targetPosition); // list offset for the target entry
		ber.encodeInt(contentCount); // server's estimate of the current
		// number of entries in the list
		ber.encodeInt(virtualListViewResult, Ber.ASN_ENUMERATED);
		ber.encodeOctetString(new byte[0], Ber.ASN_OCTET_STR);
		ber.endSeq();

		return ber.getTrimmedBuf();
	}

	private void assertEncodedRequest(byte[] encodedValue, int expectedBeforeCount, int expectedAfterCount,
			int expectedOffset, int expectedContentCount, byte[] expectedContextId) throws Exception {
		dumpEncodedValue("VirtualListViewRequest\n", encodedValue);
		BerDecoder ber = new BerDecoder(encodedValue, 0, encodedValue.length);
		ber.parseSeq(null);

		int actualBeforeCount = ber.parseInt();
		int actualAfterCount = ber.parseInt();
		byte targetType = (byte) ber.parseByte();
		targetType <<= 3; // skip highest three bits
		targetType >>= 3;
		ber.parseLength(); // ignore
		switch (targetType) {
		case 0: // byOffset
			int actualOffset = ber.parseInt();
			int actualContentCount = ber.parseInt();
			assertThat(expectedBeforeCount).isEqualTo(actualBeforeCount);
			assertThat(expectedAfterCount).isEqualTo(actualAfterCount);
			assertThat(expectedOffset).isEqualTo(actualOffset);
			assertThat(actualContentCount).isEqualTo(expectedContentCount);
			break;

		case 1: // greaterThanOrEqual
			throw new AssertionFailedError("CHOICE value greaterThanOrEqual not supported");

		default:
			throw new AssertionFailedError("illegal CHOICE value: " + targetType);
		}
		byte[] bs = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
		assertContextId(expectedContextId, bs);
	}

	private void assertContextId(byte[] expectedContextId, byte[] actualContextId) {
		if (expectedContextId == null && actualContextId == null) {
			return;
		}
		if (expectedContextId == null && actualContextId != null) {
			fail("expected <null>, got <" + actualContextId + ">");
		}
		if (expectedContextId != null && actualContextId == null) {
			fail("expected <" + expectedContextId + ">, got <null>");
		}
		assertThat(actualContextId.length).isEqualTo(expectedContextId.length);
	}

	private void assertEncodedResponse(byte[] encodedValue, int expectedEncodingLength, int expectedTargetPosition,
			int expectedContentCount, int expectedVirtualListViewResult, byte[] expectedContextId) throws Exception {
		dumpEncodedValue("VirtualListViewResponse\n", encodedValue);
		assertThat(encodedValue.length).isEqualTo(expectedEncodingLength);
		BerDecoder ber = new BerDecoder(encodedValue, 0, encodedValue.length);
		ber.parseSeq(null);

		int actualTargetPosition = ber.parseInt();
		int actualContentCount = ber.parseInt();
		int actualVirtualListViewResult = ber.parseEnumeration();
		assertThat(actualTargetPosition).isEqualTo(expectedTargetPosition);
		assertThat(actualContentCount).as("contentCount,").isEqualTo(expectedContentCount);
		assertThat(actualVirtualListViewResult).isEqualTo(expectedVirtualListViewResult);
		byte[] bs = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
		assertContextId(expectedContextId, bs);
	}

	private void dumpEncodedValue(String message, byte[] encodedValue) {
		Ber.dumpBER(System.out, message, encodedValue, 0, encodedValue.length);
	}

}
