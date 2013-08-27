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

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.AbstractPoolTestCase;
import org.springframework.ldap.pool.validation.DirContextValidator;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 */
public class PoolingContextSourceTest extends AbstractPoolTestCase {

    @Test
    public void testProperties() throws Exception {
        final PoolingContextSource poolingContextSource = new PoolingContextSource();

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
        
        poolingContextSource.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
        final byte whenExhaustedAction = poolingContextSource.getWhenExhaustedAction();
        assertEquals(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK, whenExhaustedAction);
        
        final int numActive = poolingContextSource.getNumActive();
        assertEquals(0, numActive);
        
        final int numIdle = poolingContextSource.getNumIdle();
        assertEquals(0, numIdle);
    }

    @Test
    public void testGetReadOnlyContextPool() throws Exception {
        DirContext secondDirContextMock = mock(DirContext.class);
        
        when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock, secondDirContextMock);

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

    @Test
    public void testGetReadWriteContextPool() throws Exception {
        DirContext secondDirContextMock = mock(DirContext.class);
        
        when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock, secondDirContextMock);

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

    @Test
    public void testGetContextException() throws Exception {
        when(contextSourceMock.getReadWriteContext())
                .thenThrow(new RuntimeException("Problem getting context"));

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

    @Test
    public void testGetReadOnlyLdapContext() throws Exception {
        LdapContext secondLdapContextMock = mock(LdapContext.class);

        when(contextSourceMock.getReadOnlyContext()).thenReturn(ldapContextMock, secondLdapContextMock);

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
    }
}
