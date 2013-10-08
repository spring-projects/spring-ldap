package org.springframework.ldap.test;

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
        return EmbeddedLdapServer.newEmbeddedServer(partitionName, partitionSuffix, port);
    }
}
