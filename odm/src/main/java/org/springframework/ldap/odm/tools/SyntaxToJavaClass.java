/*
 * Copyright 2005-2025 the original author or authors.
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

	private final Map<String, ClassInfo> mapSyntaxToClassInfo = new HashMap<>();

	SyntaxToJavaClass(Map<String, String> mapSyntaxToClass) {
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
			this.mapSyntaxToClassInfo.put(syntaxAndClass.getKey(), new ClassInfo(className, packageName));
		}
	}

	ClassInfo getClassInfo(String syntax) {
		return this.mapSyntaxToClassInfo.get(syntax);
	}

	public static final class ClassInfo {

		private final String className;

		private final String packageName;

		private ClassInfo(String className, String packageName) {
			this.className = className;
			this.packageName = packageName;
		}

		public String getClassName() {
			return this.className;
		}

		public String getPackageName() {
			return this.packageName;
		}

		public String getFullClassName() {
			StringBuilder result = new StringBuilder();
			if (this.packageName != null) {
				result.append(this.packageName).append(".").append(this.className);
			}
			else {
				result.append(this.className);
			}
			return result.toString();
		}

	}

}
