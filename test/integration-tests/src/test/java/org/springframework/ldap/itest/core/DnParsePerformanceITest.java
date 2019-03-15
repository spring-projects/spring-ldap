/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ldap.itest.core;

import org.junit.Test;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.util.StopWatch;

/**
 * Performance test for the {@link DistinguishedName} class.
 * 
 * @author Ulrik Sandberg
 */
public class DnParsePerformanceITest {

	@Test
	public void testCreateFromString() {
		StopWatch stopWatch = new StopWatch("Create from String");
		stopWatch.start();

		for (int i = 0; i < 2000; i++) {
			DistinguishedName migpath = new DistinguishedName("OU=G,OU=I,OU=M");
			DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,OU=G,OU=I,OU=M");
			DistinguishedName path2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=i,OU=M, ou=foo");
			DistinguishedName path3 = new DistinguishedName("ou=G,OU=i,OU=M, ou=foo");
			DistinguishedName path4 = new DistinguishedName("ou=G,OU=i,ou=m");

			DistinguishedName pathE1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=L,OU=M, ou=foo");
			DistinguishedName pathE2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE");
		}

		stopWatch.stop();
		System.out.println(stopWatch.prettyPrint());
	}

	@Test
	public void testCreateFromDistinguishedName() {
		DistinguishedName migpath = new DistinguishedName("OU=G,OU=I,OU=M");
		DistinguishedName path1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,OU=G,OU=I,OU=M");
		DistinguishedName path2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path3 = new DistinguishedName("ou=G,OU=i,OU=M, ou=foo");
		DistinguishedName path4 = new DistinguishedName("ou=G,OU=i,ou=m");

		DistinguishedName pathE1 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE,ou=G,OU=L,OU=M, ou=foo");
		DistinguishedName pathE2 = new DistinguishedName("cn=john.doe, OU=Users,OU=SE");

		StopWatch stopWatch = new StopWatch("Create from DistinguishedName");
		stopWatch.start();
		
		for (int i = 0; i < 2000; i++) {
			migpath = new DistinguishedName(migpath);
			path1 = new DistinguishedName(path1);
			path2 = new DistinguishedName(path2);
			path3 = new DistinguishedName(path3);
			path4 = new DistinguishedName(path4);

			pathE1 = new DistinguishedName(pathE1);
			pathE2 = new DistinguishedName(pathE2);
		}

		stopWatch.stop();
		System.out.println(stopWatch.prettyPrint());
	}
}