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

package org.springframework.ldap.itest.core.support;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link org.springframework.ldap.core.support.BaseLdapPathBeanPostProcessor}.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class BaseLdapPathBeanPostprocessorNamespaceConfigITest {

	@Test
	public void testPostProcessBeforeInitializationWithNamespaceConfig() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/conf/baseLdapPathPostProcessorNamespaceTestContext.xml");
		DummyBaseLdapPathAware tested = ctx.getBean(DummyBaseLdapPathAware.class);

		DistinguishedName base = tested.getBase();
		assertThat(base).isNotNull();
		assertThat(base).isEqualTo(new DistinguishedName("dc=jayway,dc=se"));

        DummyBaseLdapNameAware otherTested = ctx.getBean(DummyBaseLdapNameAware.class);
        assertThat(otherTested.getBaseLdapPath()).isEqualTo(LdapUtils.newLdapName("dc=jayway,dc=se"));
    }

    @Test
    public void testPostProcessBeforeInitializationWithNamespaceConfigAndPooling() throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/conf/baseLdapPathPostProcessorPoolingNamespaceTestContext.xml");
        DummyBaseLdapPathAware tested = ctx.getBean(DummyBaseLdapPathAware.class);

        DistinguishedName base = tested.getBase();
        assertThat(base).isNotNull();
        assertThat(base).isEqualTo(new DistinguishedName("dc=jayway,dc=se"));

        DummyBaseLdapNameAware otherTested = ctx.getBean(DummyBaseLdapNameAware.class);
        assertThat(otherTested.getBaseLdapPath()).isEqualTo(LdapUtils.newLdapName("dc=jayway,dc=se"));
    }


}
