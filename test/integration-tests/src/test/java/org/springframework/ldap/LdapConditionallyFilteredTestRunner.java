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

package org.springframework.ldap;

import org.junit.experimental.categories.Categories;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;
import org.springframework.ldap.itest.NoAdTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapConditionallyFilteredTestRunner extends SpringJUnit4ClassRunner {
    /**
     * Constructs a new {@code SpringJUnit4ClassRunner} and initializes a
     * {@link org.springframework.test.context.TestContextManager} to provide Spring testing functionality to
     * standard JUnit tests.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public LdapConditionallyFilteredTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);

        String noadtest = System.getProperty("adtest");
        if (noadtest != null) {
            try {
                filter(Categories.CategoryFilter.exclude(NoAdTest.class));
            } catch (NoTestsRemainException e) {
                // Nothing to do here.
            }
        }
    }
}
