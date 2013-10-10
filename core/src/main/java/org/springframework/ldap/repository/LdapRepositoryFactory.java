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

package org.springframework.ldap.repository;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.ldap.core.LdapOperations;

import java.io.Serializable;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdapRepositoryFactory extends RepositoryFactorySupport {
    private final LdapOperations ldapOperations;

    public LdapRepositoryFactory(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getTargetRepository(RepositoryMetadata metadata) {
        return new SimpleLdapRepository(
                ldapOperations,
                ldapOperations.getObjectDirectoryMapper(),
                metadata.getDomainType());
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleLdapRepository.class;
    }
}
