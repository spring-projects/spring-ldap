package org.springframework.ldap.odm.sample;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import java.util.List;

// A very simple example - just showing how little code you actually need to write
// when using Spring LDAP ODM
public class SearchForPeople {
    private static final SearchControls searchControls = 
        new SearchControls(SearchControls.SUBTREE_SCOPE, 100, 10000,
            null, true, false);

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
        List<SimplePerson> searchResults = odmManager.search(SimplePerson.class, baseDn, "sn=Harvey", searchControls);
        
        // Print the results
        print(searchResults);
    }
}
