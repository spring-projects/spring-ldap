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
import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PagedResultsDirContextProcessorTest {

	private LdapContext ldapContextMock;

	private PagedResultsDirContextProcessor tested;

	@Before
	public void setUp() throws Exception {

		tested = new PagedResultsDirContextProcessor(20);

		// Create ldapContext mock
		ldapContextMock = mock(LdapContext.class);
	}

	@After
	public void tearDown() throws Exception {

		tested = null;
		ldapContextMock = null;
	}

	@Test
	public void testCreateRequestControl() throws Exception {
		PagedResultsControl control = (PagedResultsControl) tested
				.createRequestControl();
		assertThat(control).isNotNull();
	}

	@Test
	public void testCreateRequestControl_CookieSet() throws Exception {
		PagedResultsCookie cookie = new PagedResultsCookie(new byte[0]);
		PagedResultsDirContextProcessor tested = new PagedResultsDirContextProcessor(20,
				cookie);

		PagedResultsControl control = (PagedResultsControl) tested
				.createRequestControl();
		assertThat(control).isNotNull();
	}

	@Test
	public void testPostProcess() throws Exception {
		int resultSize = 50;
		byte pageSize = 8;

		byte[] value = new byte[1];
		value[0] = pageSize;
		byte[] cookie = encodeValue(resultSize, value);
		PagedResultsResponseControl control = new PagedResultsResponseControl(
				"dummy", true, cookie);

		when(ldapContextMock.getResponseControls()).thenReturn(new Control[] { control });
		tested.postProcess(ldapContextMock);

		PagedResultsCookie returnedCookie = tested.getCookie();
		assertThat(returnedCookie.getCookie()[0]).isEqualTo((byte)8);
		assertThat(tested.getPageSize()).isEqualTo(20);
		assertThat(tested.getResultSize()).isEqualTo(50);
	}

	@Test
	public void testPostProcess_InvalidResponseControl() throws Exception {
		int resultSize = 50;
		byte pageSize = 8;

		byte[] value = new byte[1];
		value[0] = pageSize;
		byte[] cookie = encodeDirSyncValue(resultSize, value);

		// Using another response control to verify that it is ignored
		DirSyncResponseControl control = new DirSyncResponseControl(
				"dummy", true, cookie);


		when(ldapContextMock.getResponseControls()).thenReturn(new Control[]{control});
		tested.postProcess(ldapContextMock);

		assertThat(tested.getCookie()).isNull();
		assertThat(tested.getPageSize()).isEqualTo(20);
		assertThat(tested.getResultSize()).isEqualTo(0);
	}

	@Test
	public void testPostProcess_NoResponseControls() throws Exception {
		when(ldapContextMock.getResponseControls()).thenReturn(null);

		tested.postProcess(ldapContextMock);

		assertThat(tested.getCookie()).isNull();
		assertThat(tested.getPageSize()).isEqualTo(20);
		assertThat(tested.getResultSize()).isEqualTo(0);
	}

	@Test
	public void testBerDecoding() throws Exception {
		byte[] value = new byte[1];
		value[0] = 8;
		int pageSize = 20;
		byte[] cookie = encodeValue(pageSize, value);

		BerDecoder ber = new BerDecoder(cookie, 0, cookie.length);

		ber.parseSeq(null);
		int actualPageSize = ber.parseInt();
		byte[] actualValue = ber.parseOctetString(Ber.ASN_OCTET_STR, null);

		assertThat(actualPageSize).as("pageSize,").isEqualTo(20);
		assertThat(actualValue.length).as("value length").isEqualTo(value.length);
		for (int i = 0; i < value.length; i++) {
			assertThat(actualValue[i]).as("value (index " + i + "),").isEqualTo(value[i]);
		}
	}

	private byte[] encodeValue(int pageSize, byte[] cookie)
			throws IOException {

		// build the ASN.1 encoding
		BerEncoder ber = new BerEncoder(10 + cookie.length);

		ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		ber.encodeInt(pageSize);
		ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
		ber.endSeq();

		return ber.getTrimmedBuf();
	}

	/**
	 * Encode a value suitable for the DirSyncResponseControl used in a test.
	 */
	private byte[] encodeDirSyncValue(int pageSize, byte[] cookie)
			throws IOException {

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
