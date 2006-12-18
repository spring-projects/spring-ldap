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

import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.support.DirContextAdapter;

/**
 * Dummy implemention of ContextMapper for use in the integration tests and for
 * illustration purposes.
 * 
 * @author Mattias Arthursson
 */
public class PersonContextMapper implements ContextMapper {

    /* (non-Javadoc)
     * @see org.springframework.ldap.ContextMapper#mapFromContext(java.lang.Object)
     */
    public Object mapFromContext(Object ctx) {
        DirContextAdapter adapter = (DirContextAdapter) ctx;
        Person person = new Person();
        person.setFullname(adapter.getStringAttribute("cn"));
        person.setLastname(adapter.getStringAttribute("sn"));
        person.setPhone(adapter.getStringAttribute("telephoneNumber"));
        person.setDescription(adapter.getStringAttribute("description"));
        return person;
    }
}
