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
package org.springframework.ldap.core.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Integration tests for ContextSourceImpl.
 * 
 * @author Mattias Arthursson
 */
public class LdapContextSourcelITest extends
        AbstractDependencyInjectionSpringContextTests {

    private LdapContextSource tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    public void testGetReadOnlyContext() throws NamingException {
        DirContext ctx = null;

        try {
            ctx = tested.getReadOnlyContext();
            assertNotNull(ctx);
            Hashtable environment = ctx.getEnvironment();
            assertTrue(environment
                    .containsKey(LdapContextSource.SUN_LDAP_POOLING_FLAG));
            assertTrue(environment.containsKey(Context.SECURITY_PRINCIPAL));
            assertTrue(environment.containsKey(Context.SECURITY_CREDENTIALS));
        } finally {
            // Always clean up.
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this
                }
            }
        }
    }

    public void testGetReadWriteContext() throws NamingException {
        DirContext ctx = null;

        try {
            ctx = tested.getReadWriteContext();
            assertNotNull(ctx);
            // Double check to see that we are authenticated.
            Hashtable environment = ctx.getEnvironment();
            assertTrue(environment.containsKey(Context.SECURITY_PRINCIPAL));
            assertTrue(environment.containsKey(Context.SECURITY_CREDENTIALS));
        } finally {
            // Always clean up.
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this
                }
            }
        }
    }

    public void setTested(LdapContextSource tested) {
        this.tested = tested;
    }
}
