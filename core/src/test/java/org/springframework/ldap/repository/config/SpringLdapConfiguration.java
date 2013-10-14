package org.springframework.ldap.repository.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author Mattias Hellborg Arthursson
 */
@Configuration
@EnableLdapRepositories(basePackages = "org.springframework.ldap.config")
public class SpringLdapConfiguration {
}
