/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap;

import javax.naming.NameClassPair;
import javax.naming.NamingException;

/**
 * Responsible for mapping NameClassPair objects to beans.
 * 
 * @author Mattias Arthursson
 */
public interface NameClassPairMapper {
    /**
     * Map NameClassPair to an Object. The supplied NameClassPair is one of the
     * results from a search operation (search, list or listBindings). Depending
     * on which search operation is being performed, the NameClassPair might be
     * a SearchResult, Binding or NameClassPair.
     * 
     * @param nameClassPair
     *            NameClassPair from a search operation.
     * @return and Object built from the NameClassPair.
     * @throws NamingException
     *             if one is encountered in the operation.
     */
    public Object mapFromNameClassPair(NameClassPair nameClassPair)
            throws NamingException;
}
