/*
 * Copyright 2005-2010 the original author or authors.
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

import java.util.Arrays;

import javax.naming.ldap.Control;

import org.easymock.AbstractMatcher;

public class ControlArrayMatcher extends AbstractMatcher {
    protected boolean argumentMatches(Object expected, Object actual) {
        Control[] expectedControls = (Control[]) expected;
        Control[] actualControls = (Control[]) actual;
        if (expectedControls.length != actualControls.length) {
            return false;
        }
        for (int i = 0; i < actualControls.length; i++) {
            Control actualControl = actualControls[i];
            Control expectedControl = expectedControls[i];
            if (actualControl == null && expectedControl != null) {
                return false;
            }
            if (actualControl != null && expectedControl == null) {
                return false;
            }
            if (actualControl == null && expectedControl == null) {
                continue;
            }
            if (!actualControl.getClass().equals(expectedControl.getClass())) {
                return false;
            }
        }
        
        return true;
    }
    
    protected String argumentToString(Object argument) {
        if (argument instanceof Control[]) {
            Control[] control = (Control[]) argument;
            return Arrays.toString(control);
        }
        return super.argumentToString(argument);
    }
}
