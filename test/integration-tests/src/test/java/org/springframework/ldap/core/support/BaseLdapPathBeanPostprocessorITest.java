/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.core.support;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.itest.core.support.DummyBaseLdapNameAware;
import org.springframework.ldap.itest.core.support.DummyBaseLdapPathAware;
import org.springframework.ldap.support.LdapUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Integration tests for {@link BaseLdapPathBeanPostProcessor}.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class BaseLdapPathBeanPostprocessorITest {

	@Test
	public void testPostProcessBeforeInitialization() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/conf/baseLdapPathPostProcessorTestContext.xml");
		DummyBaseLdapPathAware tested = ctx.getBean(DummyBaseLdapPathAware.class);

		DistinguishedName base = tested.getBase();
		assertNotNull(base);
		assertEquals(new DistinguishedName("dc=jayway,dc=se"), base);

        DummyBaseLdapNameAware otherTested = ctx.getBean(DummyBaseLdapNameAware.class);
        assertEquals(LdapUtils.newLdapName("dc=jayway,dc=se"), otherTested.getBaseLdapPath());
    }

	@Test
	public void testPostProcessBeforeInitializationMultipleContextSources() throws Exception {
		try {
			new ClassPathXmlApplicationContext("/conf/baseLdapPathPostProcessorMultiContextSourceTestContext.xml");
			fail("BeanCreationException expected");
		}
		catch (BeanCreationException expected) {
			Throwable cause = expected.getCause();
			assertTrue(cause instanceof NoSuchBeanDefinitionException);
		}
	}

	@Test
	public void testPostProcessBeforeInitializationMultipleContextSourcesOneSpecified() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/conf/baseLdapPathPostProcessorMultiContextSourceOneSpecTestContext.xml");
		DummyBaseLdapPathAware tested = (DummyBaseLdapPathAware) ctx.getBean("dummyBaseContextAware");

		DistinguishedName base = tested.getBase();
		assertNotNull(base);
		assertEquals(new DistinguishedName("cn=john doe,dc=jayway,dc=se"), base);
	}

	@Test
	public void testPostProcessBeforeInitializationNoContextSource() throws Exception {
		try {
			new ClassPathXmlApplicationContext("/conf/baseLdapPathPostProcessorNoContextSourceTestContext.xml");
			fail("BeanCreationException expected");
		}
		catch (BeanCreationException expected) {
			Throwable cause = expected.getCause();
			assertTrue(cause instanceof NoSuchBeanDefinitionException);
		}
	}

	@Test
	public void testPostProcessBeforeInitializationBaseSetInProperty() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/conf/baseLdapPathPostProcessorPropertyOverrideTestContext.xml");
		DummyBaseLdapPathAware tested = (DummyBaseLdapPathAware) ctx.getBean("dummyBaseContextAware");

		DistinguishedName base = tested.getBase();
		assertNotNull(base);
		assertEquals(new DistinguishedName("cn=john doe"), base);
	}

	@Test
	public void testPostProcessBeforeInitializationTransactionProxy() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/conf/baseLdapPathPostProcessorTransactionTestContext.xml");
		DummyBaseLdapPathAware tested = (DummyBaseLdapPathAware) ctx.getBean("dummyBaseContextAware");

		DistinguishedName base = tested.getBase();
		assertNotNull(base);
		assertEquals(new DistinguishedName("dc=jayway,dc=se"), base);
	}
}
