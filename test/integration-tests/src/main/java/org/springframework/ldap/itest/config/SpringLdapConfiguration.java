package org.springframework.ldap.itest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.repository.config.EnableLdapRepositories;

/**
 * @author Mattias Hellborg Arthursson
 */
@Configuration
@EnableLdapRepositories(basePackages = "org.springframework.ldap.itest.repositories")
public class SpringLdapConfiguration {
}
