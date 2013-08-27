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
package org.springframework.ldap.pool.factory;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.AbstractPoolTestCase;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;

import javax.naming.directory.DirContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 */
public class DirContextPoolableObjectFactoryTest extends AbstractPoolTestCase {

    @Test
    public void testProperties() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

        try {
            objectFactory.setContextSource(null);
            fail("DirContextPoolableObjectFactory.setContextSource should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        
        objectFactory.setContextSource(contextSourceMock);
        final ContextSource contextSource2 = objectFactory.getContextSource();
        assertEquals(contextSourceMock, contextSource2);
        
        
        try {
            objectFactory.setDirContextValidator(null);
            fail("DirContextPoolableObjectFactory.setDirContextValidator should have thrown an IllegalArgumentException");
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
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

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
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        DirContext readOnlyContextMock = mock(DirContext.class);

        when(contextSourceMock.getReadOnlyContext()).thenReturn(readOnlyContextMock);
        objectFactory.setContextSource(contextSourceMock);

        final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
        assertEquals(readOnlyContextMock, createdDirContext);
    }

    @Test
    public void testMakeObjectReadWrite() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        DirContext readWriteContextMock = mock(DirContext.class);

        when(contextSourceMock.getReadWriteContext()).thenReturn(readWriteContextMock);
        objectFactory.setContextSource(contextSourceMock);


        final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);
        assertEquals(readWriteContextMock, createdDirContext);
    }

    @Test
    public void testValidateObjectAssertions() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

        try {
            objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        objectFactory.setDirContextValidator(dirContextValidatorMock);
        
        try {
            objectFactory.validateObject(null, dirContextMock);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            objectFactory.validateObject(new Object(), dirContextMock);
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
            objectFactory.validateObject(DirContextType.READ_ONLY, new Object());
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

        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        objectFactory.setDirContextValidator(dirContextValidatorMock);
        
        final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
        assertTrue(valid);
        
        //Check exception in validator
        DirContextValidator secondDirContextValidatorMock = mock(DirContextValidator.class);

        when(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock))
                .thenThrow(new RuntimeException("Failed to validate"));
        objectFactory.setDirContextValidator(secondDirContextValidatorMock);
        
        final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
        assertFalse(valid2);
    }

    @Test
    public void testDestroyObjectAssertions() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        try {
            objectFactory.destroyObject(DirContextType.READ_ONLY, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        
        try {
            objectFactory.validateObject(DirContextType.READ_ONLY, new Object());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testDestroyObject() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        objectFactory.destroyObject(DirContextType.READ_ONLY, dirContextMock);
        
        DirContext throwingDirContextMock = Mockito.mock(DirContext.class);

        doThrow(new RuntimeException("Failed to close"))
                .when(throwingDirContextMock).close();

        objectFactory.destroyObject(DirContextType.READ_ONLY, throwingDirContextMock);
        verify(dirContextMock).close();
    }
}
