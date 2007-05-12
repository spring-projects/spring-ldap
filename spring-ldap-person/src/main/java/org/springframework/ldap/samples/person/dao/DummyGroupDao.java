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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.samples.person.domain.Group;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

public class DummyGroupDao implements GroupDao {
    private static Map GROUPS;

    static {
        Group group1 = new Group();
        group1.setName("DUMMY_ADMIN");
        Group group2 = new Group();
        group2.setName("DUMMY_USER");
        GROUPS = new HashMap();
        GROUPS.put("DUMMY_ADMIN", group1);
        GROUPS.put("DUMMY_USER", group2);
    }

    Log log = LogFactory.getLog(DummyGroupDao.class);

    public void create(Group group) {
        log.info("create");
    }

    public void delete(Group group) {
        log.info("delete");
    }

    public List find(SearchCriteria criteria) {
        log.info("find");
        if (GROUPS.get(criteria.getName()) != null) {
            return Collections.singletonList(GROUPS.get(criteria.getName()));
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public List findAll() {
        log.info("findAll");
        return new LinkedList(GROUPS.entrySet());
    }

    public Group findByPrimaryKey(String name) {
        log.info("findByPrimaryKey");
        if (GROUPS.get(name) != null) {
            return (Group) GROUPS.get(name);
        } else {
            throw new NameNotFoundException("Could not find group with name '"
                    + name + "'");
        }
    }

    public void update(Group group) {
        log.info("update");
        Group actualGroup = findByPrimaryKey(group.getName());
        actualGroup.setMembers(group.getMembers());
    }

    public void updateMemberDn(String originalDn, String newDn) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
