package org.springframework.ldap.odm.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Simple utility class to run a given test over a set of test data
public final class ExecuteRunnable<U> {   

    public void runTests(RunnableTest<U>  runnableTest, U[] testData) throws Exception {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
        Log LOG = LogFactory.getLog(ste.getClassName());
        for (U testDatum : testData) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Running test with data %1$s", testDatum));
            }
            runnableTest.runTest(testDatum);
        }
    }
}
