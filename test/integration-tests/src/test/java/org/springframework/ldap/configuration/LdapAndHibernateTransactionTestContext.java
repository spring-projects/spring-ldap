/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.configuration;

import java.util.ArrayList;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.DummyDaoLdapAndHibernateImpl;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceAndHibernateTransactionManager;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;


/**
 * Generated Java based configuration
 * 
 */
@Configuration
public class LdapAndHibernateTransactionTestContext {


    @Bean("transactionManager")
    public ContextSourceAndHibernateTransactionManager transactionManager(
        @Qualifier("sessionFactory")
        LocalSessionFactoryBean sessionFactory,
        @Qualifier("transactedContextSource")
        TransactionAwareContextSourceProxy transactedContextSource) {
        ContextSourceAndHibernateTransactionManager bean = new ContextSourceAndHibernateTransactionManager();
        bean.setSessionFactory(sessionFactory.getObject());
        bean.setContextSource(transactedContextSource);
        bean.setRenamingStrategy(new DefaultTempEntryRenamingStrategy());
        return bean;
    }

    @Bean("transactedContextSource")
    public TransactionAwareContextSourceProxy transactedContextSource(
        @Qualifier("contextSource")
        LdapContextSource contextSource) {
        return new TransactionAwareContextSourceProxy(contextSource);
    }

    @Bean("dummyDao")
    public DummyDaoLdapAndHibernateImpl dummyDao(
        @Qualifier("ldapTemplate")
        LdapTemplate ldapTemplate,
        @Qualifier("sessionFactory")
        LocalSessionFactoryBean sessionFactory) {
        DummyDaoLdapAndHibernateImpl bean = new DummyDaoLdapAndHibernateImpl();
        bean.setLdapTemplate(ldapTemplate);
        bean.setSessionFactory(sessionFactory.getObject());
        return bean;
    }

    @Bean("hibernateTemplate")
    public HibernateTemplate hibernateTemplate(
        @Qualifier("sessionFactory")
        LocalSessionFactoryBean sessionFactory) {
        HibernateTemplate bean = new HibernateTemplate();
        bean.setSessionFactory(sessionFactory.getObject());
        return bean;
    }

    @Bean("dataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource bean = new DriverManagerDataSource();
        bean.setDriverClassName("org.hsqldb.jdbcDriver");
        bean.setUrl("jdbc:hsqldb:mem:aname");
        bean.setUsername("sa");
        bean.setPassword("");
        return bean;
    }

    @Bean("sessionFactory")
    public LocalSessionFactoryBean sessionFactory(
        @Qualifier("dataSource")
        DriverManagerDataSource dataSource) {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        ArrayList<String> list0 = new ArrayList<String>();
        list0 .add("conf/OrgPerson.hbm.xml");
        bean.setMappingResources(list0.toArray(new String[]{}));
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create");
        bean.setHibernateProperties(properties);
        return bean;
    }

    @Bean("ldapTemplate")
    public LdapTemplate ldapTemplate(
        @Qualifier("transactedContextSource")
        TransactionAwareContextSourceProxy transactedContextSource) {
        return new LdapTemplate(transactedContextSource);
    }

}
