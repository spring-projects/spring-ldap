/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.samples.useradmin.domain.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairMapper;
import org.springframework.ldap.samples.useradmin.domain.DepartmentRepo;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mattias Hellborg Arthursson
 */
public class DepartmentRepoImpl implements DepartmentRepo {

    private static final LdapName DEPARTMENTS_OU = LdapUtils.newLdapName("ou=Departments");
    private final LdapTemplate ldapTemplate;

    @Autowired
    public DepartmentRepoImpl(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public Map<String, List<String>> getDepartmentMap() {
        return new HashMap<String, List<String>>(){{
            List<String> allDepartments = getAllDepartments();
            for (String oneDepartment : allDepartments) {
                put(oneDepartment, getAllUnitsForDepartment(oneDepartment));
            }
        }};
    }

    private List<String> getAllDepartments() {
        return ldapTemplate.list(DEPARTMENTS_OU, new OuValueNameClassPairMapper());
    }

    private List<String> getAllUnitsForDepartment(String department) {
        return ldapTemplate.list(LdapNameBuilder
                .newInstance(DEPARTMENTS_OU).add("ou", department).build(), new OuValueNameClassPairMapper());
    }

    private static class OuValueNameClassPairMapper implements NameClassPairMapper<String> {
        @Override
        public String mapFromNameClassPair(NameClassPair nameClassPair) throws NamingException {
            LdapName name = LdapUtils.newLdapName(nameClassPair.getName());
            return LdapUtils.getStringValue(name, "ou");
        }
    }
}
