package org.springframework.ldap.repository.query;

import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;

/**
 * @author Mattias Hellborg Arthursson
 */
public class PartTreeLdapRepositoryQuery extends AbstractLdapRepositoryQuery {
    private final PartTree partTree;
    private final Parameters<?,?> parameters;
    private final ObjectDirectoryMapper objectDirectoryMapper;

    public PartTreeLdapRepositoryQuery(LdapQueryMethod queryMethod, Class<?> clazz, LdapOperations ldapOperations) {
        super(queryMethod, clazz, ldapOperations);
        partTree = new PartTree(queryMethod.getName(), clazz);
        parameters = queryMethod.getParameters();
        objectDirectoryMapper = ldapOperations.getObjectDirectoryMapper();
    }


    @Override
    protected LdapQuery createQuery(Object[] actualParameters) {
        LdapQueryCreator queryCreator =
                new LdapQueryCreator(partTree,
                        this.parameters,
                        getClazz(),
                        objectDirectoryMapper,
                        actualParameters);
        return queryCreator.createQuery();
    }
}
