/*
 * Copyright 2002-2015 the original author or authors.
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
package org.springframework.ldap.config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.ClassUtils;

/**
 *
 * @author Rob Winch
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClassUtils.class })
public class LdapNamespaceHandlerTests {

    // LDAP-335
    @Test
    public void repositoryClassNotLoadedIfNotOnClasspath() throws Exception {
        spy(ClassUtils.class);
        when(ClassUtils.class, "isPresent",
                eq(LdapNamespaceHandler.REPOSITORY_CLASS_NAME), any(ClassLoader.class))
            .thenReturn(false);

        LdapNamespaceHandler handler = new LdapNamespaceHandler();

        handler.init();

        Map<String, BeanDefinitionParser> parsers = WhiteboxImpl.getInternalState(handler, "parsers");
        assertFalse(parsers.containsKey(Elements.REPOSITORIES));
    }
}