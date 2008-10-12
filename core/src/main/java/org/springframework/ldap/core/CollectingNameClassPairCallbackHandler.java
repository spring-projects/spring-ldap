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

import java.util.LinkedList;
import java.util.List;

import javax.naming.NameClassPair;

/**
 * A NameClassPairCallbackHandler to collect all results in an internal List.
 * 
 * @see LdapTemplate
 * 
 * @author Mattias Arthursson
 */
public abstract class CollectingNameClassPairCallbackHandler implements
        NameClassPairCallbackHandler {

    private List list = new LinkedList();

    /**
     * Get the assembled list.
     * 
     * @return the list of all assembled objects.
     */
    public List getList() {
        return list;
    }

    /**
     * Pass on the supplied NameClassPair to
     * {@link #getObjectFromNameClassPair(NameClassPair)} and add the result to
     * the internal list.
     */
    public void handleNameClassPair(NameClassPair nameClassPair) {
        list.add(getObjectFromNameClassPair(nameClassPair));
    }

    /**
     * Handle a NameClassPair and transform it to an Object of the desired type
     * and with data from the NameClassPair.
     * 
     * @param nameClassPair
     *            a NameClassPair from a search operation.
     * @return an object constructed from the data in the NameClassPair.
     */
    public abstract Object getObjectFromNameClassPair(
            NameClassPair nameClassPair);
}
