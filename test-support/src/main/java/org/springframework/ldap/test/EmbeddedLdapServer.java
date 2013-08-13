package org.springframework.ldap.test;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.ldap.LdapServer;

/**
 * @author Mattias Hellborg Arthursson
 */
public class EmbeddedLdapServer {
    private final String principal;
    private final String password;
    private final DirectoryService directoryService;
    private final LdapServer ldapServer;

    private EmbeddedLdapServer(String principal,
                               String password,
                               DirectoryService directoryService,
                               LdapServer ldapServer) {
        this.principal = principal;
        this.password = password;
        this.directoryService = directoryService;
        this.ldapServer = ldapServer;
    }

}
