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
import javax.naming.ldap.PagedResultsControl;

import junit.framework.TestCase;

import org.easymock.MockControl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.BerEncoder;
import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import com.sun.jndi.ldap.ctl.SortControl;
import com.sun.jndi.ldap.ctl.SortResponseControl;

/**
 * Unit tests for the SortControlDirContextProcessor class.
 * {@link javax.naming.ldap.SortControl}
 * {@link javax.naming.ldap.SortResponseControl}
 * {@link PagedResultsControl}
 * 
 * @author Ulrik Sandberg
 */
public class SortControlDirContextProcessorTest extends TestCase {

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
        SortControlDirContextProcessor tested = new SortControlDirContextProcessor(
                "key");

        SortControl result = (SortControl) tested.createRequestControl();
        assertNotNull(result);
        assertEquals("1.2.840.113556.1.4.473", result.getID());
        assertEquals(9, result.getEncodedValue().length);
    }

    public void testPostProcess() throws Exception {
        byte sortResult = 0; // success

        byte[] value = encodeValue(sortResult);
        SortResponseControl control = new SortResponseControl(
                "dummy", true, value);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        SortControlDirContextProcessor tested = new SortControlDirContextProcessor("key");

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        assertEquals(true, tested.isSorted());
        assertEquals(0, tested.getResultCode());
    }

    public void testPostProcess_NonSuccess() throws Exception {
        byte sortResult = 1;

        byte[] value = encodeValue(sortResult);
        SortResponseControl control = new SortResponseControl(
                "dummy", true, value);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        SortControlDirContextProcessor tested = new SortControlDirContextProcessor("key");

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        assertEquals(false, tested.isSorted());
        assertEquals(1, tested.getResultCode());
    }

    public void testPostProcess_InvalidResponseControl() throws Exception {
        int resultSize = 50;
        byte pageSize = 8;

        byte[] value = new byte[1];
        value[0] = pageSize;
        byte[] cookie = encodeDirSyncValue(resultSize, value);

        // Using another response control to verify that it is ignored
        DirSyncResponseControl control = new DirSyncResponseControl("dummy",
                true, cookie);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        SortControlDirContextProcessor tested = new SortControlDirContextProcessor("key");

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        assertEquals(false, tested.isSorted());
    }

    public void testBerDecoding() throws Exception {
        int sortResult = 53; // unwilling to perform
        byte[] encoded = encodeValue(sortResult);

        BerDecoder ber = new BerDecoder(encoded, 0, encoded.length);

        ber.parseSeq(null);
        int actualSortResult = ber.parseEnumeration();

        assertEquals("sortResult,", 53, actualSortResult);
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
