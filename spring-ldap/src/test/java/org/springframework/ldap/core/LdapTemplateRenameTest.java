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

package org.springframework.ldap.core;

import javax.naming.Name;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.UncategorizedLdapException;

/**
 * Unit tests for the rename operations in the LdapTemplate class.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateRenameTest extends TestCase {

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl oldNameControl;

    private Name oldNameMock;

    private MockControl newNameControl;

    private Name newNameMock;

    private LdapTemplate tested;

    protected void setUp() throws Exception {
        super.setUp();

        // Setup ContextSource mock
        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        // Setup LdapContext mock
        dirContextControl = MockControl.createControl(LdapContext.class);
        dirContextMock = (LdapContext) dirContextControl.getMock();

        // Setup Name mock for old name
        oldNameControl = MockControl.createControl(Name.class);
        oldNameMock = (Name) oldNameControl.getMock();

        // Setup Name mock for new name
        newNameControl = MockControl.createControl(Name.class);
        newNameMock = (Name) newNameControl.getMock();

        tested = new LdapTemplate(contextSourceMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextSourceControl = null;
        contextSourceMock = null;

        dirContextControl = null;
        dirContextMock = null;

        oldNameControl = null;
        newNameMock = null;
    }

    protected void replay() {
        contextSourceControl.replay();
        dirContextControl.replay();
        oldNameControl.replay();
    }

    protected void verify() {
        contextSourceControl.verify();
        dirContextControl.verify();
        oldNameControl.verify();
    }

    private void expectGetReadWriteContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadWriteContext(), dirContextMock);
    }

    public void testRename() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.rename(oldNameMock, newNameMock);
        dirContextMock.close();

        replay();

        tested.rename(oldNameMock, newNameMock);

        verify();
    }

    public void testRename_NameAlreadyBoundException() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.rename(oldNameMock, newNameMock);
        javax.naming.NameAlreadyBoundException ne = new javax.naming.NameAlreadyBoundException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        replay();

        try {
            tested.rename(oldNameMock, newNameMock);
            fail("NameAlreadyBoundException expected");
        } catch (NameAlreadyBoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testRename_NamingException() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.rename(oldNameMock, newNameMock);
        javax.naming.NamingException ne = new javax.naming.NamingException();
        dirContextControl.setThrowable(ne);
        dirContextMock.close();

        replay();

        try {
            tested.rename(oldNameMock, newNameMock);
            fail("UncategorizedLdapException expected");
        } catch (UncategorizedLdapException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testRename_String() throws Exception {
        expectGetReadWriteContext();

        dirContextMock.rename("o=example.com", "o=somethingelse.com");
        dirContextMock.close();

        replay();

        tested.rename("o=example.com", "o=somethingelse.com");

        verify();
    }
}
