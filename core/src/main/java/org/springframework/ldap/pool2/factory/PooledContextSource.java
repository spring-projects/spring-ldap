/*
 * Copyright 2005-2015 the original author or authors.
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

import java.util.Collection;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.DelegatingBaseLdapPathContextSourceSupport;
import org.springframework.ldap.pool2.DelegatingDirContext;
import org.springframework.ldap.pool2.DelegatingLdapContext;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.validation.DirContextValidator;

/**
 * A {@link ContextSource} implementation that wraps an object pool and another
 * {@link ContextSource}. {@link DirContext}s are retrieved from the pool which maintains
 * them.
 *
 * NOTE: This implementation is based on apache commons-pool2. <br>
 * <br>
 * Configuration:
 * <table border="1" summary="Configuration">
 * <tr>
 * <th align="left">Property</th>
 * <th align="left">Description</th>
 * <th align="left">Required</th>
 * <th align="left">Default</th>
 * </tr>
 * <tr>
 * <td valign="top">contextSource</td>
 * <td valign="top">The {@link ContextSource} to get {@link DirContext}s from for adding
 * to the pool.</td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td valign="top">dirContextValidator</td>
 * <td valign="top">The
 * {@link org.springframework.ldap.pool2.validation.DirContextValidator} to use for
 * validating {@link DirContext}s. Required if any of the test/validate options are
 * enabled.</td>
 * <td valign="top">No</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td valign="top">poolConfig</td>
 * <td valign="top">The {@link PoolConfig} to configure the pool.</td>
 * <td valign="top">No</td>
 * <td valign="top">null</td>
 * </tr>
 * </table>
 *
 * @since 2.0
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class PooledContextSource extends DelegatingBaseLdapPathContextSourceSupport
		implements ContextSource, DisposableBean {

	/**
	 * The logger for this class and sub-classes
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final GenericKeyedObjectPool<Object, Object> keyedObjectPool;

	private final DirContextPooledObjectFactory dirContextPooledObjectFactory;

	private PoolConfig poolConfig;

	/**
	 * Creates a new pooling context source, setting up the DirContext object factory and
	 * generic keyed object pool.
	 */
	public PooledContextSource(PoolConfig poolConfig) {
		this.dirContextPooledObjectFactory = new DirContextPooledObjectFactory();
		if (poolConfig != null) {
			this.poolConfig = poolConfig;
			GenericKeyedObjectPoolConfig objectPoolConfig = getConfig(poolConfig);
			this.keyedObjectPool = new GenericKeyedObjectPool<Object, Object>(this.dirContextPooledObjectFactory,
					objectPoolConfig);
		}
		else {
			this.keyedObjectPool = new GenericKeyedObjectPool<Object, Object>(this.dirContextPooledObjectFactory);
		}
	}

	// ***** Pool Property Configuration *****//

	/**
	 * @return the poolConfig
	 */
	public PoolConfig getPoolConfig() {
		return this.poolConfig;
	}

	/**
	 * @see GenericKeyedObjectPool#getNumIdle()
	 */
	public int getNumIdle() {
		return this.keyedObjectPool.getNumIdle();
	}

	/**
	 * @see GenericKeyedObjectPool#getNumIdle(Object)
	 */
	public int getNumIdleRead() {
		return this.keyedObjectPool.getNumIdle(DirContextType.READ_ONLY);
	}

	/**
	 * @see GenericKeyedObjectPool#getNumIdle(Object)
	 */
	public int getNumIdleWrite() {
		return this.keyedObjectPool.getNumIdle(DirContextType.READ_WRITE);
	}

	/**
	 * @see GenericKeyedObjectPool#getNumActive()
	 */
	public int getNumActive() {
		return this.keyedObjectPool.getNumActive();
	}

	/**
	 * @see GenericKeyedObjectPool#getNumActive(Object)
	 */
	public int getNumActiveRead() {
		return this.keyedObjectPool.getNumActive(DirContextType.READ_ONLY);
	}

	/**
	 * @see GenericKeyedObjectPool#getNumActive(Object)
	 */
	public int getNumActiveWrite() {
		return this.keyedObjectPool.getNumActive(DirContextType.READ_WRITE);
	}

	/**
	 * @see GenericKeyedObjectPool#getNumWaiters()
	 */
	public int getNumWaiters() {
		return this.keyedObjectPool.getNumWaiters();
	}

	// ***** Object Factory Property Configuration *****//

	/**
	 * @return the contextSource
	 */
	public ContextSource getContextSource() {
		return this.dirContextPooledObjectFactory.getContextSource();
	}

	/**
	 * @return the dirContextValidator
	 */
	public DirContextValidator getDirContextValidator() {
		return this.dirContextPooledObjectFactory.getDirContextValidator();
	}

	/**
	 * @param contextSource the contextSource to set Required
	 */
	public void setContextSource(ContextSource contextSource) {
		this.dirContextPooledObjectFactory.setContextSource(contextSource);
	}

	/**
	 * @param dirContextValidator the dirContextValidator to set Required
	 */
	public void setDirContextValidator(DirContextValidator dirContextValidator) {
		this.dirContextPooledObjectFactory.setDirContextValidator(dirContextValidator);
	}

	/**
	 * Configure the exception classes that are to be interpreted as no-transient with
	 * regards to eager context invalidation. If one of the configured exceptions (or
	 * subclasses of them) is thrown by any method on a pooled DirContext, that instance
	 * will immediately be marked as invalid without any additional testing (i.e.
	 * testOnReturn). This allows for more efficient management of dead connections.
	 * Default is {@link javax.naming.CommunicationException}.
	 * @param nonTransientExceptions the exception classes that should be interpreted as
	 * non-transient with regards to eager invalidation.
	 * @since 2.0
	 */
	public void setNonTransientExceptions(Collection<Class<? extends Throwable>> nonTransientExceptions) {
		this.dirContextPooledObjectFactory.setNonTransientExceptions(nonTransientExceptions);
	}

	// ***** DisposableBean interface methods *****//

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		try {
			this.keyedObjectPool.close();
		}
		catch (Exception ex) {
			this.logger.warn("An exception occurred while closing the underlying pool.", ex);
		}
	}

	@Override
	protected ContextSource getTarget() {
		return getContextSource();
	}

	// ***** ContextSource interface methods *****//

	@Override
	public DirContext getReadOnlyContext() {
		return this.getContext(DirContextType.READ_ONLY);
	}

	@Override
	public DirContext getReadWriteContext() {
		return this.getContext(DirContextType.READ_WRITE);
	}

	/**
	 * Gets a DirContext of the specified type from the keyed object pool.
	 * @param dirContextType The type of context to return.
	 * @return A wrapped DirContext of the specified type.
	 * @throws DataAccessResourceFailureException If retrieving the object from the pool
	 * throws an exception
	 */
	protected DirContext getContext(DirContextType dirContextType) {
		final DirContext dirContext;
		try {
			dirContext = (DirContext) this.keyedObjectPool.borrowObject(dirContextType);
		}
		catch (Exception ex) {
			throw new DataAccessResourceFailureException("Failed to borrow DirContext from pool.", ex);
		}

		if (dirContext instanceof LdapContext) {
			return new DelegatingLdapContext(this.keyedObjectPool, (LdapContext) dirContext, dirContextType);
		}

		return new DelegatingDirContext(this.keyedObjectPool, dirContext, dirContextType);
	}

	@Override
	public DirContext getContext(String principal, String credentials) {
		throw new UnsupportedOperationException("Not supported for this implementation");
	}

	private GenericKeyedObjectPoolConfig getConfig(PoolConfig poolConfig) {
		GenericKeyedObjectPoolConfig objectPoolConfig = new GenericKeyedObjectPoolConfig();

		objectPoolConfig.setMaxTotalPerKey(poolConfig.getMaxTotalPerKey());
		objectPoolConfig.setMaxTotal(poolConfig.getMaxTotal());

		objectPoolConfig.setMaxIdlePerKey(poolConfig.getMaxIdlePerKey());
		objectPoolConfig.setMinIdlePerKey(poolConfig.getMinIdlePerKey());

		objectPoolConfig.setTestWhileIdle(poolConfig.isTestWhileIdle());
		objectPoolConfig.setTestOnReturn(poolConfig.isTestOnReturn());
		objectPoolConfig.setTestOnCreate(poolConfig.isTestOnCreate());
		objectPoolConfig.setTestOnBorrow(poolConfig.isTestOnBorrow());

		objectPoolConfig.setTimeBetweenEvictionRunsMillis(poolConfig.getTimeBetweenEvictionRunsMillis());
		objectPoolConfig.setEvictionPolicyClassName(poolConfig.getEvictionPolicyClassName());
		objectPoolConfig.setMinEvictableIdleTimeMillis(poolConfig.getMinEvictableIdleTimeMillis());
		objectPoolConfig.setNumTestsPerEvictionRun(poolConfig.getNumTestsPerEvictionRun());
		objectPoolConfig.setSoftMinEvictableIdleTimeMillis(poolConfig.getSoftMinEvictableIdleTimeMillis());

		objectPoolConfig.setJmxEnabled(poolConfig.isJmxEnabled());
		objectPoolConfig.setJmxNameBase(poolConfig.getJmxNameBase());
		objectPoolConfig.setJmxNamePrefix(poolConfig.getJmxNamePrefix());

		objectPoolConfig.setMaxWaitMillis(poolConfig.getMaxWaitMillis());

		objectPoolConfig.setFairness(poolConfig.isFairness());
		objectPoolConfig.setBlockWhenExhausted(poolConfig.isBlockWhenExhausted());
		objectPoolConfig.setLifo(poolConfig.isLifo());

		return objectPoolConfig;
	}

}
