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

import javax.naming.Binding;
import javax.naming.NameClassPair;

import org.apache.commons.lang.Validate;

/**
 * A CollectingNameClassPairCallbackHandler to wrap a ContextMapper. That is,
 * the found object is extracted from each {@link Binding}, and then passed to
 * the specified ContextMapper for translation.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 * @since 1.2
 */
public class ContextMapperCallbackHandler extends
        CollectingNameClassPairCallbackHandler {
    private ContextMapper mapper;

    /**
     * Constructs a new instance wrapping the supplied {@link ContextMapper}.
     * 
     * @param mapper
     *            the mapper to be called for each entry.
     */
    public ContextMapperCallbackHandler(ContextMapper mapper) {
        Validate.notNull(mapper, "Mapper must not be empty");
        this.mapper = mapper;
    }

    /**
     * Cast the NameClassPair to a {@link Binding} and pass its attributes to
     * the ContextMapper.
     * 
     * @param nameClassPair
     *            a SearchResult instance.
     * @return the Object returned from the Mapper.
     */
    public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
        Binding binding = (Binding) nameClassPair;
        Object object = binding.getObject();
        if (object == null) {
            throw new ObjectRetrievalException(
                    "SearchResult did not contain any object.");
        }
        return mapper.mapFromContext(object);
    }
}