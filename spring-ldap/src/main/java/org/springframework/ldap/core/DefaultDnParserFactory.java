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
package org.springframework.ldap.core;

import java.io.StringReader;

import org.springframework.ldap.core.DnParserImpl;

/**
 * A factory for creating DnParser instances. The actual implementation of
 * DnParser is generated using javacc and should not be constructed directly.
 * 
 * @author Mattias Arthursson
 * 
 */
public class DefaultDnParserFactory {
    /**
     * Create a new DnParser instance.
     * 
     * @param string
     *            the DN String to be parsed.
     * @return a new DnParser instance for parsing the supplied DN string.
     */
    public static DnParser createDnParser(String string) {
        return new DnParserImpl(new StringReader(string));
    }
}
