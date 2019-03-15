/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.core.support;

import org.junit.Test;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class AbstractContextSourceTest {

    @Test
    public void testFormatForUrlNormal() throws InvalidNameException {
        LdapName ldapName = new LdapName("dc=261consulting, dc=com");

        String result = AbstractContextSource.formatForUrl(ldapName);
        assertThat(result).isEqualTo("dc=261consulting,dc=com");
    }

    @Test
    public void testFormatForUrlNormalWithQuestionMark() throws InvalidNameException {
        LdapName ldapName = new LdapName("dc=261consulting?, dc=com");

        String result = AbstractContextSource.formatForUrl(ldapName);
        assertThat(result).isEqualTo("dc=261consulting%3F,dc=com");
    }

    @Test
    public void testFormatForUrlWithSpace() throws InvalidNameException {
        LdapName ldapName = new LdapName("ou=some department, dc=261consulting, dc=com");

        String result = AbstractContextSource.formatForUrl(ldapName);
        assertThat(result).isEqualTo("ou=some%20department,dc=261consulting,dc=com");
    }

    @Test
    public void testFormatForUrlEmpty() throws InvalidNameException {
        LdapName ldapName = new LdapName("");

        String result = AbstractContextSource.formatForUrl(ldapName);
        assertThat(result).isEqualTo("");
    }
}
