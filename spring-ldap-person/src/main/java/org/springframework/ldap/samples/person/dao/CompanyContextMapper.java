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

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.samples.person.domain.Company;

/**
 * Maps from DirContextOperations (DirContextAdapters, really) to Company
 * objects. A DN for a company will be of the form <code>ou=[name],c=country</code>
 * 
 * @author Ulrik Sandberg
 */
public class CompanyContextMapper implements ContextMapper {

    /*
     * @see org.springframework.ldap.core.ContextMapper#mapFromContext(java.lang.Object)
     */
    public Object mapFromContext(Object ctx) {
        DirContextOperations dirContext = (DirContextOperations) ctx;
        Company company = new Company();
        company.setName(dirContext.getStringAttribute("ou"));
        return company;
    }
}
