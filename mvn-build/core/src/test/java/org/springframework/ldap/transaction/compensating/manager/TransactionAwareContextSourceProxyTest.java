/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;

/**
 * Tests for {@link TransactionAwareContextSourceProxy}.
 * 
 * @author Mattias Arthursson
 */
public class TransactionAwareContextSourceProxyTest extends TestCase {
    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private TransactionAwareContextSourceProxy tested;

    private MockControl ldapContextControl;

    private LdapContext ldapContextMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    protected void setUp() throws Exception {

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        ldapContextControl = MockControl.createControl(LdapContext.class);
        ldapContextMock = (LdapContext) ldapContextControl.getMock();

        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        tested = new TransactionAwareContextSourceProxy(contextSourceMock);
    }

    protected void tearDown() throws Exception {
        contextSourceControl = null;
        contextSourceMock = null;

        ldapContextControl = null;
        ldapContextMock = null;

        dirContextControl = null;
        dirContextMock = null;

        tested = null;
    }

    public void testGetReadWriteContext_LdapContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), ldapContextMock);

        contextSourceControl.replay();

        DirContext result = tested.getReadWriteContext();

        contextSourceControl.verify();

        assertNotNull("Result should not be null", result);
        assertTrue("Should be an LdapContext instance",
                result instanceof LdapContext);
        assertTrue("Should be a DirContextProxy instance",
                result instanceof DirContextProxy);
    }

    public void testGetReadWriteContext_DirContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), dirContextMock);

        contextSourceControl.replay();

        DirContext result = tested.getReadWriteContext();

        contextSourceControl.verify();

        assertNotNull("Result should not be null", result);
        assertTrue("Should be a DirContext instance",
                result instanceof DirContext);
        assertFalse("Should not be an LdapContext instance",
                result instanceof LdapContext);
        assertTrue("Should be a DirContextProxy instance",
                result instanceof DirContextProxy);
    }

    public void testGetReadOnlyContext_LdapContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), ldapContextMock);

        contextSourceControl.replay();

        DirContext result = tested.getReadOnlyContext();

        contextSourceControl.verify();

        assertNotNull("Result should not be null", result);
        assertTrue("Should be an LdapContext instance",
                result instanceof LdapContext);
        assertTrue("Should be a DirContextProxy instance",
                result instanceof DirContextProxy);
    }
}
