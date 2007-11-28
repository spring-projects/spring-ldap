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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.apache.commons.pool.KeyedObjectPool;
import org.easymock.MockControl;

/**
 * @author Eric Dalquist <a
 *         href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingContextTest extends AbstractPoolTestCase {

    public void testConstructorAssertions() {

        replay();

        try {
            new DelegatingContext(null, contextMock, DirContextType.READ_ONLY);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            new DelegatingContext(keyedObjectPoolMock, null,
                    DirContextType.READ_ONLY);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            new DelegatingContext(keyedObjectPoolMock, contextMock, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testHelperMethods() throws Exception {
        keyedObjectPoolMock.returnObject(DirContextType.READ_ONLY, contextMock);
        keyedObjectPoolControl.setVoidCallable(1);

        replay();

        // Wrap the Context once
        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        final Context delegateContext = delegatingContext.getDelegateContext();
        assertEquals(contextMock, delegateContext);

        final Context innerDelegateContext = delegatingContext
                .getInnermostDelegateContext();
        assertEquals(contextMock, innerDelegateContext);

        delegatingContext.assertOpen();

        // Wrap the wrapper
        MockControl secondKeyedObjectPoolControl = MockControl
                .createControl(KeyedObjectPool.class);
        KeyedObjectPool secondKeyedObjectPoolMock = (KeyedObjectPool) secondKeyedObjectPoolControl
                .getMock();
        secondKeyedObjectPoolMock.returnObject(DirContextType.READ_ONLY,
                delegatingContext);
        secondKeyedObjectPoolControl.setVoidCallable(1);
        secondKeyedObjectPoolControl.replay();

        final DelegatingContext delegatingContext2 = new DelegatingContext(
                secondKeyedObjectPoolMock, delegatingContext,
                DirContextType.READ_ONLY);

        final Context delegateContext2 = delegatingContext2
                .getDelegateContext();
        assertEquals(delegatingContext, delegateContext2);

        final Context innerDelegateContext2 = delegatingContext2
                .getInnermostDelegateContext();
        assertEquals(contextMock, innerDelegateContext2);

        delegatingContext2.assertOpen();

        // Close the outer wrapper
        delegatingContext2.close();

        final Context delegateContext2closed = delegatingContext2
                .getDelegateContext();
        assertNull(delegateContext2closed);

        final Context innerDelegateContext2closed = delegatingContext2
                .getInnermostDelegateContext();
        assertNull(innerDelegateContext2closed);

        try {
            delegatingContext2.assertOpen();
            fail("delegatingContext2.assertOpen() should have thrown a NamingException");
        } catch (NamingException ne) {
            // Expected
        }

        // Close the outer wrapper
        delegatingContext.close();

        final Context delegateContextclosed = delegatingContext
                .getDelegateContext();
        assertNull(delegateContextclosed);

        final Context innerDelegateContextclosed = delegatingContext
                .getInnermostDelegateContext();
        assertNull(innerDelegateContextclosed);

        try {
            delegatingContext.assertOpen();
            fail("delegatingContext.assertOpen() should have thrown a NamingException");
        } catch (NamingException ne) {
            // Expected
        }
        
        verify();
        secondKeyedObjectPoolControl.verify();
    }

    public void testObjectMethods() throws Exception {
        keyedObjectPoolMock.returnObject(DirContextType.READ_ONLY, contextMock);
        keyedObjectPoolControl.setVoidCallable(1);

        replay();

        // Wrap the Context once
        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);
        assertEquals("EasyMock for interface javax.naming.Context",
                delegatingContext.toString());
        delegatingContext.hashCode(); // Run it to make sure it doesn't fail

        assertTrue(delegatingContext.equals(delegatingContext));
        assertFalse(delegatingContext.equals(new Object()));

        final DelegatingContext delegatingContext2 = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);
        assertTrue(delegatingContext.equals(delegatingContext2));
        assertTrue(delegatingContext2.equals(delegatingContext));
        assertTrue(delegatingContext.equals(contextMock));

        // Close the contextMock and try again
        delegatingContext.close();

        assertEquals("Context is closed", delegatingContext.toString());
        assertEquals(0, delegatingContext.hashCode()); // Run it to make sure
        // it doesn't fail

        assertTrue(delegatingContext.equals(delegatingContext));
        assertFalse(delegatingContext.equals(new Object()));

        assertFalse(delegatingContext.equals(delegatingContext2));
        assertFalse(delegatingContext2.equals(delegatingContext));
        assertFalse(delegatingContext.equals(contextMock));
        
        verify();
    }

    public void testUnsupportedMethods() throws Exception {

        replay();

        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        try {
            delegatingContext.addToEnvironment(null, null);
            fail("DelegatingContext.addToEnvironment Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        try {
            delegatingContext.createSubcontext((Name) null);
            fail("DelegatingContext.createSubcontext Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        try {
            delegatingContext.createSubcontext((String) null);
            fail("DelegatingContext.createSubcontext Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        try {
            delegatingContext.destroySubcontext((Name) null);
            fail("DelegatingContext.destroySubcontext Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        try {
            delegatingContext.destroySubcontext((String) null);
            fail("DelegatingContext.destroySubcontext Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        try {
            delegatingContext.removeFromEnvironment(null);
            fail("DelegatingContext.removeFromEnvironment Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // Expected
        }
        verify();
    }

    public void testAllMethodsOpened() throws Exception {
        contextControl = MockControl.createNiceControl(Context.class);
        contextMock = (Context) contextControl.getMock();

        replay();

        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        delegatingContext.bind((Name) null, null);
        delegatingContext.bind((String) null, null);
        delegatingContext.composeName((Name) null, (Name) null);
        delegatingContext.composeName((String) null, (String) null);
        delegatingContext.getEnvironment();
        delegatingContext.getNameInNamespace();
        delegatingContext.getNameParser((Name) null);
        delegatingContext.getNameParser((String) null);
        delegatingContext.list((Name) null);
        delegatingContext.list((String) null);
        delegatingContext.listBindings((Name) null);
        delegatingContext.listBindings((String) null);
        delegatingContext.lookup((Name) null);
        delegatingContext.lookup((String) null);
        delegatingContext.lookupLink((Name) null);
        delegatingContext.lookupLink((String) null);
        delegatingContext.rebind((Name) null, null);
        delegatingContext.rebind((String) null, null);
        delegatingContext.rename((Name) null, (Name) null);
        delegatingContext.rename((String) null, (String) null);
        delegatingContext.unbind((Name) null);
        delegatingContext.unbind((String) null);
        
        verify();
    }

    public void testAllMethodsClosed() throws Exception {
        keyedObjectPoolMock.returnObject(DirContextType.READ_ONLY, contextMock);
        keyedObjectPoolControl.setVoidCallable(1);

        replay();

        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        delegatingContext.close();

        try {
            delegatingContext.bind((Name) null, null);
            fail("DelegatingContext.bind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.bind((String) null, null);
            fail("DelegatingContext.bind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.composeName((Name) null, (Name) null);
            fail("DelegatingContext.composeName should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.composeName((String) null, (String) null);
            fail("DelegatingContext.composeName should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.getEnvironment();
            fail("DelegatingContext.getEnvironment should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.getNameInNamespace();
            fail("DelegatingContext.getNameInNamespace should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.getNameParser((Name) null);
            fail("DelegatingContext.getNameParser should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.getNameParser((String) null);
            fail("DelegatingContext.getNameParser should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.list((Name) null);
            fail("DelegatingContext.list should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.list((String) null);
            fail("DelegatingContext.list should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.listBindings((Name) null);
            fail("DelegatingContext.listBindings should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.listBindings((String) null);
            fail("DelegatingContext.listBindings should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.lookup((Name) null);
            fail("DelegatingContext.lookup should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.lookup((String) null);
            fail("DelegatingContext.lookup should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.lookupLink((Name) null);
            fail("DelegatingContext.lookupLink should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.lookupLink((String) null);
            fail("DelegatingContext.lookupLink should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.rebind((Name) null, null);
            fail("DelegatingContext.rebind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.rebind((String) null, null);
            fail("DelegatingContext.rebind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.rename((Name) null, (Name) null);
            fail("DelegatingContext.rename should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.rename((String) null, (String) null);
            fail("DelegatingContext.rename should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.unbind((Name) null);
            fail("DelegatingContext.unbind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        try {
            delegatingContext.unbind((String) null);
            fail("DelegatingContext.unbind should have thrown a NamingException");
        } catch (NamingException ne) {
            //  Expected
        }
        
        verify();
    }

    public void testDoubleClose() throws Exception {
        keyedObjectPoolMock.returnObject(DirContextType.READ_ONLY, contextMock);
        keyedObjectPoolControl.setVoidCallable(1);

        replay();
        
        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        delegatingContext.close();

        // noop close
        delegatingContext.close();
        
        verify();
    }

    public void testPoolExceptionOnClose() throws Exception {
        keyedObjectPoolMock.returnObject(DirContextType.READ_ONLY, contextMock);
        keyedObjectPoolControl.setThrowable(new Exception(
                "Fake Pool returnObject Exception"));

        replay();

        final DelegatingContext delegatingContext = new DelegatingContext(
                keyedObjectPoolMock, contextMock, DirContextType.READ_ONLY);

        try {
            delegatingContext.close();
            fail("DelegatingContext.close should have thrown a NamingException");
        } catch (NamingException ne) {
            // Expected
        }
        
        verify();
    }
}
