package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

public interface DirContextProxy {
    DirContext getTargetContext();
}
