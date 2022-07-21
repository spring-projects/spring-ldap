package org.springframework.ldap.odm.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Simple utility class to run a given test over a set of test data
public final class ExecuteRunnable<U> {   

	public void runTests(RunnableTest<U>  runnableTest, U[] testData) throws Exception {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Logger LOG = LoggerFactory.getLogger(ste.getClassName());
		for (U testDatum : testData) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("Running test with data %1$s", testDatum));
			}
			runnableTest.runTest(testDatum);
		}
	}
}
