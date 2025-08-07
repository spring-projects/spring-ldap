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

package org.springframework.ldap.control;

import java.io.IOException;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortResponseControl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.BerEncoder;
import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * Unit tests for the SortControlDirContextProcessor class.
 *
 * @author Ulrik Sandberg
 */
public class SortControlDirContextProcessorTests {

	private LdapContext ldapContextMock;

	private SortControlDirContextProcessor tested;

	@BeforeEach
	public void setUp() throws Exception {
		this.tested = new SortControlDirContextProcessor("key");

		// Create ldapContext mock
		this.ldapContextMock = mock(LdapContext.class);
	}

	@Test
	public void testCreateRequestControl() throws Exception {
		SortControl result = (SortControl) this.tested.createRequestControl();
		assertThat(result).isNotNull();
		assertThat(result.getID()).isEqualTo("1.2.840.113556.1.4.473");
		assertThat(result.getEncodedValue().length).isEqualTo(9);
	}

	@Test
	public void testPostProcess() throws Exception {
		byte sortResult = 0; // success

		byte[] value = encodeValue(sortResult);
		SortResponseControl control = new SortResponseControl("dummy", true, value);

		given(this.ldapContextMock.getResponseControls()).willReturn(new Control[] { control });

		this.tested.postProcess(this.ldapContextMock);

		assertThat(this.tested.isSorted()).isEqualTo(true);
		assertThat(this.tested.getResultCode()).isEqualTo(0);
	}

	@Test
	public void testPostProcess_NonSuccess() throws Exception {
		byte sortResult = 1;

		byte[] value = encodeValue(sortResult);
		SortResponseControl control = new SortResponseControl("dummy", true, value);

		given(this.ldapContextMock.getResponseControls()).willReturn(new Control[] { control });

		this.tested.postProcess(this.ldapContextMock);

		assertThat(this.tested.isSorted()).isEqualTo(false);
		assertThat(this.tested.getResultCode()).isEqualTo(1);
	}

	@Test
	public void testPostProcess_InvalidResponseControl() throws Exception {
		int resultSize = 50;
		byte pageSize = 8;

		byte[] value = new byte[1];
		value[0] = pageSize;
		byte[] cookie = encodeDirSyncValue(resultSize, value);

		// Using another response control to verify that it is ignored
		DirSyncResponseControl control = new DirSyncResponseControl("dummy", true, cookie);

		given(this.ldapContextMock.getResponseControls()).willReturn(new Control[] { control });

		this.tested.postProcess(this.ldapContextMock);

		assertThat(this.tested.isSorted()).isEqualTo(false);
	}

	@Test
	public void testBerDecoding() throws Exception {
		int sortResult = 53; // unwilling to perform
		byte[] encoded = encodeValue(sortResult);

		BerDecoder ber = new BerDecoder(encoded, 0, encoded.length);

		ber.parseSeq(null);
		int actualSortResult = ber.parseEnumeration();

		assertThat(actualSortResult).as("sortResult,").isEqualTo(53);
	}

	private byte[] encodeValue(int sortResult) throws IOException {

		// build the ASN.1 encoding
		BerEncoder ber = new BerEncoder(10);

		ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		ber.encodeInt(sortResult, Ber.ASN_ENUMERATED);
		ber.endSeq();

		return ber.getTrimmedBuf();
	}

	/**
	 * Encode a value suitable for the DirSyncResponseControl used in a test.
	 */
	private byte[] encodeDirSyncValue(int pageSize, byte[] cookie) throws IOException {

		// build the ASN.1 encoding
		BerEncoder ber = new BerEncoder(10 + cookie.length);

		ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		ber.encodeInt(1); // flag
		ber.encodeInt(pageSize); // maxReturnLength
		ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
		ber.endSeq();

		return ber.getTrimmedBuf();
	}

}
