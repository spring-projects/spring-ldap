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

package org.springframework.ldap.odm.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A map from an LDAP syntax to the Java class used to represent it.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
/* package */ final class SyntaxToJavaClass {

	public static final class ClassInfo {

		private final String className;

		private final String packageName;

		private ClassInfo(String className, String packageName) {
			this.className = className;
			this.packageName = packageName;
		}

		public String getClassName() {
			return className;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getFullClassName() {
			StringBuilder result = new StringBuilder();
			if (packageName != null) {
				result.append(packageName).append(".").append(className);
			}
			else {
				result.append(className);
			}
			return result.toString();
		}

	}

	private final Map<String, ClassInfo> mapSyntaxToClassInfo = new HashMap<String, ClassInfo>();

	public SyntaxToJavaClass(Map<String, String> mapSyntaxToClass) {
		for (Entry<String, String> syntaxAndClass : mapSyntaxToClass.entrySet()) {
			String fullClassName = syntaxAndClass.getValue().trim();
			String packageName = null;
			String className = null;
			int lastDotIndex = fullClassName.lastIndexOf('.');
			if (lastDotIndex != -1) {
				className = fullClassName.substring(lastDotIndex + 1);
				packageName = fullClassName.substring(0, lastDotIndex);
			}
			else {
				className = fullClassName;
			}
			mapSyntaxToClassInfo.put(syntaxAndClass.getKey(), new ClassInfo(className, packageName));
		}
	}

	public ClassInfo getClassInfo(String syntax) {
		return mapSyntaxToClassInfo.get(syntax);
	}

}
