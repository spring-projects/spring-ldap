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

import javax.naming.ldap.Control;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.AbstractMatcher;

/**
 * Custom argument matcher that matches javax.naming.ldap.Control objects.
 * 
 * @author Adam Skogman
 */
public class RequestControlMatcher extends AbstractMatcher {

    /**
     * @see org.easymock.AbstractMatcher#argumentMatches(java.lang.Object,
     *      java.lang.Object)
     */
    protected boolean argumentMatches(Object expected, Object actual) {

        // null checks
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        // Both params should be arrays
        Object[] expArray = (Object[]) expected;
        Object[] actArray = (Object[]) actual;

        if (expArray.length != actArray.length) {
            return false;
        }

        // Compary each object
        for (int i = 0; i < expArray.length; i++) {
            if (!controlMatches((Control) expArray[i], (Control) actArray[i])) {
                return false;
            }
        }
        return true;

    }

    private boolean controlMatches(Control expected, Control actual) {

        // Compare SortControl
        return StringUtils.equals(expected.getID(), actual.getID())
                && expected.isCritical() == actual.isCritical()
                && ArrayUtils.isEquals(expected.getEncodedValue(), actual
                        .getEncodedValue());
    }
}
