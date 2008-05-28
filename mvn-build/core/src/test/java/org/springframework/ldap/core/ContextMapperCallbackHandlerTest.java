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

import javax.naming.Binding;

import org.easymock.MockControl;

import junit.framework.TestCase;

public class ContextMapperCallbackHandlerTest extends TestCase {

    private MockControl mapperControl;

    private ContextMapper mapperMock;

    private ContextMapperCallbackHandler tested;

    protected void setUp() throws Exception {
        super.setUp();

        mapperControl = MockControl.createControl(ContextMapper.class);
        mapperMock = (ContextMapper) mapperControl.getMock();
        tested = new ContextMapperCallbackHandler(mapperMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        mapperControl = null;
        mapperMock = null;
        tested = null;
    }

    public void testConstructorWithEmptyArgument() {
        try {
            new ContextMapperCallbackHandler(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testGetObjectFromNameClassPair() {
        Object expectedObject = "object";
        Object expectedResult = "result";
        Binding expectedBinding = new Binding("some name", expectedObject);

        mapperControl.expectAndReturn(
                mapperMock.mapFromContext(expectedObject), expectedResult);

        mapperControl.replay();

        Object actualResult = tested
                .getObjectFromNameClassPair(expectedBinding);

        mapperControl.verify();

        assertEquals(expectedResult, actualResult);
    }

    public void testGetObjectFromNameClassPairObjectRetrievalException() {
        Binding expectedBinding = new Binding("some name", null);

        mapperControl.replay();

        try {
            tested.getObjectFromNameClassPair(expectedBinding);
            fail("ObjectRetrievalException expected");
        } catch (ObjectRetrievalException expected) {
            assertTrue(true);
        }
        mapperControl.verify();
    }
}
