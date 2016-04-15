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
package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BaseLdapPathBeanPostProcessor}.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class BaseLdapPathBeanPostProcessorTest {

	private BaseLdapPathBeanPostProcessor tested;
	private BaseLdapPathAware ldapPathAwareMock;
	private ApplicationContext applicationContextMock;
    private BaseLdapNameAware ldapNameAwareMock;

    @Before
	public void setUp() throws Exception {
		tested = new BaseLdapPathBeanPostProcessor();

		ldapPathAwareMock = mock(BaseLdapPathAware.class);
        ldapNameAwareMock = mock(BaseLdapNameAware.class);

        applicationContextMock = mock(ApplicationContext.class);

		tested.setApplicationContext(applicationContextMock);
	}

    @Test
	public void testPostProcessBeforeInitializationWithLdapPathAwareBasePathSet() throws Exception {
		String expectedPath = "dc=example, dc=com";
		tested.setBasePath(new DistinguishedName(expectedPath));

		Object result = tested.postProcessBeforeInitialization(ldapPathAwareMock, "someName");

		verify(ldapPathAwareMock).setBaseLdapPath(new DistinguishedName(expectedPath));

		assertThat(result).isSameAs(ldapPathAwareMock);
	}

    @Test
    public void testPostProcessBeforeInitializationWithLdapNameAwareBasePathSet() throws Exception {
        String expectedPath = "dc=example, dc=com";
        tested.setBasePath(expectedPath);

        Object result = tested.postProcessBeforeInitialization(ldapNameAwareMock, "someName");

        verify(ldapNameAwareMock).setBaseLdapPath(LdapUtils.newLdapName(expectedPath));

        assertThat(result).isSameAs(ldapNameAwareMock);
    }


    @Test
	public void testPostProcessBeforeInitializationWithLdapPathAwareNoBasePathSet() throws Exception {
		final LdapContextSource expectedContextSource = new LdapContextSource();
		String expectedPath = "dc=example, dc=com";
		expectedContextSource.setBase(expectedPath);

		tested = new BaseLdapPathBeanPostProcessor() {
			BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
				return expectedContextSource;
			}
		};

		Object result = tested.postProcessBeforeInitialization(ldapPathAwareMock, "someName");

        verify(ldapPathAwareMock).setBaseLdapPath(new DistinguishedName(expectedPath));

		assertThat(result).isSameAs(ldapPathAwareMock);
	}

    @Test
    public void testPostProcessBeforeInitializationWithLdapNameAwareNoBasePathSet() throws Exception {
        final LdapContextSource expectedContextSource = new LdapContextSource();
        String expectedPath = "dc=example, dc=com";
        expectedContextSource.setBase(expectedPath);

        tested = new BaseLdapPathBeanPostProcessor() {
            BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
                return expectedContextSource;
            }
        };

        Object result = tested.postProcessBeforeInitialization(ldapNameAwareMock, "someName");

        verify(ldapNameAwareMock).setBaseLdapPath(LdapUtils.newLdapName(expectedPath));

        assertThat(result).isSameAs(ldapNameAwareMock);
    }

    @Test
	public void testGetAbstractContextSourceFromApplicationContext() throws Exception {
		when(applicationContextMock.getBeanNamesForType(BaseLdapPathSource.class))
                .thenReturn(new String[]{"contextSource"});
		final LdapContextSource expectedContextSource = new LdapContextSource();

        HashMap<String, BaseLdapPathSource> expectedBeans = new HashMap<String, BaseLdapPathSource>() {{
            put("dummy", expectedContextSource);
        }};
        when(applicationContextMock.getBeansOfType(BaseLdapPathSource.class)).thenReturn(expectedBeans);

		BaseLdapPathSource result = tested.getBaseLdapPathSourceFromApplicationContext();

		assertThat(result).isSameAs(expectedContextSource);
	}

    @Test(expected = NoSuchBeanDefinitionException.class)
	public void testGetAbstractContextSourceFromApplicationContextNoContextSource() throws Exception {
		when(applicationContextMock.getBeanNamesForType(BaseLdapPathSource.class))
                .thenReturn(new String[0]);

        tested.getBaseLdapPathSourceFromApplicationContext();
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
	public void testGetAbstractContextSourceFromApplicationContextTwoContextSources() throws Exception {
		when(applicationContextMock
				.getBeanNamesForType(BaseLdapPathSource.class)).thenReturn(new String[2]);

        tested.getBaseLdapPathSourceFromApplicationContext();
    }

    @Test
	public void testGetAbstractContextSourceFromApplicationContextTwoContextSourcesAndSpecifiedName() throws Exception {
		LdapContextSource expectedContextSource = new LdapContextSource();

		tested.setBaseLdapPathSourceName("myContextSource");
		when(applicationContextMock.getBean("myContextSource")).thenReturn(expectedContextSource);

		tested.getBaseLdapPathSourceFromApplicationContext();
	}
}
