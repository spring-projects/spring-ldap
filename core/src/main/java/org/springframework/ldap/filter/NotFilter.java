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

import org.springframework.util.Assert;

/**
 * A filter for 'not'. The following code:
 * 
 * <pre>
 * Filter filter = new NotFilter(new EqualsFilter(&quot;cn&quot;, &quot;foo&quot;);
 * System.out.println(filter.encode());
 * </pre>
 * 
 * would result in:
 * 
 * <pre>
 * (!(cn = foo))
 * </pre>
 * 
 * @author Adam Skogman
 */
public class NotFilter extends AbstractFilter {

	private final Filter filter;

	/**
	 * Create a filter that negates the outcome of the given <code>filter</code>.
	 * 
	 * @param filter The filter that should be negated.
	 */
	public NotFilter(Filter filter) {
		Assert.notNull(filter, "Filter must not be null");
		this.filter = filter;
	}

	public StringBuffer encode(StringBuffer buff) {

		buff.append("(!");
		filter.encode(buff);
		buff.append(')');

		return buff;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotFilter notFilter = (NotFilter) o;

        if (filter != null ? !filter.equals(notFilter.filter) : notFilter.filter != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return filter != null ? filter.hashCode() : 0;
    }
}
