/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.pool.factory;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.Test;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.AbstractPoolTestCase;
import org.springframework.ldap.pool.validation.DirContextValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 */
public class PoolingContextSourceTests extends AbstractPoolTestCase {

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
		assertThat(contextSource2).isEqualTo(contextSourceMock);

		try {
			poolingContextSource.setDirContextValidator(null);
			fail("PoolingContextSource.setDirContextValidator should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}
		poolingContextSource.setDirContextValidator(dirContextValidatorMock);
		final DirContextValidator dirContextValidator2 = poolingContextSource.getDirContextValidator();
		assertThat(dirContextValidator2).isEqualTo(dirContextValidatorMock);

		poolingContextSource.setMaxActive(1000);
		final int maxActive = poolingContextSource.getMaxActive();
		assertThat(maxActive).isEqualTo(1000);

		poolingContextSource.setMaxIdle(500);
		final int maxIdle = poolingContextSource.getMaxIdle();
		assertThat(maxIdle).isEqualTo(500);

		poolingContextSource.setMaxTotal(5000);
		final int maxTotal = poolingContextSource.getMaxTotal();
		assertThat(maxTotal).isEqualTo(5000);

		poolingContextSource.setMaxWait(2000L);
		final long maxWait = poolingContextSource.getMaxWait();
		assertThat(maxWait).isEqualTo(2000L);

		poolingContextSource.setMinEvictableIdleTimeMillis(60000L);
		final long minEvictableIdleTimeMillis = poolingContextSource.getMinEvictableIdleTimeMillis();
		assertThat(minEvictableIdleTimeMillis).isEqualTo(60000L);

		poolingContextSource.setMinIdle(100);
		final int minIdle = poolingContextSource.getMinIdle();
		assertThat(minIdle).isEqualTo(100);

		poolingContextSource.setNumTestsPerEvictionRun(5);
		final int numTestsPerEvictionRun = poolingContextSource.getNumTestsPerEvictionRun();
		assertThat(numTestsPerEvictionRun).isEqualTo(5);

		poolingContextSource.setTestOnBorrow(true);
		final boolean testOnBorrow = poolingContextSource.getTestOnBorrow();
		assertThat(testOnBorrow).isEqualTo(true);

		poolingContextSource.setTestOnReturn(true);
		final boolean testOnReturn = poolingContextSource.getTestOnReturn();
		assertThat(testOnReturn).isEqualTo(true);

		poolingContextSource.setTestWhileIdle(true);
		final boolean testWhileIdle = poolingContextSource.getTestWhileIdle();
		assertThat(testWhileIdle).isEqualTo(true);

		poolingContextSource.setTimeBetweenEvictionRunsMillis(120000L);
		final long timeBetweenEvictionRunsMillis = poolingContextSource.getTimeBetweenEvictionRunsMillis();
		assertThat(timeBetweenEvictionRunsMillis).isEqualTo(120000L);

		poolingContextSource.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
		final byte whenExhaustedAction = poolingContextSource.getWhenExhaustedAction();
		assertThat(whenExhaustedAction).isEqualTo(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);

		final int numActive = poolingContextSource.getNumActive();
		assertThat(numActive).isEqualTo(0);

		final int numIdle = poolingContextSource.getNumIdle();
		assertThat(numIdle).isEqualTo(0);
	}

	@Test
	public void testGetReadOnlyContextPool() throws Exception {
		DirContext secondDirContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock, secondDirContextMock);

		final PoolingContextSource poolingContextSource = new PoolingContextSource();
		poolingContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext readOnlyContext1 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext1).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext2).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext3).isEqualTo(secondDirContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(2);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(2);
	}

	@Test
	public void testGetReadWriteContextPool() throws Exception {
		DirContext secondDirContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock, secondDirContextMock);

		final PoolingContextSource poolingContextSource = new PoolingContextSource();
		poolingContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext readOnlyContext1 = poolingContextSource.getReadWriteContext();
		assertThat(readOnlyContext1).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = poolingContextSource.getReadWriteContext();
		assertThat(readOnlyContext2).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = poolingContextSource.getReadWriteContext();
		assertThat(readOnlyContext3).isEqualTo(secondDirContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(2);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(2);
	}

	@Test
	public void testGetContextException() throws Exception {
		when(contextSourceMock.getReadWriteContext()).thenThrow(new RuntimeException("Problem getting context"));

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

		// Get a context
		final DirContext readOnlyContext1 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext1).isEqualTo(ldapContextMock); // Order reversed because
																	// the 'wrapper' has
																	// the needed equals
																	// logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext2).isEqualTo(ldapContextMock); // Order reversed because
																	// the 'wrapper' has
																	// the needed equals
																	// logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = poolingContextSource.getReadOnlyContext();
		assertThat(readOnlyContext3).isEqualTo(secondLdapContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(poolingContextSource.getNumActive()).isEqualTo(2);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(1);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(poolingContextSource.getNumActive()).isEqualTo(0);
		assertThat(poolingContextSource.getNumIdle()).isEqualTo(2);
	}

}
