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

package org.springframework.ldap.odm.tools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple value class to hold the schema of an object class
 * <p>
 * It is public only to allow Freemarker access.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public final class ObjectSchema {

	private final Set<AttributeSchema> must = new HashSet<>();

	private final Set<AttributeSchema> may = new HashSet<>();

	private final Set<String> objectClass = new HashSet<>();

	public void addMust(AttributeSchema must) {
		// if may attributes contain must attribute, remove from may and add to must
		if (this.may.contains(must)) {
			this.may.remove(must);
		}
		this.must.add(must);
	}

	public void addMay(AttributeSchema may) {
		// only add may if not in must
		if (!this.must.contains(may)) {
			this.may.add(may);
		}
	}

	public void addObjectClass(String objectClass) {
		this.objectClass.add(objectClass);
	}

	public Set<AttributeSchema> getMust() {
		return Collections.unmodifiableSet(this.must);
	}

	public Set<AttributeSchema> getMay() {
		return Collections.unmodifiableSet(this.may);
	}

	public Set<String> getObjectClass() {
		return Collections.unmodifiableSet(this.objectClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ObjectSchema other = (ObjectSchema) obj;
		if (this.may == null) {
			if (other.may != null) {
				return false;
			}
		}
		else if (!this.may.equals(other.may)) {
			return false;
		}
		if (this.must == null) {
			if (other.must != null) {
				return false;
			}
		}
		else if (!this.must.equals(other.must)) {
			return false;
		}
		if (this.objectClass == null) {
			if (other.objectClass != null) {
				return false;
			}
		}
		else if (!this.objectClass.equals(other.objectClass)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.may == null) ? 0 : this.may.hashCode());
		result = prime * result + ((this.must == null) ? 0 : this.must.hashCode());
		result = prime * result + ((this.objectClass == null) ? 0 : this.objectClass.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("objectClass=%1$s | must=%2$s | may=%3$s", this.objectClass, this.must, this.may);
	}

}
