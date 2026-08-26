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

package org.springframework.ldap.pool2.factory;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * A wrapper class for the pool configuration. It helps to create an instance of
 * {@link GenericKeyedObjectPoolConfig}.
 *
 * @author Anindya Chatterjee
 * @since 2.0
 */
public class PoolConfig {

	private int maxIdlePerKey = 8;

	private int maxTotal = -1;

	private int maxTotalPerKey = 8;

	private int minIdlePerKey = 0;

	private boolean blockWhenExhausted = true;

	private String evictionPolicyClassName = "org.apache.commons.pool2.impl.DefaultEvictionPolicy";

	private boolean fairness = false;

	private boolean jmxEnabled = true;

	private String jmxNameBase = null;

	private String jmxNamePrefix = "ldap-pool";

	private boolean lifo = true;

	private long maxWaitMillis = -1L;

	private long minEvictableIdleTimeMillis = 1000L * 60L * 30L;

	private int numTestsPerEvictionRun = 3;

	private long softMinEvictableIdleTimeMillis = -1L;

	private boolean testOnBorrow = false;

	private boolean testOnCreate = false;

	private boolean testOnReturn = false;

	private boolean testWhileIdle = false;

	private long timeBetweenEvictionRunsMillis = -1L;

	/**
	 * Set the maximum number of idle instances per key.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxIdlePerKey(int)
	 *
	 */
	public void setMaxIdlePerKey(int maxIdlePerKey) {
		this.maxIdlePerKey = maxIdlePerKey;
	}

	/**
	 * Set the overall maximum number of objects that can exist in this pool.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxTotal(int)
	 *
	 */
	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	/**
	 * Set the maximum number of objects that can exist in this pool for a given key.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxTotalPerKey(int)
	 */
	public void setMaxTotalPerKey(int maxTotalPerKey) {
		this.maxTotalPerKey = maxTotalPerKey;
	}

	/**
	 * Set the minimum number of idle instances per key.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMinIdlePerKey(int)
	 */
	public void setMinIdlePerKey(int minIdlePerKey) {
		this.minIdlePerKey = minIdlePerKey;
	}

	/**
	 * Set whether to block when the pool is exhausted.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setBlockWhenExhausted(boolean)
	 */
	public void setBlockWhenExhausted(boolean blockWhenExhausted) {
		this.blockWhenExhausted = blockWhenExhausted;
	}

	/**
	 * Set the name of the eviction policy class.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setEvictionPolicyClassName(String)
	 */
	public void setEvictionPolicyClassName(String evictionPolicyClassName) {
		this.evictionPolicyClassName = evictionPolicyClassName;
	}

	/**
	 * Set whether waiting threads are served as if waiting in a FIFO queue.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setFairness(boolean)
	 */
	public void setFairness(boolean fairness) {
		this.fairness = fairness;
	}

	/**
	 * Set whether JMX is enabled for this pool.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxEnabled(boolean)
	 */
	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * Set the base name to use for JMX naming.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxNameBase(String)
	 */
	public void setJmxNameBase(String jmxNameBase) {
		this.jmxNameBase = jmxNameBase;
	}

	/**
	 * Set the JMX name prefix.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxNamePrefix(String)
	 */
	public void setJmxNamePrefix(String jmxNamePrefix) {
		this.jmxNamePrefix = jmxNamePrefix;
	}

	/**
	 * Set whether the pool acts as a LIFO queue.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setLifo(boolean)
	 */
	public void setLifo(boolean lifo) {
		this.lifo = lifo;
	}

	/**
	 * Set the maximum time to wait for an object to become available.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxWaitMillis(long)
	 */
	public void setMaxWaitMillis(long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	/**
	 * Set the minimum time an object may sit idle before being eligible for eviction.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMinEvictableIdleTimeMillis(long)
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * Set the number of objects to examine during each eviction run.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setNumTestsPerEvictionRun(int)
	 */
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	/**
	 * Set the minimum time an object may sit idle before being eligible for eviction,
	 * subject to the minimum idle constraint.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setSoftMinEvictableIdleTimeMillis(long)
	 */
	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
	}

	/**
	 * Set whether objects are validated before being borrowed from the pool.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnBorrow(boolean)
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	/**
	 * Set whether objects are validated after creation.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnCreate(boolean)
	 */
	public void setTestOnCreate(boolean testOnCreate) {
		this.testOnCreate = testOnCreate;
	}

	/**
	 * Set whether objects are validated before being returned to the pool.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnReturn(boolean)
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	/**
	 * Set whether idle objects are validated by the idle object evictor.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestWhileIdle(boolean)
	 */
	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	/**
	 * Set the time between runs of the idle object evictor.
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTimeBetweenEvictionRunsMillis(long)
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	/**
	 * Get the maximum number of idle instances per key.
	 * @see GenericKeyedObjectPoolConfig#getMaxIdlePerKey()
	 */
	public int getMaxIdlePerKey() {
		return this.maxIdlePerKey;
	}

	/**
	 * Get the overall maximum number of objects that can exist in this pool.
	 * @see GenericKeyedObjectPoolConfig#getMaxTotal()
	 */
	public int getMaxTotal() {
		return this.maxTotal;
	}

	/**
	 * Get the maximum number of objects that can exist in this pool for a given key.
	 * @see GenericKeyedObjectPoolConfig#getMaxTotalPerKey()
	 */
	public int getMaxTotalPerKey() {
		return this.maxTotalPerKey;
	}

	/**
	 * Get the minimum number of idle instances per key.
	 * @see GenericKeyedObjectPoolConfig#getMinIdlePerKey()
	 */
	public int getMinIdlePerKey() {
		return this.minIdlePerKey;
	}

	/**
	 * Return whether to block when the pool is exhausted.
	 * @see GenericKeyedObjectPoolConfig#getBlockWhenExhausted()
	 */
	public boolean isBlockWhenExhausted() {
		return this.blockWhenExhausted;
	}

	/**
	 * Get the name of the eviction policy class.
	 * @see GenericKeyedObjectPoolConfig#getEvictionPolicyClassName()
	 */
	public String getEvictionPolicyClassName() {
		return this.evictionPolicyClassName;
	}

	/**
	 * Return whether waiting threads are served as if waiting in a FIFO queue.
	 * @see GenericKeyedObjectPoolConfig#getFairness()
	 */
	public boolean isFairness() {
		return this.fairness;
	}

	/**
	 * Return whether JMX is enabled for this pool.
	 * @see GenericKeyedObjectPoolConfig#getJmxEnabled()
	 */
	public boolean isJmxEnabled() {
		return this.jmxEnabled;
	}

	/**
	 * Get the base name to use for JMX naming.
	 * @see GenericKeyedObjectPoolConfig#getJmxNameBase()
	 */
	public String getJmxNameBase() {
		return this.jmxNameBase;
	}

	/**
	 * Get the JMX name prefix.
	 * @see GenericKeyedObjectPoolConfig#getJmxNamePrefix()
	 */
	public String getJmxNamePrefix() {
		return this.jmxNamePrefix;
	}

	/**
	 * Return whether the pool acts as a LIFO queue.
	 * @see GenericKeyedObjectPoolConfig#getLifo()
	 */
	public boolean isLifo() {
		return this.lifo;
	}

	/**
	 * Get the maximum time to wait for an object to become available.
	 * @see GenericKeyedObjectPoolConfig#getMaxWaitMillis()
	 */
	public long getMaxWaitMillis() {
		return this.maxWaitMillis;
	}

	/**
	 * Get the minimum time an object may sit idle before being eligible for eviction.
	 * @see GenericKeyedObjectPoolConfig#getMinEvictableIdleTimeMillis()
	 */
	public long getMinEvictableIdleTimeMillis() {
		return this.minEvictableIdleTimeMillis;
	}

	/**
	 * Get the number of objects to examine during each eviction run.
	 * @see GenericKeyedObjectPoolConfig#getNumTestsPerEvictionRun()
	 */
	public int getNumTestsPerEvictionRun() {
		return this.numTestsPerEvictionRun;
	}

	/**
	 * Get the minimum time an object may sit idle before being eligible for eviction,
	 * subject to the minimum idle constraint.
	 * @see GenericKeyedObjectPoolConfig#getSoftMinEvictableIdleTimeMillis()
	 */
	public long getSoftMinEvictableIdleTimeMillis() {
		return this.softMinEvictableIdleTimeMillis;
	}

	/**
	 * Return whether objects are validated before being borrowed from the pool.
	 * @see GenericKeyedObjectPoolConfig#getTestOnBorrow()
	 */
	public boolean isTestOnBorrow() {
		return this.testOnBorrow;
	}

	/**
	 * Return whether objects are validated after creation.
	 * @see GenericKeyedObjectPoolConfig#getTestOnCreate()
	 */
	public boolean isTestOnCreate() {
		return this.testOnCreate;
	}

	/**
	 * Return whether objects are validated before being returned to the pool.
	 * @see GenericKeyedObjectPoolConfig#getTestOnReturn()
	 */
	public boolean isTestOnReturn() {
		return this.testOnReturn;
	}

	/**
	 * Return whether idle objects are validated by the idle object evictor.
	 * @see GenericKeyedObjectPoolConfig#getTestWhileIdle()
	 */
	public boolean isTestWhileIdle() {
		return this.testWhileIdle;
	}

	/**
	 * Get the time between runs of the idle object evictor.
	 * @see GenericKeyedObjectPoolConfig#getTimeBetweenEvictionRunsMillis()
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return this.timeBetweenEvictionRunsMillis;
	}

}
