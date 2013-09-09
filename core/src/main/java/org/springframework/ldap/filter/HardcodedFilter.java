/*
 * Copyright 2005-2013 the original author or authors.
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

import org.springframework.util.StringUtils;

/**
 * Allows hard coded parts to be included in a search filter. Particularly useful
 * if some filters are specified in configuration files and these should be
 * combined with other ones.
 * 
 * <pre>
 * Filter filter = new HardcodedFilter(&quot;(&amp;(objectClass=user)(!(objectClass=computer)))&quot;);
 * System.out.println(filter.toString());
 * </pre>
 * 
 * would result in:
 * <code>(&amp;(objectClass=user)(!(objectClass=computer)))</code>
 * <p>
 * <b>Note 1</b>: If the definition is in XML you will need to properly encode any special characters so that they are valid in an XML file,
 * e.g. &quot;&amp;&quot; needs to be encoded as &quot;&amp;amp;&quot;, e.g.
 * <pre>
 * &lt;bean class="MyClass"&gt;
 *   &lt;property name="filter" value="(&amp;amp;(objectClass=user)(!(objectClass=computer)))" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * </p>
 * <p>
 * <b>Note 2</b>: There will be no validation to ensure that the supplied filter is
 * valid. Using this implementation to build filters from user input is strongly
 * discouraged.
 * </p>
 * @author Justen Stepka
 * @author Mathieu Larchet
 */
public class HardcodedFilter extends AbstractFilter {

	private String filter;

	/**
	 * The hardcoded string to be used for this filter.
	 * @param filter the hardcoded filter string.
	 */
	public HardcodedFilter(String filter) {
		this.filter = filter;
	}

	public StringBuffer encode(StringBuffer buff) {
		if (!StringUtils.hasLength(filter)) {
			return buff;
		}

		buff.append(filter);
		return buff;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HardcodedFilter that = (HardcodedFilter) o;

        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return filter != null ? filter.hashCode() : 0;
    }
}
