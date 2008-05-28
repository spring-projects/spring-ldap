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

package org.springframework.ldap.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Abstract superclass for binary logical operations, that is &quot;AND&quot;
 * and &quot;OR&quot; operations.
 * 
 * @author Mattias Arthursson
 */
public abstract class BinaryLogicalFilter extends AbstractFilter {

	protected List queryList = new LinkedList();

	/*
	 * @see org.springframework.ldap.filter.AbstractFilter#encode(java.lang.StringBuffer)
	 */
	public StringBuffer encode(StringBuffer buff) {
		if (queryList.size() <= 0) {

			// only output query if contains anything
			return buff;

		}
		else if (queryList.size() == 1) {

			// don't add the &
			Filter query = (Filter) queryList.get(0);
			return query.encode(buff);

		}
		else {
			buff.append("(" + getLogicalOperator());

			for (Iterator i = queryList.iterator(); i.hasNext();) {
				Filter query = (Filter) i.next();
				buff = query.encode(buff);
			}

			buff.append(")");

			return buff;
		}
	}

	/**
	 * Implement this in subclass to return the logical operator, for example
	 * &qout;&amp;&qout;.
	 * 
	 * @return the logical operator.
	 */
	protected abstract String getLogicalOperator();

	/**
	 * Compares each filter in turn.
	 * 
	 * @see org.springframework.ldap.filter.Filter#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof BinaryLogicalFilter && this.getClass() == obj.getClass()) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		else {
			return false;
		}
	}

	/**
	 * Hashes all contained data.
	 * 
	 * @see org.springframework.ldap.filter.Filter#hashCode()
	 */
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
