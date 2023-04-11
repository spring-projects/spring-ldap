/*
 * Copyright 2005-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Simple utility class to run a given test over a set of test data
public final class ExecuteRunnable<U> {

	public void runTests(RunnableTests<U> runnableTest, U[] testData) throws Exception {
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
