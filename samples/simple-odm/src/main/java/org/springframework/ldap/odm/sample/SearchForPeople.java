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

package org.springframework.ldap.odm.sample;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.ldap.LdapName;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

// A very simple example - just showing how little code you actually need to write
// when using Spring LDAP ODM
public class SearchForPeople {
    private static final LdapName baseDn = LdapUtils.newLdapName("o=Whoniverse");

    private static void print(List<SimplePerson> personList) {
        for (SimplePerson person : personList) {
            System.out.println(person);
        }
    }
    
    public static void main(String[] argv) {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring.xml" });

        // Grab the OdmManager wired by Spring
        OdmManager odmManager = (OdmManager)context.getBean("odmManager");
        
        // Find people with a surname of Harvey
        List<SimplePerson> searchResults = odmManager.search(SimplePerson.class, query().base(baseDn).where("sn").is("Harvey"));
        
        // Print the results
        print(searchResults);
    }
}
