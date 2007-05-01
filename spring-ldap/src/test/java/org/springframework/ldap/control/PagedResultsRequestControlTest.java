/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.control;

import java.io.IOException;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsRequestControl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.BerEncoder;
import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import com.sun.jndi.ldap.ctl.PagedResultsControl;
import com.sun.jndi.ldap.ctl.PagedResultsResponseControl;

public class PagedResultsRequestControlTest extends TestCase {

    private MockControl ldapContextControl;

    private LdapContext ldapContextMock;

    protected void setUp() throws Exception {
        super.setUp();

        // Create ldapContext mock
        ldapContextControl = MockControl.createControl(LdapContext.class);
        ldapContextMock = (LdapContext) ldapContextControl.getMock();

    }

    protected void tearDown() throws Exception {
        super.tearDown();

        ldapContextControl = null;
        ldapContextMock = null;

    }

    protected void replay() {
        ldapContextControl.replay();
    }

    protected void verify() {
        ldapContextControl.verify();
    }

    public void testCreateRequestControl() throws Exception {
        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        PagedResultsControl control = (PagedResultsControl) tested
                .createRequestControl();
        assertNotNull(control);
    }

    public void testCreateRequestControl_CookieSet() throws Exception {
        PagedResultsCookie cookie = new PagedResultsCookie(new byte[0]);
        PagedResultsRequestControl tested = new PagedResultsRequestControl(20,
                cookie);

        PagedResultsControl control = (PagedResultsControl) tested
                .createRequestControl();
        assertNotNull(control);
    }

    public void testPostProcess() throws Exception {
        int resultSize = 50;
        byte pageSize = 8;

        byte[] value = new byte[1];
        value[0] = pageSize;
        byte[] cookie = encodeValue(resultSize, value);
        PagedResultsResponseControl control = new PagedResultsResponseControl(
                "dummy", true, cookie);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        PagedResultsCookie returnedCookie = tested.getCookie();
        assertEquals(8, returnedCookie.getCookie()[0]);
        assertEquals(20, tested.getPageSize());
        assertEquals(50, tested.getResultSize());
    }

    public void testPostProcess_InvalidResponseControl() throws Exception {
        int resultSize = 50;
        byte pageSize = 8;

        byte[] value = new byte[1];
        value[0] = pageSize;
        byte[] cookie = encodeDirSyncValue(resultSize, value);
        
        // Using another response control to verify that it is ignored
        DirSyncResponseControl control = new DirSyncResponseControl(
                "dummy", true, cookie);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        assertNull(tested.getCookie());
        assertEquals(20, tested.getPageSize());
        assertEquals(0, tested.getResultSize());
    }

    public void testPostProcess_NoResponseControls() throws Exception {
        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), null);

        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        assertNull(tested.getCookie());
        assertEquals(20, tested.getPageSize());
        assertEquals(0, tested.getResultSize());
    }

    public void testBerDecoding() throws Exception {
        byte[] value = new byte[1];
        value[0] = 8;
        int pageSize = 20;
        byte[] cookie = encodeValue(pageSize, value);
        
        BerDecoder ber = new BerDecoder(cookie, 0, cookie.length);

        ber.parseSeq(null);
        int actualPageSize = ber.parseInt();
        byte[] actualValue = ber.parseOctetString(Ber.ASN_OCTET_STR, null);

        assertEquals("pageSize,", 20, actualPageSize);
        assertEquals("value length", value.length, actualValue.length);
        for (int i = 0; i < value.length; i++) {
            assertEquals("value (index " + i + "),", value[i], actualValue[i]);
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
