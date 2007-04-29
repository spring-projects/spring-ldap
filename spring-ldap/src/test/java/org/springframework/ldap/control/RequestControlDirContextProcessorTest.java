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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.control.AbstractRequestControlDirContextProcessor;

public class RequestControlDirContextProcessorTest extends TestCase {

    private AbstractRequestControlDirContextProcessor tested;

    private MockControl requestControlControl;

    private Control requestControlMock;

    private MockControl requestControl2Control;

    private Control requestControl2Mock;

    private MockControl ldapContextControl;

    private LdapContext ldapContextMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    protected void setUp() throws Exception {
        super.setUp();
        // Create requestControl mock
        requestControlControl = MockControl.createControl(Control.class);
        requestControlMock = (Control) requestControlControl.getMock();

        // Create requestControl2 mock
        requestControl2Control = MockControl.createControl(Control.class);
        requestControl2Mock = (Control) requestControl2Control.getMock();

        // Create ldapContext mock
        ldapContextControl = MockControl.createControl(LdapContext.class);
        ldapContextMock = (LdapContext) ldapContextControl.getMock();

        // Create dirContext mock
        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        tested = new AbstractRequestControlDirContextProcessor() {

            public Control createRequestControl() {
                return requestControlMock;
            }

            public void postProcess(DirContext ctx) throws NamingException {
            }

        };
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        requestControlControl = null;
        requestControlMock = null;

        requestControl2Control = null;
        requestControl2Mock = null;

        ldapContextControl = null;
        ldapContextMock = null;

        dirContextControl = null;
        dirContextMock = null;

    }

    protected void replay() {
        requestControlControl.replay();
        requestControl2Control.replay();
        ldapContextControl.replay();
        dirContextControl.replay();
    }

    protected void verify() {
        requestControlControl.verify();
        requestControl2Control.verify();
        ldapContextControl.verify();
        dirContextControl.verify();
    }

    public void testPreProcess() throws NamingException {
        ldapContextControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getRequestControls(), new Control[] { requestControl2Mock });
        ldapContextMock.setRequestControls(new Control[] { requestControl2Mock,
                requestControlMock });

        replay();

        tested.preProcess(ldapContextMock);

        verify();
    }

    public void testPreProcess_NoExistingControls() throws NamingException {
        ldapContextControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getRequestControls(), new Control[0]);
        ldapContextMock
                .setRequestControls(new Control[] { requestControlMock });

        replay();

        tested.preProcess(ldapContextMock);

        verify();
    }

    public void testPreProcess_NullControls() throws NamingException {
        ldapContextControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getRequestControls(), null);
        ldapContextMock
                .setRequestControls(new Control[] { requestControlMock });

        replay();

        tested.preProcess(ldapContextMock);

        verify();
    }

    public void testPreProcess_NotLdapContext() throws Exception {
        try {
            tested.preProcess(dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }
}
