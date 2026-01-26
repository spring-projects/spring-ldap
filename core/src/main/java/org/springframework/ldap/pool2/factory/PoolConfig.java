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
import org.jspecify.annotations.Nullable;

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

	private @Nullable String jmxNameBase = null;

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
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxIdlePerKey(int)
	 *
	 */
	public void setMaxIdlePerKey(int maxIdlePerKey) {
		this.maxIdlePerKey = maxIdlePerKey;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxTotal(int)
	 *
	 */
	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxTotalPerKey(int)
	 */
	public void setMaxTotalPerKey(int maxTotalPerKey) {
		this.maxTotalPerKey = maxTotalPerKey;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMinIdlePerKey(int)
	 */
	public void setMinIdlePerKey(int minIdlePerKey) {
		this.minIdlePerKey = minIdlePerKey;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setBlockWhenExhausted(boolean)
	 */
	public void setBlockWhenExhausted(boolean blockWhenExhausted) {
		this.blockWhenExhausted = blockWhenExhausted;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setEvictionPolicyClassName(String)
	 */
	public void setEvictionPolicyClassName(String evictionPolicyClassName) {
		this.evictionPolicyClassName = evictionPolicyClassName;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setFairness(boolean)
	 */
	public void setFairness(boolean fairness) {
		this.fairness = fairness;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxEnabled(boolean)
	 */
	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxNameBase(String)
	 */
	public void setJmxNameBase(@Nullable String jmxNameBase) {
		this.jmxNameBase = jmxNameBase;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setJmxNamePrefix(String)
	 */
	public void setJmxNamePrefix(String jmxNamePrefix) {
		this.jmxNamePrefix = jmxNamePrefix;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setLifo(boolean)
	 */
	public void setLifo(boolean lifo) {
		this.lifo = lifo;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMaxWaitMillis(long)
	 */
	public void setMaxWaitMillis(long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setMinEvictableIdleTimeMillis(long)
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setNumTestsPerEvictionRun(int)
	 */
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setSoftMinEvictableIdleTimeMillis(long)
	 */
	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnBorrow(boolean)
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnCreate(boolean)
	 */
	public void setTestOnCreate(boolean testOnCreate) {
		this.testOnCreate = testOnCreate;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestOnReturn(boolean)
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTestWhileIdle(boolean)
	 */
	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	/**
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig#setTimeBetweenEvictionRunsMillis(long)
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMaxIdlePerKey()
	 */
	public int getMaxIdlePerKey() {
		return this.maxIdlePerKey;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMaxTotal()
	 */
	public int getMaxTotal() {
		return this.maxTotal;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMaxIdlePerKey()
	 */
	public int getMaxTotalPerKey() {
		return this.maxTotalPerKey;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMinIdlePerKey()
	 */
	public int getMinIdlePerKey() {
		return this.minIdlePerKey;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getBlockWhenExhausted()
	 */
	public boolean isBlockWhenExhausted() {
		return this.blockWhenExhausted;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getEvictionPolicyClassName()
	 */
	public String getEvictionPolicyClassName() {
		return this.evictionPolicyClassName;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getFairness()
	 */
	public boolean isFairness() {
		return this.fairness;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getJmxEnabled()
	 */
	public boolean isJmxEnabled() {
		return this.jmxEnabled;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getJmxNameBase()
	 */
	public @Nullable String getJmxNameBase() {
		return this.jmxNameBase;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getJmxNamePrefix()
	 */
	public String getJmxNamePrefix() {
		return this.jmxNamePrefix;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getLifo()
	 */
	public boolean isLifo() {
		return this.lifo;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMaxWaitMillis()
	 */
	public long getMaxWaitMillis() {
		return this.maxWaitMillis;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getMinEvictableIdleTimeMillis()
	 */
	public long getMinEvictableIdleTimeMillis() {
		return this.minEvictableIdleTimeMillis;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getNumTestsPerEvictionRun()
	 */
	public int getNumTestsPerEvictionRun() {
		return this.numTestsPerEvictionRun;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getSoftMinEvictableIdleTimeMillis()
	 */
	public long getSoftMinEvictableIdleTimeMillis() {
		return this.softMinEvictableIdleTimeMillis;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getTestOnBorrow()
	 */
	public boolean isTestOnBorrow() {
		return this.testOnBorrow;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getTestOnCreate()
	 */
	public boolean isTestOnCreate() {
		return this.testOnCreate;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getTestOnReturn()
	 */
	public boolean isTestOnReturn() {
		return this.testOnReturn;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getTestWhileIdle()
	 */
	public boolean isTestWhileIdle() {
		return this.testWhileIdle;
	}

	/**
	 * @see GenericKeyedObjectPoolConfig#getTimeBetweenEvictionRunsMillis()
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return this.timeBetweenEvictionRunsMillis;
	}

}
