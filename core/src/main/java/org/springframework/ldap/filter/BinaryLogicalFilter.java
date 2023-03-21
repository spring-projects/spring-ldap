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
package org.springframework.ldap.filter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract superclass for binary logical operations, that is &quot;AND&quot; and
 * &quot;OR&quot; operations.
 *
 * @author Mattias Hellborg Arthursson
 */
public abstract class BinaryLogicalFilter extends AbstractFilter {

	private List<Filter> queryList = new LinkedList<Filter>();

	public StringBuffer encode(StringBuffer buff) {
		if (queryList.size() <= 0) {

			// only output query if contains anything
			return buff;

		}
		else if (queryList.size() == 1) {

			// don't add the &
			Filter query = queryList.get(0);
			return query.encode(buff);

		}
		else {
			buff.append("(").append(getLogicalOperator());

			for (Filter query : queryList) {
				query.encode(buff);
			}

			buff.append(")");

			return buff;
		}
	}

	/**
	 * Implement this in subclass to return the logical operator, for example
	 * &quot;&amp;&quot;.
	 * @return the logical operator.
	 */
	protected abstract String getLogicalOperator();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		BinaryLogicalFilter that = (BinaryLogicalFilter) o;

		if (queryList != null ? !queryList.equals(that.queryList) : that.queryList != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return queryList != null ? queryList.hashCode() : 0;
	}

	/**
	 * Add a query to this logical operation.
	 * @param query the query to add.
	 * @return This instance.
	 */
	public final BinaryLogicalFilter append(Filter query) {
		queryList.add(query);
		return this;
	}

	public final BinaryLogicalFilter appendAll(Collection<Filter> subQueries) {
		queryList.addAll(subQueries);
		return this;
	}

}
