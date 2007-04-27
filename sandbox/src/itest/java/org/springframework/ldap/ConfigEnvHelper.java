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

package org.springframework.ldap;

import java.io.File;
import java.util.Hashtable;

import javax.naming.Context;

import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper class to simplify Spring configuration of ApacheDS.
 * 
 * @author Mattias Arthursson
 */
public class ConfigEnvHelper implements InitializingBean {
    private final MutableServerStartupConfiguration configuration;

    private final Hashtable initialEnv;

    private final File workingDir = new File(System
            .getProperty("java.io.tmpdir")
            + File.separator + "ldaptemplate_apacheds");

    public ConfigEnvHelper(Hashtable initialEnv,
            MutableServerStartupConfiguration configuration) {

        this.initialEnv = initialEnv;
        this.configuration = configuration;
    }

    public Hashtable getEnv() {
        return initialEnv;
    }

    public void afterPropertiesSet() throws Exception {
        initialEnv.put(Context.INITIAL_CONTEXT_FACTORY, ServerContextFactory.class.getName());
        configuration.setWorkingDirectory(workingDir);
        initialEnv.putAll(configuration.toJndiEnvironment());
    }
}
