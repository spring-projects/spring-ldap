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

import java.util.List;

import javax.naming.NameClassPair;

import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;

import junit.framework.TestCase;

public class CollectingNameClassPairCallbackHandlerTest extends TestCase {

    private CollectingNameClassPairCallbackHandler tested;

    private Object expectedResult;

    private NameClassPair expectedNameClassPair;

    protected void setUp() throws Exception {
        super.setUp();

        expectedResult = new Object();
        expectedNameClassPair = new NameClassPair(null, null);
        tested = new CollectingNameClassPairCallbackHandler() {
            public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
                assertSame(expectedNameClassPair, nameClassPair);
                return expectedResult;
            }
        };
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        expectedNameClassPair = null;
        expectedResult = null;
        tested = null;
    }

    public void testHandleNameClassPair() {
        tested.handleNameClassPair(expectedNameClassPair);
        List result = tested.getList();
        assertEquals(1, result.size());
        assertSame(expectedResult, result.get(0));
    }

}
