/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.itest.ad;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class CompilerInterface {

	// Compile the given file - when we can drop Java 5 we'll use the Java 6 compiler API
	public static void compile(String directory, String file) throws Exception {

		ProcessBuilder pb = new ProcessBuilder(new String[] {
				"javac", "-cp", "." + File.pathSeparatorChar + "target" + File.separatorChar + "classes"
						+ File.pathSeparatorChar + System.getProperty("java.class.path"),
				directory + File.separatorChar + file });

		pb.redirectErrorStream(true);
		Process proc = pb.start();
		InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);

		char[] buf = new char[1024];
		int count;
		StringBuilder builder = new StringBuilder();
		while ((count = isr.read(buf)) > 0) {
			builder.append(buf, 0, count);
		}

		boolean ok = proc.waitFor() == 0;

		if (!ok) {
			throw new RuntimeException(builder.toString());
		}
	}

	private CompilerInterface() {

	}

}
