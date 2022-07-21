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

package org.springframework.ldap.odm.core.impl;

import org.springframework.util.Assert;

// A case independent String wrapper.
/* package */ final class CaseIgnoreString implements Comparable<CaseIgnoreString> {
    private final String string;
    private final int hashCode;  
    
    public CaseIgnoreString(String string) {
        Assert.notNull(string, "string must not be null");
        this.string = string;
        hashCode = string.toUpperCase().hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof CaseIgnoreString &&
            ((CaseIgnoreString)other).string.equalsIgnoreCase(string);
    }
    
    public int hashCode() { 
        return hashCode;
    }

    public int compareTo(CaseIgnoreString other) {
        CaseIgnoreString cis = other;
        return String.CASE_INSENSITIVE_ORDER.compare(string, cis.string);
    }

    public String toString() {
        return string;
    }
}
