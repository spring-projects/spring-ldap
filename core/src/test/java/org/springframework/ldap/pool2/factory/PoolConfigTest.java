/*
 * Copyright 2005-2015 the original author or authors.
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
package org.springframework.ldap.pool2.factory;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.Test;
import org.springframework.ldap.pool2.AbstractPoolTestCase;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 * */
public class PoolConfigTest extends AbstractPoolTestCase {

    @Test
    public void testProperties() {
        final PoolConfig poolConfig = new PoolConfig();

        poolConfig.setMaxTotalPerKey(5);
        final int maxTotalPerKey = poolConfig.getMaxTotalPerKey();
        assertEquals(5, maxTotalPerKey);

        poolConfig.setMaxIdlePerKey(500);
        final int maxIdle = poolConfig.getMaxIdlePerKey();
        assertEquals(500, maxIdle);

        poolConfig.setMaxTotal(5000);
        final int maxTotal = poolConfig.getMaxTotal();
        assertEquals(5000, maxTotal);

        poolConfig.setMaxWaitMillis(2000L);
        final long maxWait = poolConfig.getMaxWaitMillis();
        assertEquals(2000L, maxWait);

        poolConfig.setMinEvictableIdleTimeMillis(60000L);
        final long minEvictableIdleTimeMillis = poolConfig.getMinEvictableIdleTimeMillis();
        assertEquals(60000L, minEvictableIdleTimeMillis);

        poolConfig.setMinIdlePerKey(100);
        final int minIdle = poolConfig.getMinIdlePerKey();
        assertEquals(100, minIdle);

        poolConfig.setNumTestsPerEvictionRun(5);
        final int numTestsPerEvictionRun = poolConfig.getNumTestsPerEvictionRun();
        assertEquals(5, numTestsPerEvictionRun);

        poolConfig.setTestOnBorrow(true);
        final boolean testOnBorrow = poolConfig.isTestOnBorrow();
        assertEquals(true, testOnBorrow);

        poolConfig.setTestOnReturn(true);
        final boolean testOnReturn = poolConfig.isTestOnReturn();
        assertEquals(true, testOnReturn);

        poolConfig.setTestWhileIdle(true);
        final boolean testWhileIdle = poolConfig.isTestWhileIdle();
        assertEquals(true, testWhileIdle);

        poolConfig.setTestOnCreate(true);
        final boolean testOnCreate = poolConfig.isTestOnCreate();
        assertEquals(true, testOnCreate);

        poolConfig.setTimeBetweenEvictionRunsMillis(120000L);
        final long timeBetweenEvictionRunsMillis = poolConfig.getTimeBetweenEvictionRunsMillis();
        assertEquals(120000L, timeBetweenEvictionRunsMillis);

        poolConfig.setSoftMinEvictableIdleTimeMillis(120000L);
        final long softMinEvictableIdleTimeMillis = poolConfig.getSoftMinEvictableIdleTimeMillis();
        assertEquals(120000L, softMinEvictableIdleTimeMillis);

        poolConfig.setBlockWhenExhausted(true);
        final boolean whenExhaustedAction = poolConfig.isBlockWhenExhausted();
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED, whenExhaustedAction);

        poolConfig.setEvictionPolicyClassName(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME);
        final String evictionPolicyClassName = poolConfig.getEvictionPolicyClassName();
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME, evictionPolicyClassName);

        poolConfig.setFairness(true);
        final boolean fairness = poolConfig.isFairness();
        assertEquals(true, fairness);

        poolConfig.setJmxEnabled(true);
        final boolean jmxEnabled = poolConfig.isJmxEnabled();
        assertEquals(true, jmxEnabled);

        poolConfig.setJmxNameBase("test");
        final String jmxBaseName = poolConfig.getJmxNameBase();
        assertEquals("test", jmxBaseName);

        poolConfig.setJmxNamePrefix("pool");
        final String prefix = poolConfig.getJmxNamePrefix();
        assertEquals("pool", prefix);

        poolConfig.setLifo(true);
        final boolean lifo = poolConfig.isLifo();
        assertEquals(true, lifo);
    }
}
