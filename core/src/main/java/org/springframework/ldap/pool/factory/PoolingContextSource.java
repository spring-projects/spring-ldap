/*
 * Copyright 2006-present the original author or authors.
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

import java.util.Collection;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.DelegatingBaseLdapPathContextSourceSupport;
import org.springframework.ldap.pool.DelegatingDirContext;
import org.springframework.ldap.pool.DelegatingLdapContext;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;

/**
 * A {@link ContextSource} implementation that wraps an object pool and another
 * {@link ContextSource}. {@link DirContext}s are retrieved from the pool which maintains
 * them.
 *
 *
 * <br>
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
 * <td valign="top">The {@link DirContextValidator} to use for validating
 * {@link DirContext}s. Required if any of the test/validate options are enabled.</td>
 * <td valign="top">No</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td valign="top">minIdle</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setMinIdle(int)}</td>
 * <td valign="top">No</td>
 * <td valign="top">0</td>
 * </tr>
 * <tr>
 * <td valign="top">maxIdle</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setMaxIdle(int)}</td>
 * <td valign="top">No</td>
 * <td valign="top">8</td>
 * </tr>
 * <tr>
 * <td valign="top">maxActive</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setMaxActive(int)}</td>
 * <td valign="top">No</td>
 * <td valign="top">8</td>
 * </tr>
 * <tr>
 * <td valign="top">maxTotal</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setMaxTotal(int)}</td>
 * <td valign="top">No</td>
 * <td valign="top">-1</td>
 * </tr>
 * <tr>
 * <td valign="top">maxWait</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setMaxWait(long)}</td>
 * <td valign="top">No</td>
 * <td valign="top">-1L</td>
 * </tr>
 * <tr>
 * <td valign="top">whenExhaustedAction</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setWhenExhaustedAction(byte)}</td>
 * <td valign="top">No</td>
 * <td valign="top">{@link GenericKeyedObjectPool#WHEN_EXHAUSTED_BLOCK}</td>
 * </tr>
 * <tr>
 * <td valign="top">testOnBorrow</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setTestOnBorrow(boolean)}</td>
 * <td valign="top">No</td>
 * <td valign="top">false</td>
 * </tr>
 * <tr>
 * <td valign="top">testOnReturn</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setTestOnReturn(boolean)}</td>
 * <td valign="top">No</td>
 * <td valign="top">false</td>
 * </tr>
 * <tr>
 * <td valign="top">testWhileIdle</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setTestWhileIdle(boolean)}</td>
 * <td valign="top">No</td>
 * <td valign="top">false</td>
 * </tr>
 * <tr>
 * <td valign="top">timeBetweenEvictionRunsMillis</td>
 * <td valign="top">
 * {@link GenericKeyedObjectPool#setTimeBetweenEvictionRunsMillis(long)}</td>
 * <td valign="top">No</td>
 * <td valign="top">-1L</td>
 * </tr>
 * <tr>
 * <td valign="top">minEvictableIdleTimeMillis</td>
 * <td valign="top">
 * {@link GenericKeyedObjectPool#setMinEvictableIdleTimeMillis(long)}</td>
 * <td valign="top">No</td>
 * <td valign="top">1000L * 60L * 30L</td>
 * </tr>
 * <tr>
 * <td valign="top">numTestsPerEvictionRun</td>
 * <td valign="top">{@link GenericKeyedObjectPool#setNumTestsPerEvictionRun(int)}</td>
 * <td valign="top">No</td>
 * <td valign="top">3</td>
 * </tr>
 * </table>
 *
 * @author Eric Dalquist
 */
public class PoolingContextSource extends DelegatingBaseLdapPathContextSourceSupport
		implements ContextSource, DisposableBean {

	/**
	 * The logger for this class and sub-classes
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final GenericKeyedObjectPool keyedObjectPool;

	private final DirContextPoolableObjectFactory dirContextPoolableObjectFactory;

	/**
	 * Creates a new pooling context source, setting up the DirContext object factory and
	 * generic keyed object pool.
	 */
	public PoolingContextSource() {
		this.dirContextPoolableObjectFactory = new DirContextPoolableObjectFactory();
		this.keyedObjectPool = new GenericKeyedObjectPool();
		this.keyedObjectPool.setFactory(this.dirContextPoolableObjectFactory);
	}

	// ***** Pool Property Configuration *****//

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMaxActive()
	 */
	public int getMaxActive() {
		return this.keyedObjectPool.getMaxActive();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMaxIdle()
	 */
	public int getMaxIdle() {
		return this.keyedObjectPool.getMaxIdle();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMaxTotal()
	 */
	public int getMaxTotal() {
		return this.keyedObjectPool.getMaxTotal();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMaxWait()
	 */
	public long getMaxWait() {
		return this.keyedObjectPool.getMaxWait();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMinEvictableIdleTimeMillis()
	 */
	public long getMinEvictableIdleTimeMillis() {
		return this.keyedObjectPool.getMinEvictableIdleTimeMillis();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getMinIdle()
	 */
	public int getMinIdle() {
		return this.keyedObjectPool.getMinIdle();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getNumActive()
	 */
	public int getNumActive() {
		return this.keyedObjectPool.getNumActive();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getNumIdle()
	 */
	public int getNumIdle() {
		return this.keyedObjectPool.getNumIdle();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getNumTestsPerEvictionRun()
	 */
	public int getNumTestsPerEvictionRun() {
		return this.keyedObjectPool.getNumTestsPerEvictionRun();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getTestOnBorrow()
	 */
	public boolean getTestOnBorrow() {
		return this.keyedObjectPool.getTestOnBorrow();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getTestOnReturn()
	 */
	public boolean getTestOnReturn() {
		return this.keyedObjectPool.getTestOnReturn();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getTestWhileIdle()
	 */
	public boolean getTestWhileIdle() {
		return this.keyedObjectPool.getTestWhileIdle();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getTimeBetweenEvictionRunsMillis()
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return this.keyedObjectPool.getTimeBetweenEvictionRunsMillis();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#getWhenExhaustedAction()
	 */
	public byte getWhenExhaustedAction() {
		return this.keyedObjectPool.getWhenExhaustedAction();
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMaxActive(int)
	 */
	public void setMaxActive(int maxActive) {
		this.keyedObjectPool.setMaxActive(maxActive);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMaxIdle(int)
	 */
	public void setMaxIdle(int maxIdle) {
		this.keyedObjectPool.setMaxIdle(maxIdle);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMaxTotal(int)
	 */
	public void setMaxTotal(int maxTotal) {
		this.keyedObjectPool.setMaxTotal(maxTotal);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMaxWait(long)
	 */
	public void setMaxWait(long maxWait) {
		this.keyedObjectPool.setMaxWait(maxWait);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMinEvictableIdleTimeMillis(long)
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.keyedObjectPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setMinIdle(int)
	 */
	public void setMinIdle(int poolSize) {
		this.keyedObjectPool.setMinIdle(poolSize);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setNumTestsPerEvictionRun(int)
	 */
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.keyedObjectPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setTestOnBorrow(boolean)
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.keyedObjectPool.setTestOnBorrow(testOnBorrow);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setTestOnReturn(boolean)
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		this.keyedObjectPool.setTestOnReturn(testOnReturn);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setTestWhileIdle(boolean)
	 */
	public void setTestWhileIdle(boolean testWhileIdle) {
		this.keyedObjectPool.setTestWhileIdle(testWhileIdle);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setTimeBetweenEvictionRunsMillis(long)
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.keyedObjectPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericKeyedObjectPool#setWhenExhaustedAction(byte)
	 */
	public void setWhenExhaustedAction(byte whenExhaustedAction) {
		this.keyedObjectPool.setWhenExhaustedAction(whenExhaustedAction);
	}

	// ***** Object Factory Property Configuration *****//

	/**
	 * @return the contextSource
	 */
	public ContextSource getContextSource() {
		return this.dirContextPoolableObjectFactory.getContextSource();
	}

	/**
	 * @return the dirContextValidator
	 */
	public DirContextValidator getDirContextValidator() {
		return this.dirContextPoolableObjectFactory.getDirContextValidator();
	}

	/**
	 * @param contextSource the contextSource to set Required
	 */
	public void setContextSource(ContextSource contextSource) {
		this.dirContextPoolableObjectFactory.setContextSource(contextSource);
	}

	/**
	 * @param dirContextValidator the dirContextValidator to set Required
	 */
	public void setDirContextValidator(DirContextValidator dirContextValidator) {
		this.dirContextPoolableObjectFactory.setDirContextValidator(dirContextValidator);
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
		this.dirContextPoolableObjectFactory.setNonTransientExceptions(nonTransientExceptions);
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
			this.logger.warn("An exception occured while closing the underlying pool.", ex);
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

}
