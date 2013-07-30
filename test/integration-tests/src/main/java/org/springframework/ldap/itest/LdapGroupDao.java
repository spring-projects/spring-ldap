package org.springframework.ldap.itest;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.BaseLdapPathAware;

import javax.naming.Name;

/**
 *
 * @author Mattias Hellborg Arthursson
 */
public class LdapGroupDao implements BaseLdapPathAware
{
    private Name basePath;

    public LdapGroupDao() {
        super();
    }

    public void setBaseLdapPath(DistinguishedName baseLdapPath) {
        this.basePath = baseLdapPath;
    }

    public Name getBasePath() {
        return basePath;
    }
}
