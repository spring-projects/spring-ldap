package org.springframework.ldap.odm.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.sample.SearchForPeople;
import org.springframework.ldap.test.LdapTestUtils;

public class TestSearchForPeople {
    // Base DN for test data
    private static final DistinguishedName baseName = new DistinguishedName("o=Whoniverse");

    private static final String PRINCIPAL="uid=admin,ou=system";
    private static final String CREDENTIALS="secret";
     
    // This port MUST be free on local host for these unit tests to function.
    private static int PORT=10389;
  
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Start an LDAP server and import test data
        LdapTestUtils.startApacheDirectoryServer(PORT, baseName.toString(), "odm-test", PRINCIPAL, CREDENTIALS, null);
    }
   
    @AfterClass
    public static void tearDownClass() throws Exception {
        LdapTestUtils.destroyApacheDirectoryServer(PRINCIPAL, CREDENTIALS);
    }
    
    @Before
    public void setUp() throws Exception {
        // Bind to the directory
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://127.0.0.1:" + PORT);
        contextSource.setUserDn("");
        contextSource.setPassword("");
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();

        // Create the Sprint LDAP template
        LdapTemplate template = new LdapTemplate(contextSource);

        // Clear out any old data - and load the test data
        LdapTestUtils.cleanAndSetup(template.getContextSource(), baseName, new ClassPathResource("testdata.ldif"));
    }
    
    @After
    public void tearDown() {
    }
    
    // Very simple test - mainly just to exercise the code and to
    // ensure we get representative test coverage
    @Test
    public void runSample() throws Exception {
        PrintStream originalOut=System.out;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));
            
            SearchForPeople.main(null);

            assertEquals("dn=cn=Bramble Harvey,ou=Doctors,o=Whoniverse | objectClass=[person, top] | cn=[Bramble Harvey] | sn=[Harvey] | description=[Really not a Doctor] | userPassword=[] | telephoneNumber=[22] | seeAlso=[]",
                    output.toString().trim());
        } finally {
            System.setOut(originalOut);
        }
    }
}
