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
package org.springframework.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Tests for LdapTemplate's context executor methods.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateContextExecutorTest extends
        AbstractLdapTemplateIntegrationTest {

    private LdapTemplate tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();
    }

    public void testLookupLink() {
        ContextExecutor executor = new ContextExecutor(){
            public Object executeWithContext(DirContext ctx) throws NamingException {
                return ctx.lookupLink("cn=Some Person,ou=company1,c=Sweden");
            }
        };
        
        Object object = tested.executeReadOnly(executor);
        assertTrue("Should be a DirContextAdapter", object instanceof DirContextAdapter);
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
