/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.core;

import org.junit.Test;
import org.springframework.util.StopWatch;

/**
 * Performance test for the {@link DistinguishedName} class.
 * 
 * @author Ulrik Sandberg
 */
public class DnParsePerformanceITest {

	/**
	 * Tests parsing and toString.
	 */
	@Test
	public void testContains() {
		StopWatch stopWatch = new StopWatch("Dn Parse Performance");
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
}