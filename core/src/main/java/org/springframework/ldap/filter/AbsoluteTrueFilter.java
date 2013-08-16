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

/**
 * A filter that will always evaluate to <code>true</code>, as specified in RFC4526.
 *
 * @author Mattias Hellborg Arthursson
 * @see <a href="http://tools.ietf.org/html/rfc4526">RFC4526</a>
 * @since 1.3.2
 */
public class AbsoluteTrueFilter extends AbstractFilter {
    public StringBuffer encode(StringBuffer buff) {
        buff.append("(&)");
        return buff;
    }
}
