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

/**
 * @author Mattias Hellborg Arthursson
 */
public class EmbeddedLdapServerFactoryBean extends AbstractFactoryBean<EmbeddedLdapServer> {
    private int port;
    private String partitionName;
    private String partitionSuffix;

    @Override
    public Class<?> getObjectType() {
        return EmbeddedLdapServer.class;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public void setPartitionSuffix(String partitionSuffix) {
        this.partitionSuffix = partitionSuffix;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    protected EmbeddedLdapServer createInstance() throws Exception {
	    return EmbeddedLdapServer.newEmbeddedServer(this.partitionName, this.partitionSuffix, this.port);
    }

    @Override
    protected void destroyInstance(EmbeddedLdapServer instance) throws Exception {
        instance.shutdown();
    }
}
