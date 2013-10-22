package org.springframework.ldap.itest.repositories;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.repository.LdapRepository;

/**
 * @author Mattias Hellborg Arthursson
 */
public interface PersonQueryDslRepository extends LdapRepository<Person>, QueryDslPredicateExecutor<Person> {
}
