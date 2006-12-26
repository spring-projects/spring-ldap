package org.springframework.ldap.support.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NullRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(NullRollbackOperation.class);

    public void rollback() {
        log.info("Rolling back null operation");
    }

}
