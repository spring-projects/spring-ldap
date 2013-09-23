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

package org.springframework.ldap.query;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.BinaryLogicalFilter;
import org.springframework.ldap.filter.OrFilter;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
enum CriteriaContainerType {
    AND {
        @Override
        public BinaryLogicalFilter constructFilter() {
            return new AndFilter();
        }
    }, OR {
        @Override
        public BinaryLogicalFilter constructFilter() {
            return new OrFilter();
        }
    };

    public void validateSameType(CriteriaContainerType oldType) {
        if (oldType != null && oldType != this) {
            throw new IllegalStateException(
                    String.format("Container type has already been specified as %s, cannot change it to %s",
                            oldType.toString(),
                            this.toString()));
        }

    }

    public abstract BinaryLogicalFilter constructFilter();
}
