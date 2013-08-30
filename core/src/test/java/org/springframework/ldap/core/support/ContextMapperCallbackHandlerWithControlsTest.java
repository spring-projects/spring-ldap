/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.ObjectRetrievalException;

import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @Ulrik Sandberg
 * @author Mattias Hellborg Arthursson
 */
public class ContextMapperCallbackHandlerWithControlsTest {
    private ContextMapperWithControls mapperMock;

    private ContextMapperCallbackHandlerWithControls tested;

    private static class MyBindingThatHasControls extends Binding implements HasControls {
        private static final long serialVersionUID = 1L;

        public MyBindingThatHasControls(String name, Object obj) {
            super(name, obj);
        }

        public Control[] getControls() throws NamingException {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mapperMock = mock(ContextMapperWithControls.class);
        tested = new ContextMapperCallbackHandlerWithControls(mapperMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyArgument() {
        new ContextMapperCallbackHandlerWithControls<Object>(null);
    }

    @Test
    public void testGetObjectFromNameClassPair() throws NamingException {
        Object expectedObject = "object";
        Object expectedResult = "result";
        Binding expectedBinding = new Binding("some name", expectedObject);

        when(mapperMock.mapFromContext(expectedObject)).thenReturn(expectedResult);

        Object actualResult = tested.getObjectFromNameClassPair(expectedBinding);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetObjectFromNameClassPairImplementingHasControls() throws NamingException {
        Object expectedObject = "object";
        Object expectedResult = "result";
        MyBindingThatHasControls expectedBinding = new MyBindingThatHasControls("some name", expectedObject);

        when(mapperMock.mapFromContextWithControls(expectedObject, expectedBinding)).thenReturn(expectedResult);

        Object actualResult = tested.getObjectFromNameClassPair(expectedBinding);

        assertEquals(expectedResult, actualResult);
    }

    @Test(expected = ObjectRetrievalException.class)
    public void testGetObjectFromNameClassPairObjectRetrievalException() throws NamingException {
        Binding expectedBinding = new Binding("some name", null);

        tested.getObjectFromNameClassPair(expectedBinding);
    }

}
