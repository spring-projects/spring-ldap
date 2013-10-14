package org.springframework.ldap.itest.repositories;

import org.springframework.ldap.itest.odm.PersonWithDnAnnotations;
import org.springframework.ldap.repository.LdapRepository;

/**
 * @author Mattias Hellborg Arthursson
 */
public interface PersonWithDnAnnotationsRepository extends LdapRepository<PersonWithDnAnnotations>  {
}
