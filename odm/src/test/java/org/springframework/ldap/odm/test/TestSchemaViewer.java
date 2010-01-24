package org.springframework.ldap.odm.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.test.utils.ExecuteRunnable;
import org.springframework.ldap.odm.test.utils.GetFreePort;
import org.springframework.ldap.odm.test.utils.RunnableTest;
import org.springframework.ldap.odm.tools.SchemaViewer;
import org.springframework.ldap.test.LdapTestUtils;

public final class TestSchemaViewer {
    // Base DN for test data
    private static final DistinguishedName baseName = new DistinguishedName("o=Whoniverse");

    private static final String PRINCIPAL="";
    private static final String CREDENTIALS="";
    private static final String lineSeparator = System.getProperty ("line.separator");
    
    private static int port;
    
    private static String[] commonFlags;
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Added because the close down of Apache DS on Linux does
        // not seem to free up its port.
        port=GetFreePort.getFreePort();
        
        commonFlags=new String[] { 
                "--url", "ldap://127.0.0.1:"+port,
                "--username", "",
                "--password", "",
                "--error"};
        
        // Start an in process LDAP server
        LdapTestUtils.startApacheDirectoryServer(port, baseName.toString(), "odm-test", "", "", null);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LdapTestUtils.destroyApacheDirectoryServer(PRINCIPAL, CREDENTIALS);
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private static String runSchemaViewer(String... flags) {
        String result=null;
        PrintStream originalOut=System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {   
            System.setErr(new PrintStream(output));

            List<String> commandFlags=
                new ArrayList<String>(Arrays.asList(commonFlags));
            commandFlags.addAll(Arrays.asList(flags));
            
            SchemaViewer.main(commandFlags.toArray(new String[0]));
            
            // Turn end of lines into | for portability
            result=output.toString().trim().replace(lineSeparator, "|");
            
        } finally {
            System.setErr(originalOut);
        }
        
        return result;
    }
    
    private static class TestData {
        private final String flag;
        private final String value;
        private final String result;
        
        public TestData(String flag, String value, String result) {
            this.flag=flag;
            this.value=value;
            this.result=result;
        }
    }
    
    // This makes the test dependent on the order in which the data is returned - it is invalid to assume that this will not change
    private static TestData[] viewerTestData=new TestData[] {
        new TestData("-o", "top", 
                "NAME:top|MUST:objectClass |NAME:top |NUMERICOID:2.5.6.0 |DESC:top of the superclass chain |ABSTRACT:true"),
        new TestData("-o", "country", 
                "NAME:country|MUST:c |SUP:top |NAME:country |STRUCTURAL:true |NUMERICOID:2.5.6.2 |DESC:RFC2256: a country |MAY:searchGuide description"),
        new TestData("-a", "sn", 
                "NAME:sn|SUP:name |SYNTAX:1.3.6.1.4.1.1466.115.121.1.15 |NAME:sn surname |EQUALITY:caseIgnoreMatch |SUBSTR:caseIgnoreSubstringsMatch |USAGE:userApplications |NUMERICOID:2.5.4.4 |DESC:RFC2256: last (family) name(s) for which the entity is known by"),
        new TestData("-a", "jpegPhoto", 
                "NAME:jpegPhoto|SYNTAX:1.3.6.1.4.1.1466.115.121.1.28 |NAME:jpegPhoto |USAGE:userApplications |NUMERICOID:0.9.2342.19200300.100.1.60 |DESC:RFC2798: a JPEG image"),
        new TestData("-s", "jpeg",
                "NAME:jpeg|NAME:JPEG |NUMERICOID:1.3.6.1.4.1.1466.115.121.1.28"),
        new TestData("-s", "OID",
                "NAME:OID|NAME:OID |NUMERICOID:1.3.6.1.4.1.1466.115.121.1.38"),
    };
    
    // Very simple test - mainly just to exercise the code and to
    // ensure we get representative test coverage
    @Test
    public void testSchemaViewer() throws Exception {
        new ExecuteRunnable<TestData>().runTests(new RunnableTest<TestData>() {
            public void runTest(TestData testData) {
                String result=runSchemaViewer(testData.flag, testData.value);             
                assertEquals(testData.result, result);
            }
        }, viewerTestData);
    }
}
