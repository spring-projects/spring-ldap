/*
 * Copyright 2005-2018 the original author or authors.
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

import java.util.List;

/**
 * A filter for 'in'. The following code:
 * <p>
 * <pre>
 * InFilter filter = new InFilter(&quot;cn&quot;, Arrays.asList(&quot;Some CN 1&quot;, &quot;Some CN 2&quot;, &quot;Some CN 3&quot;));
 * System.out.println(filter.encode());
 * </pre>
 * <p>
 * would result in:
 * <p>
 * <pre>
 * (|(cn=Some CN 1)(cn=Some CN 2)(cn=Some CN 3))
 * </pre>
 *
 * @author Vincent Law
 */
public class InFilter extends AbstractFilter {

    private OrFilter orFilter = new OrFilter();

    public InFilter(String attribute, List<String> values) {
        if(values != null){
            for (String value : values) {
                orFilter.or(new EqualsFilter(attribute, value));
            }
        }
    }

    @Override
    public StringBuffer encode(StringBuffer buf) {
        return orFilter.encode(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InFilter inFilter = (InFilter) o;

        return (orFilter != null ? orFilter.equals(inFilter.orFilter) : inFilter.orFilter != null);
    }

    @Override
    public int hashCode() {
        return orFilter != null ? orFilter.hashCode() : 0;
    }

}
