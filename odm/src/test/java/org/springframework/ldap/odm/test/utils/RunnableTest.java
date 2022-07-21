package org.springframework.ldap.odm.test.utils;

// Interface to implement for tests to be run by ExecuteRunnable
public interface RunnableTest<T> {
	void runTest(T testData) throws Exception;
}
