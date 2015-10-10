/*
 * Copyright 2005-2015 the original author or authors.
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
package org.springframework.ldap.pool2.factory;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.ldap.pool2.AbstractPoolTestCase;

import javax.naming.directory.DirContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class DirContextPooledObjectFactoryTest extends AbstractPoolTestCase {

    @Test
    public void testProperties() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

        try {
            objectFactory.setContextSource(null);
            fail("DirContextPooledObjectFactory.setContextSource should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        
        objectFactory.setContextSource(contextSourceMock);
        final ContextSource contextSource2 = objectFactory.getContextSource();
        assertEquals(contextSourceMock, contextSource2);
        
        
        try {
            objectFactory.setDirContextValidator(null);
            fail("DirContextPooledObjectFactory.setDirContextValidator should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        
        objectFactory.setDirContextValidator(dirContextValidatorMock);
        final DirContextValidator dirContextValidator2 = objectFactory.getDirContextValidator();
        assertEquals(dirContextValidatorMock, dirContextValidator2);
    }

    @Test
    public void testMakeObjectAssertions() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

        try {
            objectFactory.makeObject(DirContextType.READ_ONLY);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        objectFactory.setContextSource(contextSourceMock);
        
        try {
            objectFactory.makeObject(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testMakeObjectReadOnly() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();
        
        DirContext readOnlyContextMock = mock(DirContext.class);

        when(contextSourceMock.getReadOnlyContext()).thenReturn(readOnlyContextMock);
        objectFactory.setContextSource(contextSourceMock);

        final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
        assertEquals(readOnlyContextMock, Whitebox.getInternalState(invocationHandler, "target"));
    }

    @Test
    public void testMakeObjectReadWrite() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();
        
        DirContext readWriteContextMock = mock(DirContext.class);

        when(contextSourceMock.getReadWriteContext()).thenReturn(readWriteContextMock);
        objectFactory.setContextSource(contextSourceMock);

        final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);

        InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
        assertEquals(readWriteContextMock, Whitebox.getInternalState(invocationHandler, "target"));
    }

    @Test
    public void testValidateObjectAssertions() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

        try {
            PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
            objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        objectFactory.setDirContextValidator(dirContextValidatorMock);
        
        try {
            PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
            objectFactory.validateObject(null, pooledObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
            objectFactory.validateObject(new Object(), pooledObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            objectFactory.validateObject(DirContextType.READ_ONLY, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            PooledObject pooledObject = new DefaultPooledObject(new Object());
            objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testValidateObject() throws Exception {
        when(dirContextValidatorMock
                .validateDirContext(DirContextType.READ_ONLY, dirContextMock))
                .thenReturn(true);

        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();
        objectFactory.setDirContextValidator(dirContextValidatorMock);

        PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
        final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
        assertTrue(valid);
        
        //Check exception in validator
        DirContextValidator secondDirContextValidatorMock = mock(DirContextValidator.class);

        when(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock))
                .thenThrow(new RuntimeException("Failed to validate"));
        objectFactory.setDirContextValidator(secondDirContextValidatorMock);

        final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
        assertFalse(valid2);
    }

    @Test
    public void testDestroyObjectAssertions() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();
        
        try {
            objectFactory.destroyObject(DirContextType.READ_ONLY, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            PooledObject pooledObject = new DefaultPooledObject(new Object());
            objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testDestroyObject() throws Exception {
        final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

        PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
        objectFactory.destroyObject(DirContextType.READ_ONLY, pooledObject);
        
        DirContext throwingDirContextMock = Mockito.mock(DirContext.class);

        doThrow(new RuntimeException("Failed to close"))
                .when(throwingDirContextMock).close();

        pooledObject = new DefaultPooledObject(throwingDirContextMock);
        objectFactory.destroyObject(DirContextType.READ_ONLY, pooledObject);
        verify(dirContextMock).close();
    }
}
