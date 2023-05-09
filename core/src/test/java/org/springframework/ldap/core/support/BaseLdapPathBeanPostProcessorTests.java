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

package org.springframework.ldap.core.support;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BaseLdapPathBeanPostProcessor}.
 *
 * @author Mattias Hellborg Arthursson
 */
public class BaseLdapPathBeanPostProcessorTests {

	private BaseLdapPathBeanPostProcessor tested;

	private BaseLdapPathAware ldapPathAwareMock;

	private ApplicationContext applicationContextMock;

	private BaseLdapNameAware ldapNameAwareMock;

	@Before
	public void setUp() throws Exception {
		this.tested = new BaseLdapPathBeanPostProcessor();

		this.ldapPathAwareMock = mock(BaseLdapPathAware.class);
		this.ldapNameAwareMock = mock(BaseLdapNameAware.class);

		this.applicationContextMock = mock(ApplicationContext.class);

		this.tested.setApplicationContext(this.applicationContextMock);
	}

	@Test
	public void testPostProcessBeforeInitializationWithLdapPathAwareBasePathSet() throws Exception {
		String expectedPath = "dc=example, dc=com";
		this.tested.setBasePath(new DistinguishedName(expectedPath));

		Object result = this.tested.postProcessBeforeInitialization(this.ldapPathAwareMock, "someName");

		verify(this.ldapPathAwareMock).setBaseLdapPath(new DistinguishedName(expectedPath));

		assertThat(result).isSameAs(this.ldapPathAwareMock);
	}

	@Test
	public void testPostProcessBeforeInitializationWithLdapNameAwareBasePathSet() throws Exception {
		String expectedPath = "dc=example, dc=com";
		this.tested.setBasePath(expectedPath);

		Object result = this.tested.postProcessBeforeInitialization(this.ldapNameAwareMock, "someName");

		verify(this.ldapNameAwareMock).setBaseLdapPath(LdapUtils.newLdapName(expectedPath));

		assertThat(result).isSameAs(this.ldapNameAwareMock);
	}

	@Test
	public void testPostProcessBeforeInitializationWithLdapPathAwareNoBasePathSet() throws Exception {
		final LdapContextSource expectedContextSource = new LdapContextSource();
		String expectedPath = "dc=example, dc=com";
		expectedContextSource.setBase(expectedPath);

		this.tested = new BaseLdapPathBeanPostProcessor() {
			BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
				return expectedContextSource;
			}
		};

		Object result = this.tested.postProcessBeforeInitialization(this.ldapPathAwareMock, "someName");

		verify(this.ldapPathAwareMock).setBaseLdapPath(new DistinguishedName(expectedPath));

		assertThat(result).isSameAs(this.ldapPathAwareMock);
	}

	@Test
	public void testPostProcessBeforeInitializationWithLdapNameAwareNoBasePathSet() throws Exception {
		final LdapContextSource expectedContextSource = new LdapContextSource();
		String expectedPath = "dc=example, dc=com";
		expectedContextSource.setBase(expectedPath);

		this.tested = new BaseLdapPathBeanPostProcessor() {
			BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
				return expectedContextSource;
			}
		};

		Object result = this.tested.postProcessBeforeInitialization(this.ldapNameAwareMock, "someName");

		verify(this.ldapNameAwareMock).setBaseLdapPath(LdapUtils.newLdapName(expectedPath));

		assertThat(result).isSameAs(this.ldapNameAwareMock);
	}

	@Test
	public void testGetAbstractContextSourceFromApplicationContext() throws Exception {
		when(this.applicationContextMock.getBeanNamesForType(BaseLdapPathSource.class))
				.thenReturn(new String[] { "contextSource" });
		final LdapContextSource expectedContextSource = new LdapContextSource();

		HashMap<String, BaseLdapPathSource> expectedBeans = new HashMap<String, BaseLdapPathSource>() {
			{
				put("dummy", expectedContextSource);
			}
		};
		when(this.applicationContextMock.getBeansOfType(BaseLdapPathSource.class)).thenReturn(expectedBeans);

		BaseLdapPathSource result = this.tested.getBaseLdapPathSourceFromApplicationContext();

		assertThat(result).isSameAs(expectedContextSource);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testGetAbstractContextSourceFromApplicationContextNoContextSource() throws Exception {
		when(this.applicationContextMock.getBeanNamesForType(BaseLdapPathSource.class)).thenReturn(new String[0]);

		this.tested.getBaseLdapPathSourceFromApplicationContext();
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testGetAbstractContextSourceFromApplicationContextTwoContextSources() throws Exception {
		when(this.applicationContextMock.getBeanNamesForType(BaseLdapPathSource.class)).thenReturn(new String[2]);

		this.tested.getBaseLdapPathSourceFromApplicationContext();
	}

	@Test
	public void testGetAbstractContextSourceFromApplicationContextTwoContextSourcesAndSpecifiedName() throws Exception {
		LdapContextSource expectedContextSource = new LdapContextSource();

		this.tested.setBaseLdapPathSourceName("myContextSource");
		when(this.applicationContextMock.getBean("myContextSource")).thenReturn(expectedContextSource);

		this.tested.getBaseLdapPathSourceFromApplicationContext();
	}

}
