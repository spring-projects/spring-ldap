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
package org.springframework.ldap.samples.person.dao;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.samples.person.domain.Group;

/**
 * Maps from DirContextOperations (DirContextAdapters, really) to Group objects.
 * A DN for a group will be of the form <code>cn=[name],ou=groups</code>
 * 
 * @author Ulrik Sandberg
 */
public class GroupContextMapper implements ContextMapper {

    /*
     * @see org.springframework.ldap.core.ContextMapper#mapFromContext(java.lang.Object)
     */
    public Object mapFromContext(Object ctx) {
        DirContextOperations dirContext = (DirContextOperations) ctx;
        Group group = new Group();
        group.setName(dirContext.getStringAttribute("cn"));
        String[] membersArray = dirContext.getStringAttributes("uniqueMember");
        if (membersArray != null) {
            List list = Arrays.asList(membersArray);
            group.setMembers(new TreeSet(list));
        }
        return group;
    }
}
