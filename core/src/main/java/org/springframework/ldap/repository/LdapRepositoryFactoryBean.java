package org.springframework.ldap.repository;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.util.Assert;

import javax.naming.Name;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdapRepositoryFactoryBean<T extends Repository<S, Name>, S> extends RepositoryFactoryBeanSupport<T, S, Name> {
    private LdapOperations ldapOperations;

    public void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new LdapRepositoryFactory(ldapOperations);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(ldapOperations, "LdapOperations must be set");
    }
}
