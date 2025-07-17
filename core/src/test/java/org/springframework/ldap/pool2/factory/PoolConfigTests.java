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

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.pool2.AbstractPoolTestCase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Anindya Chatterjee
 */
public class PoolConfigTests extends AbstractPoolTestCase {

	@Test
	public void testProperties() {
		final PoolConfig poolConfig = new PoolConfig();

		poolConfig.setMaxTotalPerKey(5);
		final int maxTotalPerKey = poolConfig.getMaxTotalPerKey();
		assertThat(maxTotalPerKey).isEqualTo(5);

		poolConfig.setMaxIdlePerKey(500);
		final int maxIdle = poolConfig.getMaxIdlePerKey();
		assertThat(maxIdle).isEqualTo(500);

		poolConfig.setMaxTotal(5000);
		final int maxTotal = poolConfig.getMaxTotal();
		assertThat(maxTotal).isEqualTo(5000);

		poolConfig.setMaxWaitMillis(2000L);
		final long maxWait = poolConfig.getMaxWaitMillis();
		assertThat(maxWait).isEqualTo(2000L);

		poolConfig.setMinEvictableIdleTimeMillis(60000L);
		final long minEvictableIdleTimeMillis = poolConfig.getMinEvictableIdleTimeMillis();
		assertThat(minEvictableIdleTimeMillis).isEqualTo(60000L);

		poolConfig.setMinIdlePerKey(100);
		final int minIdle = poolConfig.getMinIdlePerKey();
		assertThat(minIdle).isEqualTo(100);

		poolConfig.setNumTestsPerEvictionRun(5);
		final int numTestsPerEvictionRun = poolConfig.getNumTestsPerEvictionRun();
		assertThat(numTestsPerEvictionRun).isEqualTo(5);

		poolConfig.setTestOnBorrow(true);
		final boolean testOnBorrow = poolConfig.isTestOnBorrow();
		assertThat(testOnBorrow).isEqualTo(true);

		poolConfig.setTestOnReturn(true);
		final boolean testOnReturn = poolConfig.isTestOnReturn();
		assertThat(testOnReturn).isEqualTo(true);

		poolConfig.setTestWhileIdle(true);
		final boolean testWhileIdle = poolConfig.isTestWhileIdle();
		assertThat(testWhileIdle).isEqualTo(true);

		poolConfig.setTestOnCreate(true);
		final boolean testOnCreate = poolConfig.isTestOnCreate();
		assertThat(testOnCreate).isEqualTo(true);

		poolConfig.setTimeBetweenEvictionRunsMillis(120000L);
		final long timeBetweenEvictionRunsMillis = poolConfig.getTimeBetweenEvictionRunsMillis();
		assertThat(timeBetweenEvictionRunsMillis).isEqualTo(120000L);

		poolConfig.setSoftMinEvictableIdleTimeMillis(120000L);
		final long softMinEvictableIdleTimeMillis = poolConfig.getSoftMinEvictableIdleTimeMillis();
		assertThat(softMinEvictableIdleTimeMillis).isEqualTo(120000L);

		poolConfig.setBlockWhenExhausted(true);
		final boolean whenExhaustedAction = poolConfig.isBlockWhenExhausted();
		assertThat(whenExhaustedAction).isEqualTo(GenericKeyedObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED);

		poolConfig.setEvictionPolicyClassName(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME);
		final String evictionPolicyClassName = poolConfig.getEvictionPolicyClassName();
		assertThat(evictionPolicyClassName).isEqualTo(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME);

		poolConfig.setFairness(true);
		final boolean fairness = poolConfig.isFairness();
		assertThat(fairness).isEqualTo(true);

		poolConfig.setJmxEnabled(true);
		final boolean jmxEnabled = poolConfig.isJmxEnabled();
		assertThat(jmxEnabled).isEqualTo(true);

		poolConfig.setJmxNameBase("test");
		final String jmxBaseName = poolConfig.getJmxNameBase();
		assertThat(jmxBaseName).isEqualTo("test");

		poolConfig.setJmxNamePrefix("pool");
		final String prefix = poolConfig.getJmxNamePrefix();
		assertThat(prefix).isEqualTo("pool");

		poolConfig.setLifo(true);
		final boolean lifo = poolConfig.isLifo();
		assertThat(lifo).isEqualTo(true);
	}

}
