/*
 * Copyright 2005-2016 the original author or authors.
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
import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(outerContextSource).isNotNull();
        assertThat(ldapTemplate).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertThat(LdapUtils.emptyLdapName()).isEqualTo(getInternalState(contextSource, "base"));
        assertThat("uid=admin").isEqualTo(getInternalState(contextSource, "userDn"));
        assertThat("apassword").isEqualTo(getInternalState(contextSource, "password"));
        assertThat(new String[]{"ldap://localhost:389"}).isEqualTo((Object[]) getInternalState(contextSource, "urls"));
        assertThat(Boolean.FALSE).isEqualTo(getInternalState(contextSource, "pooled"));
        assertThat(Boolean.FALSE).isEqualTo(getInternalState(contextSource, "anonymousReadOnly"));
        assertThat(getInternalState(contextSource, "referral")).isNull();

        assertThat(outerContextSource).isSameAs(getInternalState(ldapTemplate, "contextSource"));
        assertThat(Boolean.FALSE).isEqualTo(getInternalState(ldapTemplate, "ignorePartialResultException"));
        assertThat(Boolean.FALSE).isEqualTo(getInternalState(ldapTemplate, "ignoreNameNotFoundException"));
        assertThat(0).isEqualTo(getInternalState(ldapTemplate, "defaultCountLimit"));
        assertThat(0).isEqualTo(getInternalState(ldapTemplate, "defaultTimeLimit"));
        assertThat(SearchControls.SUBTREE_SCOPE).isEqualTo(getInternalState(ldapTemplate, "defaultSearchScope"));
    }

    @Test
    public void verifyThatAnonymousReadOnlyContextWillNotBeWrappedInProxy() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-anonymous-read-only.xml");
        ContextSource contextSource = ctx.getBean(ContextSource.class);

        assertThat(contextSource).isNotNull();
        assertThat(contextSource instanceof LdapContextSource).isTrue();
        assertThat(Boolean.TRUE).isEqualTo(getInternalState(contextSource, "anonymousReadOnly"));
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

        assertThat(outerContextSource).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertThat(authenticationSource).isSameAs(getInternalState(contextSource, "authenticationSource"));
        assertThat(authenticationStrategy).isSameAs(getInternalState(contextSource, "authenticationStrategy"));
        assertThat(baseEnv).isEqualTo(getInternalState(contextSource, "baseEnv"));
    }

    @Test
    public void verifyParseWithCustomValues() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-values.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);
        DirContextAuthenticationStrategy authenticationStrategy = ctx.getBean(DirContextAuthenticationStrategy.class);

        assertThat(outerContextSource).isNotNull();
        assertThat(ldapTemplate).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertThat(LdapUtils.newLdapName("dc=261consulting,dc=com")).isEqualTo(getInternalState(contextSource, "base"));
        assertThat("uid=admin").isEqualTo(getInternalState(contextSource, "userDn"));
        assertThat("apassword").isEqualTo(getInternalState(contextSource, "password"));
        assertThat(new String[]{"ldap://localhost:389"}).isEqualTo((Object[]) getInternalState(contextSource, "urls"));
        assertThat(Boolean.TRUE).isEqualTo(getInternalState(contextSource, "pooled"));
        assertThat(Boolean.FALSE).isEqualTo(getInternalState(contextSource, "anonymousReadOnly"));
        assertThat("follow").isEqualTo(getInternalState(contextSource, "referral"));
        assertThat(authenticationStrategy).isSameAs(getInternalState(contextSource, "authenticationStrategy"));

        assertThat(outerContextSource).isSameAs(getInternalState(ldapTemplate, "contextSource"));
        assertThat(Boolean.TRUE).isEqualTo(getInternalState(ldapTemplate, "ignorePartialResultException"));
        assertThat(Boolean.TRUE).isEqualTo(getInternalState(ldapTemplate, "ignoreNameNotFoundException"));
        assertThat(100).isEqualTo(getInternalState(ldapTemplate, "defaultCountLimit"));
        assertThat(200).isEqualTo(getInternalState(ldapTemplate, "defaultTimeLimit"));
        assertThat(SearchControls.OBJECT_SCOPE).isEqualTo(getInternalState(ldapTemplate, "defaultSearchScope"));
    }

    @Test
    public void supportsSpel() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-spel.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertThat(outerContextSource).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertThat(LdapUtils.newLdapName("dc=261consulting,dc=com")).isEqualTo(getInternalState(contextSource, "base"));
        assertThat("uid=admin").isEqualTo(getInternalState(contextSource, "userDn"));
        assertThat("apassword").isEqualTo(getInternalState(contextSource, "password"));
        assertThat(new String[]{"ldap://localhost:389"}).isEqualTo((Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void supportsSpelMultiUrls() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-spel-multiurls.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertThat(outerContextSource).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertArrayEquals(new String[] { "ldap://a.localhost:389", "ldap://b.localhost:389" },
                (Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void supportsMultipleUrls() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-multiurls.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);

        assertThat(outerContextSource).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertArrayEquals(new String[] { "ldap://a.localhost:389", "ldap://b.localhost:389" },
                (Object[]) getInternalState(contextSource, "urls"));

    }

    @Test
    public void verifyParseWithDefaultTransactions() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertThat(outerContextSource).isNotNull();
        assertThat(transactionManager).isNotNull();

        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();
        ContextSource contextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();

        assertThat(transactionManager instanceof ContextSourceTransactionManager).isTrue();

        Object delegate = getInternalState(transactionManager, "delegate");
        assertThat(contextSource).isSameAs(getInternalState(delegate, "contextSource"));
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertThat(renamingStrategy instanceof DefaultTempEntryRenamingStrategy).isTrue();
        assertThat("_temp").isEqualTo(getInternalState(renamingStrategy, "tempSuffix"));
    }

    @Test
    public void verifyParseTransactionWithDataSource() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-datasource.xml");
        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertThat(transactionManager instanceof ContextSourceAndDataSourceTransactionManager).isTrue();
    }

    @Test
    public void verifyParseTransactionsWithDefaultStrategyAndSuffix() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-defaults-with-suffix.xml");

        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertThat(transactionManager).isNotNull();
        assertThat(transactionManager instanceof ContextSourceTransactionManager).isTrue();

        Object delegate = getInternalState(transactionManager, "delegate");
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertThat(renamingStrategy instanceof DefaultTempEntryRenamingStrategy).isTrue();
        assertThat("_thisisthesuffix").isEqualTo(getInternalState(renamingStrategy, "tempSuffix"));
    }

    @Test
    public void verifyParseTransactionsWithDifferentSubtreeStrategy() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-transactional-different-subtree.xml");

        PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);

        assertThat(transactionManager).isNotNull();
        assertThat(transactionManager instanceof ContextSourceTransactionManager).isTrue();

        Object delegate = getInternalState(transactionManager, "delegate");
        TempEntryRenamingStrategy renamingStrategy =
                (TempEntryRenamingStrategy) getInternalState(delegate, "renamingStrategy");

        assertThat(renamingStrategy instanceof DifferentSubtreeTempEntryRenamingStrategy).isTrue();
        assertThat(LdapUtils.newLdapName("ou=temp")).isEqualTo(getInternalState(renamingStrategy, "subtreeNode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyParsePoolingDefaults() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();
        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();
        assertThat(pooledContextSource instanceof PoolingContextSource).isTrue();

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPoolableObjectFactory");
        assertThat(getInternalState(objectFactory, "contextSource")).isNotNull();
        assertThat(getInternalState(objectFactory, "dirContextValidator")).isNull();
        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertThat(nonTransientExceptions).hasSize(1);
        assertThat(nonTransientExceptions.contains(CommunicationException.class)).isTrue();

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMaxActive()).isEqualTo(8);
        assertThat(objectPool.getMaxTotal()).isEqualTo(-1);
        assertThat(objectPool.getMaxIdle()).isEqualTo(8);
        assertThat(objectPool.getMaxWait()).isEqualTo(-1);
        assertThat(objectPool.getMinIdle()).isEqualTo(0);
        assertThat(objectPool.getWhenExhaustedAction()).isEqualTo((byte)1);
    }

    @Test
    public void verifyParsePoolingSizeSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-configured-poolsize.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMaxActive()).isEqualTo(10);
        assertThat(objectPool.getMaxTotal()).isEqualTo(12);
        assertThat(objectPool.getMaxIdle()).isEqualTo(11);
        assertThat(objectPool.getMaxWait()).isEqualTo(13);
        assertThat(objectPool.getMinIdle()).isEqualTo(14);
        assertThat(objectPool.getWhenExhaustedAction()).isEqualTo((byte)0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyParsePoolingValidationSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-test-specified.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMinEvictableIdleTimeMillis()).isEqualTo(123);
        assertThat(objectPool.getTimeBetweenEvictionRunsMillis()).isEqualTo(321);
        assertThat(objectPool.getNumTestsPerEvictionRun()).isEqualTo(22);

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPoolableObjectFactory");
        DefaultDirContextValidator validator = (DefaultDirContextValidator) getInternalState(objectFactory, "dirContextValidator");
        assertThat(validator.getBase()).isEqualTo("ou=test");
        assertThat(validator.getFilter()).isEqualTo("objectclass=person");

        SearchControls searchControls = ctx.getBean(SearchControls.class);
        assertThat(validator.getFilter()).isEqualTo("objectclass=person");
        assertThat(validator.getSearchControls()).isSameAs(searchControls);

        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertThat(nonTransientExceptions).hasSize(2);
        assertThat(nonTransientExceptions.contains(CommunicationException.class)).isTrue();
        assertThat(nonTransientExceptions.contains(CannotProceedException.class)).isTrue();
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPoolingAndNativePoolingWillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-with-native.xml");
    }

    @Test
    public void verifyAutomaticRepositorySupport() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-with-repositories.xml");
        DummyLdapRepository repository = ctx.getBean(DummyLdapRepository.class);

        assertThat(repository).isNotNull();
    }

    @Test
    public void verifyParsePooling2Defaults() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling2-defaults.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();
        assertThat(outerContextSource instanceof TransactionAwareContextSourceProxy).isTrue();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();
        assertThat(pooledContextSource instanceof PooledContextSource).isTrue();
        assertThat(getInternalState(pooledContextSource, "poolConfig")).isNotNull();

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPooledObjectFactory");
        assertThat(getInternalState(objectFactory, "contextSource")).isNotNull();
        assertThat(getInternalState(objectFactory, "dirContextValidator")).isNull();
        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertThat(nonTransientExceptions).hasSize(1);
        assertThat(nonTransientExceptions.contains(CommunicationException.class)).isTrue();

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMaxIdlePerKey()).isEqualTo(8);
        assertThat(objectPool.getMaxTotal()).isEqualTo(-1);
        assertThat(objectPool.getMaxTotalPerKey()).isEqualTo(8);
        assertThat(objectPool.getMinIdlePerKey()).isEqualTo(0);
        assertThat(objectPool.getBlockWhenExhausted()).isEqualTo(true);
        assertThat(objectPool.getEvictionPolicyClassName()).isEqualTo(GenericKeyedObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME);
        assertThat(objectPool.getFairness()).isEqualTo(false);

        // ensures the pool is registered
        ObjectName oname = objectPool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(oname, null);
        assertThat(result).hasSize(1);

        assertThat(objectPool.getLifo()).isEqualTo(true);
        assertThat(objectPool.getMaxWaitMillis()).isEqualTo(-1L);
        assertThat(objectPool.getMinEvictableIdleTimeMillis()).isEqualTo(1000L*60L*30L);
        assertThat(objectPool.getNumTestsPerEvictionRun()).isEqualTo(3);
        assertThat(objectPool.getSoftMinEvictableIdleTimeMillis()).isEqualTo(-1L);
        assertThat(objectPool.getTimeBetweenEvictionRunsMillis()).isEqualTo(-1L);
        assertThat(objectPool.getTestOnBorrow()).isEqualTo(false);
        assertThat(objectPool.getTestOnCreate()).isEqualTo(false);
        assertThat(objectPool.getTestOnReturn()).isEqualTo(false);
        assertThat(objectPool.getTestWhileIdle()).isEqualTo(false);
    }

    @Test
    public void verifyParsePool2SizeSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-configured-poolsize.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMaxTotal()).isEqualTo(12);
        assertThat(objectPool.getMaxIdlePerKey()).isEqualTo(20);
        assertThat(objectPool.getMaxTotalPerKey()).isEqualTo(10);
        assertThat(objectPool.getMaxWaitMillis()).isEqualTo(13);
        assertThat(objectPool.getMinIdlePerKey()).isEqualTo(14);
        assertThat(objectPool.getBlockWhenExhausted()).isEqualTo(true);
        assertThat(objectPool.getEvictionPolicyClassName()).isEqualTo("org.springframework.ldap.pool2.DummyEvictionPolicy");
        assertThat(objectPool.getFairness()).isEqualTo(true);
        assertThat(objectPool.getLifo()).isEqualTo(false);

        // ensures the pool is registered
        ObjectName oname = objectPool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(oname, null);
        assertThat(result).hasSize(1);
        assertThat(oname.toString()).isEqualTo("org.springframework.ldap.pool2:type=ldap-pool,name=test-pool");
    }

    @Test
    public void verifyParsePool2ValidationSet() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-test-specified.xml");

        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getMinEvictableIdleTimeMillis()).isEqualTo(123);
        assertThat(objectPool.getTimeBetweenEvictionRunsMillis()).isEqualTo(321);
        assertThat(objectPool.getNumTestsPerEvictionRun()).isEqualTo(22);
        assertThat(objectPool.getSoftMinEvictableIdleTimeMillis()).isEqualTo(12);

        assertThat(objectPool.getTestOnBorrow()).isEqualTo(true);
        assertThat(objectPool.getTestOnReturn()).isEqualTo(true);
        assertThat(objectPool.getTestOnCreate()).isEqualTo(true);
        assertThat(objectPool.getTestWhileIdle()).isEqualTo(true);

        Object objectFactory = getInternalState(pooledContextSource, "dirContextPooledObjectFactory");
        org.springframework.ldap.pool2.validation.DefaultDirContextValidator validator =
                (org.springframework.ldap.pool2.validation.DefaultDirContextValidator) getInternalState(objectFactory, "dirContextValidator");
        assertThat(validator.getBase()).isEqualTo("ou=test");
        assertThat(validator.getFilter()).isEqualTo("objectclass=person");

        SearchControls searchControls = ctx.getBean(SearchControls.class);
        assertThat(validator.getFilter()).isEqualTo("objectclass=person");
        assertThat(validator.getSearchControls()).isSameAs(searchControls);

        Set<Class<? extends Throwable>> nonTransientExceptions =
                (Set<Class<? extends Throwable>>) getInternalState(objectFactory, "nonTransientExceptions");
        assertThat(nonTransientExceptions).hasSize(2);
        assertThat(nonTransientExceptions.contains(CommunicationException.class)).isTrue();
        assertThat(nonTransientExceptions.contains(CannotProceedException.class)).isTrue();
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPool2AndNativePoolingWillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-with-native.xml");
    }

    @Test(expected = BeansException.class)
    public void verifyParseWithPool1AndPool2WillFail() {
        new ClassPathXmlApplicationContext("/ldap-namespace-config-pool2-with-pool1.xml");
    }

    @Test
    public void verifyParsePoolWithPlaceholders() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling-config-with-placeholders.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        GenericKeyedObjectPool objectPool = (GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getTimeBetweenEvictionRunsMillis()).isEqualTo(10);
        assertThat(objectPool.getMinEvictableIdleTimeMillis()).isEqualTo(20);
        assertThat(objectPool.getMaxWait()).isEqualTo(10);
        assertThat(objectPool.getMaxTotal()).isEqualTo(11);
        assertThat(objectPool.getMaxActive()).isEqualTo(15);
        assertThat(objectPool.getMinIdle()).isEqualTo(16);
        assertThat(objectPool.getMaxIdle()).isEqualTo(17);
        assertThat(objectPool.getNumTestsPerEvictionRun()).isEqualTo(18);
    }

    @Test
    public void verifyParsePool2WithPlaceholders() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-namespace-config-pooling2-config-with-placeholders.xml");
        ContextSource outerContextSource = ctx.getBean(ContextSource.class);
        assertThat(outerContextSource).isNotNull();

        ContextSource pooledContextSource = ((TransactionAwareContextSourceProxy) outerContextSource).getTarget();
        assertThat(pooledContextSource).isNotNull();

        org.apache.commons.pool2.impl.GenericKeyedObjectPool objectPool =
                (org.apache.commons.pool2.impl.GenericKeyedObjectPool) getInternalState(pooledContextSource, "keyedObjectPool");
        assertThat(objectPool.getTimeBetweenEvictionRunsMillis()).isEqualTo(10);
        assertThat(objectPool.getMinEvictableIdleTimeMillis()).isEqualTo(20);
        assertThat(objectPool.getMaxWaitMillis()).isEqualTo(10);
        assertThat(objectPool.getMaxTotal()).isEqualTo(11);
        assertThat(objectPool.getMinIdlePerKey()).isEqualTo(12);
        assertThat(objectPool.getMaxIdlePerKey()).isEqualTo(13);
        assertThat(objectPool.getMaxTotalPerKey()).isEqualTo(14);
        assertThat(objectPool.getNumTestsPerEvictionRun()).isEqualTo(18);
    }
}
