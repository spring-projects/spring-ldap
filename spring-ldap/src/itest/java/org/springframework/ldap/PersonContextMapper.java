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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

/**
 * Dummy implemention of ContextMapper for use in the integration tests and for
 * illustration purposes.
 * 
 * @author Mattias Arthursson
 */
public class PersonContextMapper extends AbstractContextMapper {

    protected Object doMapFromContext(DirContextOperations ctx) {
        Person person = new Person();
        person.setFullname(ctx.getStringAttribute("cn"));
        person.setLastname(ctx.getStringAttribute("sn"));
        person.setPhone(ctx.getStringAttribute("telephoneNumber"));
        person.setDescription(ctx.getStringAttribute("description"));

        return person;
    }
}
