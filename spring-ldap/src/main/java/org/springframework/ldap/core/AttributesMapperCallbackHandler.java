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

package org.springframework.ldap.core;

import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.support.LdapUtils;

/**
 * A CollectingNameClassPairCallbackHandler to wrap an AttributesMapper.
 * That is, the found object is extracted from the {@link Attributes} of
 * each {@link SearchResult}, and then passed to the specified
 * AttributesMapper for translation.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class AttributesMapperCallbackHandler extends
        CollectingNameClassPairCallbackHandler {
    private AttributesMapper mapper;

    public AttributesMapperCallbackHandler(AttributesMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Cast the NameClassPair to a SearchResult and pass its attributes to
     * the AttributesMapper.
     * 
     * @param nameClassPair
     *            a SearchResult instance.
     * @return the Object returned from the Mapper.
     */
    public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
        SearchResult searchResult = (SearchResult) nameClassPair;
        Attributes attributes = searchResult.getAttributes();
        try {
            return mapper.mapFromAttributes(attributes);
        } catch (javax.naming.NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
    }
}