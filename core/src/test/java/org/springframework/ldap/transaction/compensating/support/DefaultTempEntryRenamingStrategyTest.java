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
package org.springframework.ldap.transaction.compensating.support;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;

import junit.framework.TestCase;

public class DefaultTempEntryRenamingStrategyTest extends TestCase {

    public void testGetTemporaryName() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe, ou=somecompany, c=SE");
        DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp, ou=somecompany, c=SE", result
                .toString());
        assertNotSame(expectedOriginalName, result);
    }

    public void testGetTemporaryDN_MultivalueDN() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe+sn=doe, ou=somecompany, c=SE");
        DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp+sn=doe, ou=somecompany, c=SE", result
                .toString());
    }


}
