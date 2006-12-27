package org.springframework.ldap.support.transaction;

import javax.naming.directory.DirContext;

public interface DirContextProxy extends DirContext {
    DirContext getTargetContext();
}
