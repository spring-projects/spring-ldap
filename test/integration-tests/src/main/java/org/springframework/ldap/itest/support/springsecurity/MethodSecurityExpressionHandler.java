package org.springframework.ldap.itest.support.springsecurity;

import org.springframework.ldap.itest.LdapGroupDao;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;

/**
 * @author Mattias Hellborg Arthursson
 */
public class MethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    private LdapGroupDao groupDao;

    public LdapGroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(LdapGroupDao groupDao) {
        this.groupDao = groupDao;
    }

}
