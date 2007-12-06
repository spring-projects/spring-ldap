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
package org.springframework.ldap.samples.person.service;

import java.util.List;
import java.util.Set;

import org.springframework.ldap.samples.person.dao.GroupDao;
import org.springframework.ldap.samples.person.domain.Group;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Service implementation for managing the Group entity.
 * 
 * @author Ulrik Sandberg
 */
public class GroupServiceImpl implements GroupService {

    private GroupDao groupDao;

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#create(java.lang.String,
     *      java.util.Set)
     */
    public void create(String name, Set members) {

        Group group = new Group();
        group.setName(name);
        group.setMembers(members);

        groupDao.create(group);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#update(org.springframework.ldap.samples.person.domain.Group)
     */
    public void update(Group group) {
        groupDao.update(group);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#delete(org.springframework.ldap.samples.person.domain.Group)
     */
    public void delete(Group group) {
        groupDao.delete(group);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#findByPrimaryKey(java.lang.String)
     */
    public Group findByPrimaryKey(String name) {
        return groupDao.findByPrimaryKey(name);
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#findAll()
     */
    public List findAll() {
        return groupDao.findAll();
    }

    /*
     * @see org.springframework.ldap.samples.person.service.GroupService#find(org.springframework.ldap.samples.person.domain.SearchCriteria)
     */
    public List find(SearchCriteria criteria) {
        return groupDao.find(criteria);
    }
}
