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
import org.springframework.ldap.transaction.compensating.support.DifferentSubtreeTempEntryRenamingStrategy;

import junit.framework.TestCase;

public class DifferentSubtreeTempEntryRenamingStrategyTest extends TestCase {

    public void testGetTemporaryName() {
        DistinguishedName originalName = new DistinguishedName(
                "cn=john doe, ou=somecompany, c=SE");
        DifferentSubtreeTempEntryRenamingStrategy tested = new DifferentSubtreeTempEntryRenamingStrategy(
                new DistinguishedName("ou=tempEntries"));

        int nextSequenceNo = tested.getNextSequenceNo();

        // Perform test
        Name result = tested.getTemporaryName(originalName);

        // Verify result
        assertEquals("cn=john doe" + nextSequenceNo + ", ou=tempEntries",
                result.toString());
    }

}
