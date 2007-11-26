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
import javax.naming.ldap.LdapContext;

import org.easymock.MockControl;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.PoolingContextSource.WhenExhaustedAction;
import org.springframework.ldap.pool.validation.DirContextValidator;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class PoolingContextSourceTest extends AbstractPoolTestCase {
    
    public void testProperties() throws Exception {
        final PoolingContextSource poolingContextSource = new PoolingContextSource();
        
        replay();
        
        try {
            poolingContextSource.setContextSource(null);
            fail("PoolingContextSource.setBaseName should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        poolingContextSource.setContextSource(contextSourceMock);
        final ContextSource contextSource2 = poolingContextSource.getContextSource();
        assertEquals(contextSourceMock, contextSource2);
        
        try {
            poolingContextSource.setDirContextValidator(null);
            fail("PoolingContextSource.setDirContextValidator should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        poolingContextSource.setDirContextValidator(dirContextValidatorMock);
        final DirContextValidator dirContextValidator2 = poolingContextSource.getDirContextValidator();
        assertEquals(dirContextValidatorMock, dirContextValidator2);
        
        poolingContextSource.setMaxActive(1000);
        final int maxActive = poolingContextSource.getMaxActive();
        assertEquals(1000, maxActive);
        
        poolingContextSource.setMaxIdle(500);
        final int maxIdle = poolingContextSource.getMaxIdle();
        assertEquals(500, maxIdle);
        
        poolingContextSource.setMaxTotal(5000);
        final int maxTotal = poolingContextSource.getMaxTotal();
        assertEquals(5000, maxTotal);
        
        poolingContextSource.setMaxWait(2000L);
        final long maxWait = poolingContextSource.getMaxWait();
        assertEquals(2000L, maxWait);
        
        poolingContextSource.setMinEvictableIdleTimeMillis(60000L);
        final long minEvictableIdleTimeMillis = poolingContextSource.getMinEvictableIdleTimeMillis();
        assertEquals(60000L, minEvictableIdleTimeMillis);
        
        poolingContextSource.setMinIdle(100);
        final int minIdle = poolingContextSource.getMinIdle();
        assertEquals(100, minIdle);
        
        poolingContextSource.setNumTestsPerEvictionRun(5);
        final int numTestsPerEvictionRun = poolingContextSource.getNumTestsPerEvictionRun();
        assertEquals(5, numTestsPerEvictionRun);
        
        poolingContextSource.setTestOnBorrow(true);
        final boolean testOnBorrow = poolingContextSource.getTestOnBorrow();
        assertEquals(true, testOnBorrow);
        
        poolingContextSource.setTestOnReturn(true);
        final boolean testOnReturn = poolingContextSource.getTestOnReturn();
        assertEquals(true, testOnReturn);
        
        poolingContextSource.setTestWhileIdle(true);
        final boolean testWhileIdle = poolingContextSource.getTestWhileIdle();
        assertEquals(true, testWhileIdle);
        
        poolingContextSource.setTimeBetweenEvictionRunsMillis(120000L);
        final long timeBetweenEvictionRunsMillis = poolingContextSource.getTimeBetweenEvictionRunsMillis();
        assertEquals(120000L, timeBetweenEvictionRunsMillis);
        
        try {
            poolingContextSource.setWhenExhaustedAction(null);
            fail("PoolingContextSource.setWhenExhaustedAction should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Expected
        }
        poolingContextSource.setWhenExhaustedAction(PoolingContextSource.WhenExhaustedAction.BLOCK);
        final WhenExhaustedAction whenExhaustedAction = poolingContextSource.getWhenExhaustedAction();
        assertEquals(PoolingContextSource.WhenExhaustedAction.BLOCK, whenExhaustedAction);
        
        assertNull(PoolingContextSource.WhenExhaustedAction.getActionForId(Byte.MAX_VALUE));
        
        final int numActive = poolingContextSource.getNumActive();
        assertEquals(0, numActive);
        
        final int numIdle = poolingContextSource.getNumIdle();
        assertEquals(0, numIdle);
    }

    public void testGetReadOnlyContextPool() throws Exception {
        MockControl secondDirContextControl = MockControl.createControl(DirContext.class);
        DirContext secondDirContextMock = (DirContext) secondDirContextControl.getMock();
        
        contextSourceControl.expectAndReturn(contextSourceMock.getReadOnlyContext(), dirContextMock);
        contextSourceControl.expectAndReturn(contextSourceMock.getReadOnlyContext(), secondDirContextMock);
        
        replay();
        
        final PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setContextSource(contextSourceMock);

        //Get a context
        final DirContext readOnlyContext1 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext1, dirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Close the context
        readOnlyContext1.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Get the context again
        final DirContext readOnlyContext2 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext2, dirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Get a new context
        final DirContext readOnlyContext3 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext3, secondDirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(2, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());

        //Close context
        readOnlyContext2.close();
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Close context
        readOnlyContext3.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(2, poolingContextSource.getNumIdle());
    }
    
    public void testGetReadWriteContextPool() throws Exception {
        MockControl secondDirContextControl = MockControl.createControl(DirContext.class);
        DirContext secondDirContextMock = (DirContext) secondDirContextControl.getMock();
        
        contextSourceControl.expectAndReturn(contextSourceMock.getReadWriteContext(), dirContextMock);
        contextSourceControl.expectAndReturn(contextSourceMock.getReadWriteContext(), secondDirContextMock);
        
        replay();
        
        final PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setContextSource(contextSourceMock);

        //Get a context
        final DirContext readOnlyContext1 = poolingContextSource.getReadWriteContext();
        assertEquals(readOnlyContext1, dirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Close the context
        readOnlyContext1.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Get the context again
        final DirContext readOnlyContext2 = poolingContextSource.getReadWriteContext();
        assertEquals(readOnlyContext2, dirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Get a new context
        final DirContext readOnlyContext3 = poolingContextSource.getReadWriteContext();
        assertEquals(readOnlyContext3, secondDirContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(2, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());

        //Close context
        readOnlyContext2.close();
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Close context
        readOnlyContext3.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(2, poolingContextSource.getNumIdle());
    }

    public void testGetContextException() throws Exception {
        contextSourceControl.expectAndThrow(contextSourceMock.getReadWriteContext(), new RuntimeException("Problem getting context"));
        
        replay();
        
        final PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setContextSource(contextSourceMock);

        try {
            poolingContextSource.getReadWriteContext();
            fail("PoolingContextSource.getReadWriteContext should have thrown DataAccessResourceFailureException");
        }
        catch (DataAccessResourceFailureException darfe) {
            // Expected
        }
    }

    public void testGetReadOnlyLdapContext() throws Exception {
        MockControl secondLdapContextControl = MockControl.createControl(LdapContext.class);
        LdapContext secondLdapContextMock = (LdapContext) secondLdapContextControl.getMock();

        secondLdapContextControl.replay();
        
        contextSourceControl.expectAndReturn(contextSourceMock.getReadOnlyContext(), ldapContextMock);
        contextSourceControl.expectAndReturn(contextSourceMock.getReadOnlyContext(), secondLdapContextMock);
        
        replay();
        
        final PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setContextSource(contextSourceMock);

        //Get a context
        final DirContext readOnlyContext1 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext1, ldapContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Close the context
        readOnlyContext1.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Get the context again
        final DirContext readOnlyContext2 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext2, ldapContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());
        
        //Get a new context
        final DirContext readOnlyContext3 = poolingContextSource.getReadOnlyContext();
        assertEquals(readOnlyContext3, secondLdapContextMock); //Order reversed because the 'wrapper' has the needed equals logic
        assertEquals(2, poolingContextSource.getNumActive());
        assertEquals(0, poolingContextSource.getNumIdle());

        //Close context
        readOnlyContext2.close();
        assertEquals(1, poolingContextSource.getNumActive());
        assertEquals(1, poolingContextSource.getNumIdle());
        
        //Close context
        readOnlyContext3.close();
        assertEquals(0, poolingContextSource.getNumActive());
        assertEquals(2, poolingContextSource.getNumIdle());

        secondLdapContextControl.verify();
    }
}
