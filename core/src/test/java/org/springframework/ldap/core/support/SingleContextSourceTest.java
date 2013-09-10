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
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Mattias Hellborg Arthursson
 */
public class SingleContextSourceTest {

    private ContextSource contextSourceMock;
    private DirContext dirContextMock;

    @Before
    public void prepareMocks() {
        contextSourceMock = mock(ContextSource.class);
        dirContextMock = mock(DirContext.class);
    }

    @Test
    public void testDoWithSingleContext() {
        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock);
        verifyNoMoreInteractions(contextSourceMock);

        SingleContextSource.doWithSingleContext(contextSourceMock, new LdapOperationsCallback<Object>() {
            @Override
            public Object doWithLdapOperations(LdapOperations operations) {
                operations.executeReadOnly(new ContextExecutor<Object>() {
                    @Override
                    public Object executeWithContext(DirContext ctx) throws NamingException {
                        Object targetContex = Whitebox.getInternalState(Proxy.getInvocationHandler(ctx), "target");
                        assertSame(dirContextMock, targetContex);
                        return false;
                    }
                });

                // Second operation will have retrieved new DirContext from the SingleContextSource.
                // It should be the same instance.
                operations.executeReadOnly(new ContextExecutor<Object>() {
                    @Override
                    public Object executeWithContext(DirContext ctx) throws NamingException {
                        Object targetContex = Whitebox.getInternalState(Proxy.getInvocationHandler(ctx), "target");
                        assertSame(dirContextMock, targetContex);
                        return false;
                    }
                });

                return null;
            }
        });
    }

}
