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

package org.springframework.ldap.config;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceAndDataSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.support.DifferentSubtreeTempEntryRenamingStrategy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.CannotProceedException;
import javax.naming.CommunicationException;
import javax.naming.directory.SearchControls;
import java.lang.management.ManagementFactory;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapTemplateNamespaceHandlerTest {

    @Test
    public void verifyParseWithDefaultValues() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-defaults.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);

        assertNotNull(outerContextSource);
        assertNotNull(ldapTemplate);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertEquals(LdapUtils.emptyLdapName(), getInternalState(contextSource, "base"));
        assertEquals("uid=admin", getInternalState(contextSource, "userDn"));
        assertEquals("apassword", getInternalState(contextSource, "password"));
        assertArrayEquals(new String[]{"ldap://localhost:389"}, (Object[]) getInternalState(contextSource, "urls"));
        assertEquals(Boolean.FALSE, getInternalState(contextSource, "pooled"));
        assertEquals(Boolean.FALSE, getInternalState(contextSource, "anonymousReadOnly"));
        assertNull(getInternalState(contextSource, "referral"));

        assertSame(outerContextSource, getInternalState(ldapTemplate, "contextSource"));
        assertEquals(Boolean.FALSE, getInternalState(ldapTemplate, "ignorePartialResultException"));
        assertEquals(Boolean.FALSE, getInternalState(ldapTemplate, "ignoreNameNotFoundException"));
        assertEquals(0, getInternalState(ldapTemplate, "defaultCountLimit"));
        assertEquals(0, getInternalState(ldapTemplate, "defaultTimeLimit"));
        assertEquals(SearchControls.SUBTREE_SCOPE, getInternalState(ldapTemplate, "defaultSearchScope"));
    }

    @Test
    public void verifyThatAnonymousReadOnlyContextWillNotBeWrappedInProxy() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-anonymous-read-only.xml");
        ContextSource contextSource = ctx.getBean(ContextSource.class);

        assertNotNull(contextSource);
        assertTrue(contextSource instanceof LdapContextSource);
        assertEquals(Boolean.TRUE, getInternalState(contextSource, "anonymousReadOnly"));
    }

    @Test(expected = BeansException.class)
    public void verifyThatAnonymousReadOnlyAndTransactionalThrowsException() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-anonymous-read-only-and-transactions.xml");
    }

    @Test(expected = BeansException.class)
    public void verifyThatMissingUsernameThrowsException() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-missing-username.xml");
    }

    @Test(expected = BeansException.class)
    public void verifyThatMissingPasswordThrowsException() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-missing-password.xml");
    }

    @Test
    public void verifyReferences() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-references.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        AuthenticationSource authenticationSource = ctx.getBean(AuthenticationSource.class);
        DirContextAuthenticationStrategy authenticationStrategy = ctx.getBean(DirContextAuthenticationStrategy.class);
        Object baseEnv = ctx.getBean("baseEnvProps");

        assertNotNull(outerContextSource);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertSame(authenticationSource, getInternalState(contextSource, "authenticationSource"));
        assertSame(authenticationStrategy, getInternalState(contextSource, "authenticationStrategy"));
        assertEquals(baseEnv, getInternalState(contextSource, "baseEnv"));
    }

    @Test
    public void verifyParseWithCustomValues() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-values.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);
        DirContextAuthenticationStrategy authenticationStrategy = ctx.getBean(DirContextAuthenticationStrategy.class);

        assertNotNull(outerContextSource);
        assertNotNull(ldapTemplate);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertEquals(LdapUtils.newLdapName("dc=261consulting,dc=com"), getInternalState(contextSource, "base"));
        assertEquals("uid=admin", getInternalState(contextSource, "userDn"));
        assertEquals("apassword", getInternalState(contextSource, "password"));
        assertArrayEquals(new String[]{"ldap://localhost:389"}, (Object[]) getInternalState(contextSource, "urls"));
        assertEquals(Boolean.TRUE, getInternalState(contextSource, "pooled"));
        assertEquals(Boolean.FALSE, getInternalState(contextSource, "anonymousReadOnly"));
        assertEquals("follow", getInternalState(contextSource, "referral"));
        assertSame(authenticationStrategy, getInternalState(contextSource, "authenticationStrategy"));

        assertSame(outerContextSource, getInternalState(ldapTemplate, "contextSource"));
        assertEquals(Boolean.TRUE, getInternalState(ldapTemplate, "ignorePartialResultException"));
        assertEquals(Boolean.TRUE, getInternalState(ldapTemplate, "ignoreNameNotFoundException"));
        assertEquals(100, getInternalState(ldapTemplate, "defaultCountLimit"));
        assertEquals(200, getInternalState(ldapTemplate, "defaultTimeLimit"));
        assertEquals(SearchControls.OBJECT_SCOPE, getInternalState(ldapTemplate, "defaultSearchScope"));
    }

    @Test
    public void supportsSpel() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-spel.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertNotNull(outerContextSource);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertEquals(LdapUtils.newLdapName("dc=261consulting,dc=com"), getInternalState(contextSource, "base"));
        assertEquals("uid=admin", getInternalState(contextSource, "userDn"));
        assertEquals("apassword", getInternalState(contextSource, "password"));
        assertArrayEquals(new String[]{"ldap://localhost:389"}, (Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void supportsSpelMultiUrls() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-spel-multiurls.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertNotNull(outerContextSource);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertArrayEquals(new String[] { "ldap://a.localhost:389", "ldap://b.localhost:389" },
                (Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void supportsMultipleUrls() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-multiurls.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertNotNull(outerContextSource);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertArrayEquals(new String[] { "ldap://a.localhost:389", "ldap://b.localhost:389" },
                (Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void verifyParseWithDefaultTransactions() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertNotNull(outerContextSource);
        assertNotNull(transactionManager);

        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertTrue(transactionManager instanceof ContextSourceTransactionManager);

        Object delegate = getInternalState(transactionManager, "delegate");
        assertSame(contextSource, getInternalState(delegate, "contextSource"));
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertTrue(renamingStrategy instanceof DefaultTempEntryRenamingStrategy);
        assertEquals("_temp", getInternalState(renamingStrategy, "tempSuffix"));
    }

    @Test
    public void verifyParseTransactionWithDataSource() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-datasource.xml");
        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertTrue(transactionManager instanceof ContextSourceAndDataSourceTransactionManager);
    }

    @Test
    public void verifyParseTransactionsWithDefaultStrategyAndSuffix() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-defaults-with-suffix.xml");

        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertNotNull(transactionManager);
        assertTrue(transactionManager instanceof ContextSourceTransactionManager);

        Object delegate = getInternalState(transactionManager, "delegate");
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertTrue(renamingStrategy instanceof DefaultTempEntryRenamingStrategy);
        assertEquals("_thisisthesuffix", getInternalState(renamingStrategy, "tempSuffix"));
    }

    @Test
    public void verifyParseTransactionsWithDifferentSubtreeStrategy() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-different-subtree.xml");

        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertNotNull(transactionManager);
        assertTrue(transactionManager instanceof ContextSourceTransactionManager);

        Object delegate = getInternalState(transactionManager, "delegate");
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertTrue(renamingStrategy instanceof DifferentSubtreeTempEntryRenamingStrategy);
        assertEquals(LdapUtils.newLdapName("ou=temp"), getInternalState(renamingStrategy, "subtreeNode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyParsePoolingDefaults() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);
        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);
        assertTrue(pooledContextSource instanceof PoolingContextSource);

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPoolableObjectFactory");
        assertNotNull(getInternalState(objectFactory, "contextSource"));
        assertNull(getInternalState(objectFactory, "dirContextValidator"));
        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertEquals(1, nonTransientExceptions.size());
        assertTrue(nonTransientExceptions.contains(CommunicationException.class));

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(8, objectPool.getMaxActive());
        assertEquals(-1, objectPool.getMaxTotal());
        assertEquals(8, objectPool.getMaxIdle());
        assertEquals(-1, objectPool.getMaxWait());
        assertEquals(0, objectPool.getMinIdle());
        assertEquals(1, objectPool.getWhenExhaustedAction());
    }

    @Test
    public void verifyParsePoolingSizeSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-configured-poolsize.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(10, objectPool.getMaxActive());
        assertEquals(12, objectPool.getMaxTotal());
        assertEquals(11, objectPool.getMaxIdle());
        assertEquals(13, objectPool.getMaxWait());
        assertEquals(14, objectPool.getMinIdle());
        assertEquals(0, objectPool.getWhenExhaustedAction());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyParsePoolingValidationSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-test-specified.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(123, objectPool.getMinEvictableIdleTimeMillis());
        assertEquals(321, objectPool.getTimeBetweenEvictionRunsMillis());
        assertEquals(22, objectPool.getNumTestsPerEvictionRun());

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPoolableObjectFactory");
        DefaultDirContextValidator validator = (DefaultDirContextValidator) getInternalState(objectFactory, "dirContextValidator");
        assertEquals("ou=test", validator.getBase());
        assertEquals("objectclass=person", validator.getFilter());

        SearchControls searchControls = ctx.getBean(SearchControls.class);
        assertEquals("objectclass=person", validator.getFilter());
        assertSame(searchControls, validator.getSearchControls());

        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertEquals(2, nonTransientExceptions.size());
        assertTrue(nonTransientExceptions.contains(CommunicationException.class));
        assertTrue(nonTransientExceptions.contains(CannotProceedException.class));
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPoolingAndNativePoolingWillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-with-native.xml");
    }

    @Test
    public void verifyAutomaticRepositorySupport() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-with-repositories.xml");
        DummyLdapRepository repository = ctx.getBean(DummyLdapRepository.class);

        assertNotNull(repository);
    }

    @Test
    public void verifyParsePooling2Defaults() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling2-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);
        assertTrue(outerContextSource instanceof TransactionAwareContextSourceProxy);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);
        assertTrue(pooledContextSource instanceof PooledContextSource);
        assertNotNull(getInternalState(pooledContextSource, "poolConfig"));

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPooledObjectFactory");
        assertNotNull(getInternalState(objectFactory, "contextSource"));
        assertNull(getInternalState(objectFactory, "dirContextValidator"));
        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertEquals(1, nonTransientExceptions.size());
        assertTrue(nonTransientExceptions.contains(CommunicationException.class));

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(8, objectPool.getMaxIdlePerKey());
        assertEquals(-1, objectPool.getMaxTotal());
        assertEquals(8, objectPool.getMaxTotalPerKey());
        assertEquals(0, objectPool.getMinIdlePerKey());
        assertEquals(true, objectPool.getBlockWhenExhausted());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME, objectPool.getEvictionPolicyClassName());
        assertEquals(false, objectPool.getFairness());

        // ensures the pool is registered
        ObjectName oname = objectPool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(oname, null);
        assertEquals(1, result.size());

        assertEquals(true, objectPool.getLifo());
        assertEquals(-1L, objectPool.getMaxWaitMillis());
        assertEquals(1000L*60L*30L, objectPool.getMinEvictableIdleTimeMillis());
        assertEquals(3, objectPool.getNumTestsPerEvictionRun());
        assertEquals(-1L, objectPool.getSoftMinEvictableIdleTimeMillis());
        assertEquals(-1L, objectPool.getTimeBetweenEvictionRunsMillis());
        assertEquals(false, objectPool.getTestOnBorrow());
        assertEquals(false, objectPool.getTestOnCreate());
        assertEquals(false, objectPool.getTestOnReturn());
        assertEquals(false, objectPool.getTestWhileIdle());
    }

    @Test
    public void verifyParsePool2SizeSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-configured-poolsize.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(12, objectPool.getMaxTotal());
        assertEquals(20, objectPool.getMaxIdlePerKey());
        assertEquals(10, objectPool.getMaxTotalPerKey());
        assertEquals(13, objectPool.getMaxWaitMillis());
        assertEquals(14, objectPool.getMinIdlePerKey());
        assertEquals(true, objectPool.getBlockWhenExhausted());
        assertEquals("org.springframework.ldap.pool2.DummyEvictionPolicy", objectPool.getEvictionPolicyClassName());
        assertEquals(true, objectPool.getFairness());
        assertEquals(false, objectPool.getLifo());

        // ensures the pool is registered
        ObjectName oname = objectPool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(oname, null);
        assertEquals(1, result.size());
        assertEquals("org.springframework.ldap.pool2:type=ldap-pool,name=test-pool", oname.toString());
    }

    @Test
    public void verifyParsePool2ValidationSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-test-specified.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertNotNull(outerContextSource);

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertNotNull(pooledContextSource);

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertEquals(123, objectPool.getMinEvictableIdleTimeMillis());
        assertEquals(321, objectPool.getTimeBetweenEvictionRunsMillis());
        assertEquals(22, objectPool.getNumTestsPerEvictionRun());
        assertEquals(12, objectPool.getSoftMinEvictableIdleTimeMillis());

        assertEquals(true, objectPool.getTestOnBorrow());
        assertEquals(true, objectPool.getTestOnReturn());
        assertEquals(true, objectPool.getTestOnCreate());
        assertEquals(true, objectPool.getTestWhileIdle());

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPooledObjectFactory");
        DefaultDirContextValidator validator = (DefaultDirContextValidator) getInternalState(objectFactory, "dirContextValidator");
        assertEquals("ou=test", validator.getBase());
        assertEquals("objectclass=person", validator.getFilter());

        SearchControls searchControls = ctx.getBean(SearchControls.class);
        assertEquals("objectclass=person", validator.getFilter());
        assertSame(searchControls, validator.getSearchControls());

        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertEquals(2, nonTransientExceptions.size());
        assertTrue(nonTransientExceptions.contains(CommunicationException.class));
        assertTrue(nonTransientExceptions.contains(CannotProceedException.class));
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPool2AndNativePoolingWillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-with-native.xml");
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPool1AndPool2WillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-with-pool1.xml");
    }
}
