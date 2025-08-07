/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.odm.core.impl;

import java.util.Locale;

import org.springframework.util.Assert;

// A case independent String wrapper.
/* package */ final class CaseIgnoreString implements Comparable<CaseIgnoreString> {

	private final String string;

	private final int hashCode;

	CaseIgnoreString(String string) {
		Assert.notNull(string, "string must not be null");
		this.string = string;
		this.hashCode = string.toUpperCase(Locale.ROOT).hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof CaseIgnoreString && ((CaseIgnoreString) other).string.equalsIgnoreCase(this.string);
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public int compareTo(CaseIgnoreString other) {
		CaseIgnoreString cis = other;
		return String.CASE_INSENSITIVE_ORDER.compare(this.string, cis.string);
	}

	@Override
	public String toString() {
		return this.string;
	}

}
