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

package org.springframework.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;


/**
 * Dummy implementation of AttributesMapper for use in integration tests.
 * 
 * @author Mattias Arthursson
 * 
 */
public class PersonAttributesMapper implements AttributesMapper {

    /**
     * Maps the given attributes into a {@link Person} object.
     * 
     * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes)
            throws NamingException {
        Person person = new Person();
        person.setFullname((String) attributes.get("cn").get());
        person.setLastname((String) attributes.get("sn").get());
        person.setPhone((String) attributes.get("telephoneNumber").get());
        person.setDescription((String) attributes.get("description").get());
        return person;
    }
}
