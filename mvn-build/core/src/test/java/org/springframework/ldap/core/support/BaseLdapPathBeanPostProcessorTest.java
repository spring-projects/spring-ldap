/*
 * Copyright 2005-2007 the original author or authors.
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

import org.easymock.MockControl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DistinguishedName;

import junit.framework.TestCase;

/**
 * Unit tests for {@link BaseLdapPathBeanPostProcessor}.
 * 
 * @author Mattias Arthursson
 */
public class BaseLdapPathBeanPostProcessorTest extends TestCase {

	private BaseLdapPathBeanPostProcessor tested;

	private MockControl ldapPathAwareControl;

	private BaseLdapPathAware ldapPathAwareMock;

	private MockControl applicationContextControl;

	private ApplicationContext applicationContextMock;

	protected void setUp() throws Exception {
		tested = new BaseLdapPathBeanPostProcessor();

		ldapPathAwareControl = MockControl.createControl(BaseLdapPathAware.class);
		ldapPathAwareMock = (BaseLdapPathAware) ldapPathAwareControl.getMock();

		applicationContextControl = MockControl.createControl(ApplicationContext.class);
		applicationContextMock = (ApplicationContext) applicationContextControl.getMock();

		tested.setApplicationContext(applicationContextMock);
	}

	protected void tearDown() throws Exception {
		tested = null;
		ldapPathAwareControl = null;
		ldapPathAwareMock = null;

		applicationContextControl = null;
		applicationContextMock = null;
	}

	public void testPostProcessBeforeInitializationWithLdapPathAwareBasePathSet() throws Exception {
		String expectedPath = "dc=example, dc=com";
		tested.setBasePath(new DistinguishedName(expectedPath));

		ldapPathAwareMock.setBaseLdapPath(new DistinguishedName(expectedPath));

		ldapPathAwareControl.replay();

		Object result = tested.postProcessBeforeInitialization(ldapPathAwareMock, "someName");

		ldapPathAwareControl.verify();

		assertSame(ldapPathAwareMock, result);
	}

	public void testPostProcessBeforeInitializationWithLdapPathAwareNoBasePathSet() throws Exception {
		final LdapContextSource expectedContextSource = new LdapContextSource();
		String expectedPath = "dc=example, dc=com";
		expectedContextSource.setBase(expectedPath);

		tested = new BaseLdapPathBeanPostProcessor() {
			BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
				return expectedContextSource;
			}
		};

		ldapPathAwareMock.setBaseLdapPath(new DistinguishedName(expectedPath));

		ldapPathAwareControl.replay();

		Object result = tested.postProcessBeforeInitialization(ldapPathAwareMock, "someName");

		ldapPathAwareControl.verify();

		assertSame(ldapPathAwareMock, result);
	}

	public void testGetAbstractContextSourceFromApplicationContext() throws Exception {
		applicationContextControl.expectAndReturn(applicationContextMock
				.getBeanNamesForType(BaseLdapPathSource.class), new String[] { "contextSource" });
		LdapContextSource expectedContextSource = new LdapContextSource();
		applicationContextControl.expectAndReturn(applicationContextMock.getBean("contextSource"),
				expectedContextSource);

		applicationContextControl.replay();

		BaseLdapPathSource result = tested.getBaseLdapPathSourceFromApplicationContext();

		applicationContextControl.verify();
		assertSame(expectedContextSource, result);
	}

	public void testGetAbstractContextSourceFromApplicationContextNoContextSource() throws Exception {
		applicationContextControl.expectAndReturn(applicationContextMock
				.getBeanNamesForType(BaseLdapPathSource.class), new String[0]);

		applicationContextControl.replay();

		try {
			tested.getBaseLdapPathSourceFromApplicationContext();
			fail("NoSuchBeanDefinitionException expected");
		}
		catch (NoSuchBeanDefinitionException expected) {
			assertTrue(true);
		}
		applicationContextControl.verify();
	}

	public void testGetAbstractContextSourceFromApplicationContextTwoContextSources() throws Exception {
		applicationContextControl.expectAndReturn(applicationContextMock
				.getBeanNamesForType(BaseLdapPathSource.class), new String[2]);

		applicationContextControl.replay();

		try {
			tested.getBaseLdapPathSourceFromApplicationContext();
			fail("NoSuchBeanDefinitionException expected");
		}
		catch (NoSuchBeanDefinitionException expected) {
			assertTrue(true);
		}
		applicationContextControl.verify();
	}

	public void testGetAbstractContextSourceFromApplicationContextTwoContextSourcesAndSpecifiedName() throws Exception {
		LdapContextSource expectedContextSource = new LdapContextSource();

		tested.setBaseLdapPathSourceName("myContextSource");
		applicationContextControl.expectAndReturn(applicationContextMock.getBean("myContextSource"),
				expectedContextSource);

		applicationContextControl.replay();

		tested.getBaseLdapPathSourceFromApplicationContext();
		applicationContextControl.verify();
	}
}
