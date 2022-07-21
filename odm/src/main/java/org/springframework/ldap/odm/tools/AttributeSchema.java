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

package org.springframework.ldap.odm.tools;

import org.springframework.util.StringUtils;

/**
 * Simple value class to hold the schema of an attribute.  
 * <p>
 * It is only public to allow Freemarker access.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public final class AttributeSchema {
 
	private final String name;

	private final String syntax;

	private final boolean isMultiValued;

	private final boolean isPrimitive;

	private final String scalarType;

	private final boolean isBinary;

	private final boolean isArray;

	public AttributeSchema(final String name, final String syntax, final boolean isMultiValued,
			final boolean isPrimitive, final boolean isBinary, final boolean isArray, final String scalarType) {
		this.name = name;
		this.syntax = syntax;
		this.isMultiValued = isMultiValued;
		this.isPrimitive = isPrimitive;
		this.scalarType = scalarType;
		this.isBinary = isBinary;
		this.isArray = isArray;
	}

	public boolean getIsArray() {
		return isArray;
	}
	
	public boolean getIsBinary() {
		return isBinary;
	}

	public boolean getIsPrimitive() {
		return isPrimitive;
	}

	public String getScalarType() {
		return scalarType;
	}

	public String getName() {
		return name;
	}

	public String getJavaName() {
		return StringUtils.replace(name, "-", "");
	}

	public String getSyntax() {
		return syntax;
	}

	public boolean getIsMultiValued() {
		return isMultiValued;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return String.format(
				"{ name=%1$s, syntax=%2$s, isMultiValued=%3$s, isPrimitive=%4$s, isBinary=%5$s, isArray=%6$s, scalarType=%7$s }",
				name, syntax, isMultiValued, isPrimitive, isBinary, isArray, scalarType);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isArray ? 1231 : 1237);
		result = prime * result + (isBinary ? 1231 : 1237);
		result = prime * result + (isMultiValued ? 1231 : 1237);
		result = prime * result + (isPrimitive ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scalarType == null) ? 0 : scalarType.hashCode());
		result = prime * result + ((syntax == null) ? 0 : syntax.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeSchema other = (AttributeSchema) obj;
		if (isArray != other.isArray)
			return false;
		if (isBinary != other.isBinary)
			return false;
		if (isMultiValued != other.isMultiValued)
			return false;
		if (isPrimitive != other.isPrimitive)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (scalarType == null) {
			if (other.scalarType != null)
				return false;
		} else if (!scalarType.equals(other.scalarType))
			return false;
		if (syntax == null) {
			if (other.syntax != null)
				return false;
		} else if (!syntax.equals(other.syntax))
			return false;
		return true;
	}

}
