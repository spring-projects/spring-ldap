/*
 * Copyright 2005-2013 the original author or authors.
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

import com.sun.jndi.ldap.ctl.SortControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestControlDirContextProcessorTest {

    private AbstractRequestControlDirContextProcessor tested;

    private Control requestControlMock;

    private Control requestControl2Mock;

    private LdapContext ldapContextMock;

    private DirContext dirContextMock;

    @Before
    public void setUp() throws Exception {
        // Create requestControl mock
        requestControlMock = mock(Control.class);

        // Create requestControl2 mock
        requestControl2Mock = mock(Control.class);

        // Create ldapContext mock
        ldapContextMock = mock(LdapContext.class);

        // Create dirContext mock
        dirContextMock = mock(DirContext.class);

        tested = new AbstractRequestControlDirContextProcessor() {

            public Control createRequestControl() {
                return requestControlMock;
            }

            public void postProcess(DirContext ctx) throws NamingException {
            }

        };
    }

    @After
    public void tearDown() throws Exception {
        requestControlMock = null;
        requestControl2Mock = null;
        ldapContextMock = null;
        dirContextMock = null;

    }

    @Test
    public void testPreProcessWithExistingControlOfDifferentClassShouldAdd() throws Exception {
        SortControl existingControl = new SortControl(new String[] { "cn" }, true);
        when(ldapContextMock.getRequestControls()).thenReturn(new Control[]{existingControl});

        tested.preProcess(ldapContextMock);

        verify(ldapContextMock).setRequestControls(new Control[] { existingControl, requestControlMock });
    }

    @Test
    public void testPreProcessWithExistingControlOfSameClassShouldReplace() throws Exception {
        when(ldapContextMock.getRequestControls()).thenReturn(new Control[]{requestControl2Mock});

        tested.preProcess(ldapContextMock);

        verify(ldapContextMock).setRequestControls(new Control[] { requestControlMock });
    }

    @Test
    public void testPreProcessWithExistingControlOfSameClassAndPropertyFalseShouldAdd() throws Exception {
        when(ldapContextMock.getRequestControls()).thenReturn(new Control[] { requestControl2Mock });

        tested.setReplaceSameControlEnabled(false);
        tested.preProcess(ldapContextMock);

        verify(ldapContextMock).setRequestControls(new Control[]{requestControl2Mock, requestControlMock});
    }

    @Test
    public void testPreProcessWithNoExistingControlsShouldAdd() throws NamingException {
        when(ldapContextMock.getRequestControls()).thenReturn(new Control[0]);

        tested.preProcess(ldapContextMock);

        verify(ldapContextMock).setRequestControls(new Control[]{requestControlMock});
    }

    @Test
    public void testPreProcessWithNullControlsShouldAdd() throws NamingException {
        when(ldapContextMock.getRequestControls()).thenReturn(null);

        tested.preProcess(ldapContextMock);

        verify(ldapContextMock).setRequestControls(new Control[] { requestControlMock });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreProcessWhenNotLdapContextShouldFail() throws Exception {
        tested.preProcess(dirContextMock);
    }
}
