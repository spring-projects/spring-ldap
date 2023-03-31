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
package org.springframework.ldap.pool2.factory;

import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.ldap.pool2.AbstractPoolTestCase;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class PooledContextSourceTests extends AbstractPoolTestCase {

	@Test
	public void testProperties() throws Exception {
		final PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxIdlePerKey(500);
		poolConfig.setMinIdlePerKey(100);
		poolConfig.setMaxTotal(5000);
		poolConfig.setMaxTotalPerKey(5);
		poolConfig.setMaxWaitMillis(2000L);
		poolConfig.setMinEvictableIdleTimeMillis(60000L);
		poolConfig.setNumTestsPerEvictionRun(5);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setTestOnCreate(true);
		poolConfig.setTimeBetweenEvictionRunsMillis(120000L);
		poolConfig.setSoftMinEvictableIdleTimeMillis(120000L);
		poolConfig.setBlockWhenExhausted(true);
		poolConfig.setFairness(true);
		poolConfig.setJmxEnabled(true);
		poolConfig.setJmxNameBase("test");
		poolConfig.setJmxNamePrefix("pool");
		poolConfig.setLifo(true);

		final PooledContextSource PooledContextSource = new PooledContextSource(poolConfig);

		try {
			PooledContextSource.setContextSource(null);
			fail("PooledContextSource.setBaseName should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}
		PooledContextSource.setContextSource(contextSourceMock);
		final ContextSource contextSource2 = PooledContextSource.getContextSource();
		assertThat(contextSource2).isEqualTo(contextSourceMock);

		try {
			PooledContextSource.setDirContextValidator(null);
			fail("PooledContextSource.setDirContextValidator should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}
		PooledContextSource.setDirContextValidator(dirContextValidatorMock);
		final DirContextValidator dirContextValidator2 = PooledContextSource.getDirContextValidator();
		assertThat(dirContextValidator2).isEqualTo(dirContextValidatorMock);

		final int numActive = PooledContextSource.getNumActive();
		assertThat(numActive).isEqualTo(0);

		final int numIdle = PooledContextSource.getNumIdle();
		assertThat(numIdle).isEqualTo(0);
	}

	@Test
	public void testGetReadOnlyContextPool() throws Exception {
		DirContext secondDirContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadOnlyContext()).thenReturn(dirContextMock, secondDirContextMock);

		final PooledContextSource PooledContextSource = new PooledContextSource(null);
		PooledContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext readOnlyContext1 = PooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext1).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = PooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext2).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = PooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext3).isEqualTo(secondDirContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(2);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(2);
	}

	@Test
	public void testGetReadWriteContextPool() throws Exception {
		DirContext secondDirContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadWriteContext()).thenReturn(dirContextMock, secondDirContextMock);

		final PooledContextSource PooledContextSource = new PooledContextSource(null);
		PooledContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext readOnlyContext1 = PooledContextSource.getReadWriteContext();
		assertThat(readOnlyContext1).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = PooledContextSource.getReadWriteContext();
		assertThat(readOnlyContext2).isEqualTo(dirContextMock); // Order reversed because
																// the 'wrapper' has the
																// needed equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = PooledContextSource.getReadWriteContext();
		assertThat(readOnlyContext3).isEqualTo(secondDirContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(PooledContextSource.getNumActive()).isEqualTo(2);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(PooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(PooledContextSource.getNumIdle()).isEqualTo(2);
	}

	@Test
	public void testGetContextException() throws Exception {
		when(contextSourceMock.getReadWriteContext()).thenThrow(new RuntimeException("Problem getting context"));

		final PooledContextSource PooledContextSource = new PooledContextSource(null);
		PooledContextSource.setContextSource(contextSourceMock);

		try {
			PooledContextSource.getReadWriteContext();
			fail("PooledContextSource.getReadWriteContext should have thrown DataAccessResourceFailureException");
		}
		catch (DataAccessResourceFailureException darfe) {
			// Expected
		}
	}

	@Test
	public void testGetReadOnlyLdapContext() throws Exception {
		LdapContext secondLdapContextMock = mock(LdapContext.class);

		when(contextSourceMock.getReadOnlyContext()).thenReturn(ldapContextMock, secondLdapContextMock);

		final PooledContextSource pooledContextSource = new PooledContextSource(null);
		pooledContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext readOnlyContext1 = pooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext1).isEqualTo(ldapContextMock); // Order reversed because
																	// the 'wrapper' has
																	// the needed equals
																	// logic
		assertThat(pooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(0);

		// Close the context
		readOnlyContext1.close();
		assertThat(pooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(1);

		// Get the context again
		final DirContext readOnlyContext2 = pooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext2).isEqualTo(ldapContextMock); // Order reversed because
																	// the 'wrapper' has
																	// the needed equals
																	// logic
		assertThat(pooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(0);

		// Get a new context
		final DirContext readOnlyContext3 = pooledContextSource.getReadOnlyContext();
		assertThat(readOnlyContext3).isEqualTo(secondLdapContextMock); // Order reversed
																		// because the
																		// 'wrapper' has
																		// the needed
																		// equals logic
		assertThat(pooledContextSource.getNumActive()).isEqualTo(2);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(0);

		// Close context
		readOnlyContext2.close();
		assertThat(pooledContextSource.getNumActive()).isEqualTo(1);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(1);

		// Close context
		readOnlyContext3.close();
		assertThat(pooledContextSource.getNumActive()).isEqualTo(0);
		assertThat(pooledContextSource.getNumIdle()).isEqualTo(2);
	}

}
