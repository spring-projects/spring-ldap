package org.springframework.ldap.support;

import javax.naming.directory.DirContext;

public interface DirContextProxy {
    DirContext getTargetContext();
}
