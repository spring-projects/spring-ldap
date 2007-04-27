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

package org.springframework.ldap.authentication;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;


import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.ldap.LdapAuthenticationProvider;
import org.springframework.ldap.authentication.AcegiAuthenticationSource;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class AcegiAuthenticationSourceITest extends
        AbstractDependencyInjectionSpringContextTests {

    private LdapAuthenticationProvider ldapAuthProvider;

    private LdapTemplate ldapTemplate;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateAcegiTestContext.xml" };
    }

    public void setLdapAuthProvider(LdapAuthenticationProvider ldapAuthProvider) {
        this.ldapAuthProvider = ldapAuthProvider;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void testGetPrincipalAndCredentials() {
        Authentication authentication = ldapAuthProvider
                .authenticate(new UsernamePasswordAuthenticationToken(
                        "Some Person3", "password"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AcegiAuthenticationSource tested = new AcegiAuthenticationSource();
        assertEquals("cn=Some Person3,ou=company1,c=Sweden,dc=jayway,dc=se",
                tested.getPrincipal());
        assertEquals("password", tested.getCredentials());
    }

    public void testSearchIndiviualAuthentication() {
        Authentication authentication = ldapAuthProvider
                .authenticate(new UsernamePasswordAuthenticationToken(
                        "Some Person3", "password"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        List result = ldapTemplate.search("dc=jayway,dc=se",
                "(objectclass=person)", new AttributesMapper() {
                    public Object mapFromAttributes(Attributes attributes)
                            throws NamingException {
                        return attributes.get("cn");
                    }
                });
        assertEquals(5, result.size());
    }

}
