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

package org.springframework.ldap.odm.typeconversion.impl;

/**
 * Interface specifying the conversion between two classes
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public interface Converter {
    /**
     * Attempt to convert a given object to a named class.
     * 
     * @param <T> The class to convert to.
     * @param source The object to convert.
     * @param toClass The class to convert to.
     * @return The converted class or null if the conversion was not possible.
     * @throws Exception Any exception may be throw by a Converter on error.
     */
    <T> T convert(Object source, Class<T> toClass) throws Exception;
}
