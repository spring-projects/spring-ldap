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

package org.springframework.ldap.test.apacheds2;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;

/**
 * @author Mattias Hellborg Arthursson
 */
public class TestContextSourceFactoryBean extends AbstractFactoryBean {
    private int port;

	private String defaultPartitionSuffix;

	private String defaultPartitionName;

	private String principal;

	private String password;

	private boolean baseOnTarget = true;

	private Resource ldifFile;

	private Class dirObjectFactory = DefaultDirObjectFactory.class;

	private boolean pooled = true;

	private AuthenticationSource authenticationSource;

    private ContextSource contextSource;

    public void setAuthenticationSource(AuthenticationSource authenticationSource) {
		this.authenticationSource = authenticationSource;
	}

	public void setPooled(boolean pooled) {
		this.pooled = pooled;
	}

	public void setDirObjectFactory(Class dirObjectFactory) {
		this.dirObjectFactory = dirObjectFactory;
	}

	public void setLdifFile(Resource ldifFile) {
		this.ldifFile = ldifFile;
	}

	public void setBaseOnTarget(boolean baseOnTarget) {
		this.baseOnTarget = baseOnTarget;
	}

	public void setDefaultPartitionSuffix(String defaultPartitionSuffix) {
		this.defaultPartitionSuffix = defaultPartitionSuffix;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDefaultPartitionName(String defaultPartitionName) {
		this.defaultPartitionName = defaultPartitionName;
	}

	public void setPort(int port) {
		this.port = port;
	}

    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    protected Object createInstance() throws Exception {
        LdapTestUtils.startEmbeddedServer(port, defaultPartitionSuffix, defaultPartitionName);

        if (contextSource == null) {
            // If not explicitly configured, create a new instance.
            LdapContextSource targetContextSource = new LdapContextSource();
            if (baseOnTarget) {
                targetContextSource.setBase(defaultPartitionSuffix);
            }

            targetContextSource.setUrl("ldap://localhost:" + port);
            targetContextSource.setUserDn(principal);
            targetContextSource.setPassword(password);
            targetContextSource.setDirObjectFactory(dirObjectFactory);
            targetContextSource.setPooled(pooled);

            if (authenticationSource != null) {
                targetContextSource.setAuthenticationSource(authenticationSource);
            }
            targetContextSource.afterPropertiesSet();

            contextSource = targetContextSource;
        }

        Thread.sleep(1000);

        if (baseOnTarget) {
			LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
		}
		else {
			LdapTestUtils.clearSubContexts(contextSource, LdapUtils.newLdapName(defaultPartitionSuffix));
		}

		if (ldifFile != null) {
            LdapTestUtils.loadLdif(contextSource, ldifFile);
		}

		return contextSource;
	}

	public Class getObjectType() {
		return ContextSource.class;
	}

	protected void destroyInstance(Object instance) throws Exception {
		super.destroyInstance(instance);
        LdapTestUtils.shutdownEmbeddedServer();
	}
}
