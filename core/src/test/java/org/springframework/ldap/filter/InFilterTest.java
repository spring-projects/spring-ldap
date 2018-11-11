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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the InFilter class.
 *
 * @author Vincent Law
 */
public class InFilterTest {

    @Test
    public void testNull() {
        InFilter filter = new InFilter("a", null);
        assertThat(filter.encode()).isEqualTo("");
    }

    @Test
    public void testEmptyList() {
        InFilter filter = new InFilter("a", Collections.<String>emptyList());
        assertThat(filter.encode()).isEqualTo("");
    }

    @Test
    public void testListWithNullElement() {
        InFilter filter = new InFilter("a", Arrays.asList(new String[1]));
        assertThat(filter.encode()).isEqualTo("(a=null)");
    }

    @Test
    public void testOne() {
        InFilter filter = new InFilter("a", Arrays.asList("b"));
        assertThat(filter.encode()).isEqualTo("(a=b)");
    }

    @Test
    public void testTwo() {
        InFilter filter = new InFilter("a", Arrays.asList("b", "c"));
        assertThat(filter.encode()).isEqualTo("(|(a=b)(a=c))");
    }

    @Test
    public void testThree() {
        InFilter filter = new InFilter("a", Arrays.asList("b", "c", "d"));
        assertThat(filter.encode()).isEqualTo("(|(a=b)(a=c)(a=d))");
    }
}
