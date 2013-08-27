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

package org.springframework.ldap.core;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.Name;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the rename operations in the LdapTemplate class.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateRenameTest {

    private ContextSource contextSourceMock;

    private DirContext dirContextMock;

    private Name oldNameMock;

    private Name newNameMock;

    private LdapTemplate tested;

    @Before
    public void setUp() throws Exception {
        // Setup ContextSource mock
        contextSourceMock = mock(ContextSource.class);

        // Setup LdapContext mock
        dirContextMock = mock(LdapContext.class);

        // Setup Name mock for old name
        oldNameMock = mock(Name.class);

        // Setup Name mock for new name
        newNameMock = mock(Name.class);

        tested = new LdapTemplate(contextSourceMock);
    }

    private void expectGetReadWriteContext() {
        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
    }

    @Test
    public void testRename() throws Exception {
        expectGetReadWriteContext();

        tested.rename(oldNameMock, newNameMock);

        verify(dirContextMock).rename(oldNameMock, newNameMock);
        verify(dirContextMock).close();
    }

    @Test
    public void testRename_NameAlreadyBoundException() throws Exception {
        expectGetReadWriteContext();

        javax.naming.NameAlreadyBoundException ne = new javax.naming.NameAlreadyBoundException();
        doThrow(ne).when(dirContextMock).rename(oldNameMock, newNameMock);

        try {
            tested.rename(oldNameMock, newNameMock);
            fail("NameAlreadyBoundException expected");
        } catch (NameAlreadyBoundException expected) {
            assertTrue(true);
        }

        verify(dirContextMock).close();
    }

    @Test
    public void testRename_NamingException() throws Exception {
        expectGetReadWriteContext();

        javax.naming.NamingException ne = new javax.naming.NamingException();

        doThrow(ne).when(dirContextMock).rename(oldNameMock, newNameMock);

        try {
            tested.rename(oldNameMock, newNameMock);
            fail("UncategorizedLdapException expected");
        } catch (UncategorizedLdapException expected) {
            assertTrue(true);
        }

        verify(dirContextMock).close();
    }

    @Test
    public void testRename_String() throws Exception {
        expectGetReadWriteContext();

        tested.rename("o=example.com", "o=somethingelse.com");

        verify(dirContextMock).rename("o=example.com", "o=somethingelse.com");
        verify(dirContextMock).close();
    }
}
