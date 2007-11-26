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
package org.springframework.ldap.pool;

import javax.naming.directory.DirContext;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.validation.DirContextValidator;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DirContextPoolableObjectFactoryTest extends AbstractPoolTestCase {

    public void testProperties() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

        replay();
        
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
        verify();
    }
    
    public void testMakeObjectAssertions() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

        replay();
        
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
        
        verify();
    }
    
    public void testMakeObjectReadOnly() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        MockControl readOnlyContextControl = MockControl.createControl(DirContext.class);
        DirContext readOnlyContextMock = (DirContext) readOnlyContextControl.getMock();

        readOnlyContextControl.replay();
        
        contextSourceControl.expectAndReturn(contextSourceMock.getReadOnlyContext(), readOnlyContextMock, 1);
        objectFactory.setContextSource(contextSourceMock);

        replay();

        final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
        
        readOnlyContextControl.verify();
        verify();
        assertEquals(readOnlyContextMock, createdDirContext);
    }
    
    public void testMakeObjectReadWrite() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        MockControl readWriteContextControl = MockControl.createControl(DirContext.class);
        DirContext readWriteContextMock = (DirContext) readWriteContextControl.getMock();

        readWriteContextControl.replay();
        
        contextSourceControl.expectAndReturn(contextSourceMock.getReadWriteContext(), readWriteContextMock, 1);
        objectFactory.setContextSource(contextSourceMock);

        replay();
        
        final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);
        
        readWriteContextControl.verify();
        verify();
        assertEquals(readWriteContextMock, createdDirContext);
    }

    public void testValidateObjectAssertions() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();

        replay();
        
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
        
        verify();
    }
    
    public void testValidateObject() throws Exception {
        dirContextValidatorControl.expectAndReturn(dirContextValidatorMock
                .validateDirContext(DirContextType.READ_ONLY, dirContextMock),
                true);

        replay();
        
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        objectFactory.setDirContextValidator(dirContextValidatorMock);
        
        final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
        assertTrue(valid);
        
        //Check exception in validator
        MockControl secondDirContextValidatorControl = MockControl.createControl(DirContextValidator.class);
        DirContextValidator secondDirContextValidatorMock = (DirContextValidator) secondDirContextValidatorControl.getMock();

        secondDirContextValidatorControl.expectAndThrow(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock), new RuntimeException("Failed to validate"));
        secondDirContextValidatorControl.replay();
        objectFactory.setDirContextValidator(secondDirContextValidatorMock);
        
        final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
        assertFalse(valid2);

        secondDirContextValidatorControl.verify();
        verify();
    }
    
    public void testDestroyObjectAssertions() throws Exception {
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        replay();
        
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
        
        verify();
    }
    
    public void testDestroyObject() throws Exception {
        dirContextMock.close();
        dirContextControl.setVoidCallable(1);
        
        replay();
        
        final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory();
        
        objectFactory.destroyObject(DirContextType.READ_ONLY, dirContextMock);
        
        MockControl throwingDirContextControl = MockControl.createControl(DirContext.class);
        DirContext throwingDirContextMock = (DirContext) throwingDirContextControl.getMock();

        throwingDirContextMock.close();
        throwingDirContextControl.setThrowable(new RuntimeException("Failed to close"));
        throwingDirContextControl.replay();
        
        objectFactory.destroyObject(DirContextType.READ_ONLY, throwingDirContextMock);
        throwingDirContextControl.verify();
        verify();
    }
}
