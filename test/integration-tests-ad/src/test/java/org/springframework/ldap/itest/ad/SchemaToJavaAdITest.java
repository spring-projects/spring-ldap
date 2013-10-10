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

package org.springframework.ldap.itest.ad;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.core.impl.OdmManagerImpl;
import org.springframework.ldap.odm.tools.SchemaToJava;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.odm.typeconversion.impl.converters.FromStringConverter;
import org.springframework.ldap.odm.typeconversion.impl.converters.ToStringConverter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

// Tests the generation of entry Java classes from LDAP schema
public final class SchemaToJavaAdITest {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaToJavaAdITest.class);

    private static final DistinguishedName baseName = new DistinguishedName("dc=261consulting,dc=local");

    private static final String tempDir=System.getProperty("java.io.tmpdir");
    private static final String USER_DN = "CN=ldaptest,CN=Users,DC=261consulting,DC=local";
    private static final String PASSWORD = "Buc8xe6AZiewoh7";

    // These unit tests require this port to free on localhost
    private static int port = 13636;

    private ConverterManagerImpl converterManager;

    private LdapContextSource contextSource;
    private LdapTemplate ldapTemplate;

    @Before
    public void setUp() throws Exception {
        // Create some basic converters and a converter manager
        converterManager = new ConverterManagerImpl();

        Converter ptc = new FromStringConverter();
        converterManager.addConverter(String.class, "", Byte.class, ptc);
        converterManager.addConverter(String.class, "", Short.class, ptc);
        converterManager.addConverter(String.class, "", Integer.class, ptc);
        converterManager.addConverter(String.class, "", Long.class, ptc);
        converterManager.addConverter(String.class, "", Double.class, ptc);
        converterManager.addConverter(String.class, "", Float.class, ptc);
        converterManager.addConverter(String.class, "", Boolean.class, ptc);

        Converter tsc = new ToStringConverter();
        converterManager.addConverter(Byte.class, "", String.class, tsc);
        converterManager.addConverter(Short.class, "", String.class, tsc);
        converterManager.addConverter(Integer.class, "", String.class, tsc);
        converterManager.addConverter(Long.class, "", String.class, tsc);
        converterManager.addConverter(Double.class, "", String.class, tsc);
        converterManager.addConverter(Float.class, "", String.class, tsc);
        converterManager.addConverter(Boolean.class, "", String.class, tsc);

        // Bind to the directory
        contextSource = new LdapContextSource();
        contextSource.setUrl("ldaps://127.0.0.1:" + port);
        contextSource.setUserDn(USER_DN);
        contextSource.setPassword(PASSWORD);
        contextSource.setPooled(false);
        contextSource.setBase("dc=261consulting,dc=local");
        HashMap<String, Object> baseEnvironment = new HashMap<String, Object>() {{
            put("java.naming.ldap.attributes.binary", "thumbnailLogo replPropertyMetaData partialAttributeSet registeredAddress userPassword telexNumber partialAttributeDeletionList mS-DS-ConsistencyGuid attributeCertificateAttribute thumbnailPhoto teletexTerminalIdentifier replUpToDateVector dSASignature objectGUID");
        }};
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        contextSource.afterPropertiesSet();

        ldapTemplate = new LdapTemplate(contextSource);

        cleanup();

        DirContextAdapter ctx = new DirContextAdapter("cn=William Hartnell,cn=Users");
        ctx.setAttributeValues("objectclass", new String[]{"person","inetorgperson","organizationalperson","top"});
        ctx.setAttributeValue("cn", "William Hartnell");
        ctx.addAttributeValue("description", "First Doctor");
        ctx.addAttributeValue("description", "Grumpy");
        ctx.addAttributeValue("sn", "Hartnell");
        ctx.addAttributeValue("telephonenumber", "1");

        ldapTemplate.bind(ctx);
    }

    @After
    public void cleanup() {
        ldapTemplate.unbind("cn=William Hartnell,cn=Users");
    }

    // Figure out the path of the created Java file
    private static String calculateOutputDirectory(String outputDir, String packageName) {
        // Convert the package name to a path
        Pattern pattern=Pattern.compile("\\.");
        Matcher matcher=pattern.matcher(packageName);
        String sepToUse=File.separator;
        if (sepToUse.equals("\\")) {
            sepToUse="\\\\";
        }

        return outputDir+File.separator+matcher.replaceAll(sepToUse);
    }

    // Due of the nature of the code under test this unit test is a little unusual:
    //
    // 1) Generate an entry class corresponding to objects classes
    //    "inetorgperson, organizationalperson, person, top"
    //    using the SchemaToJavaTool
    // 2) Compile the generated code
    // 3) Create an OdmManager to managing the newly created
    //    entry class.
    // 4) Use this OdmManager to read an entry from LDAP and check the results.
    //
    @Test
    public void verifySchemaToJavaOnAd() throws Exception {
        final String className="Person";
        final String packageName="org.springframework.ldap.odm.testclasses";

        File tempFile = File.createTempFile("test-odm-syntax-to-class-map", ".txt");
        FileUtils.copyInputStreamToFile(new ClassPathResource("/syntax-to-class-map.txt").getInputStream(), tempFile);

        // Add classes dir to class path - needed for compilation
        System.setProperty("java.class.path",
                System.getProperty("java.class.path")+File.pathSeparator+"target/classes");

        String[] flags=new String[] {
            "--url", "ldaps://127.0.0.1:" + port,
            "--objectclasses", "organizationalperson",
            "--syntaxmap", tempFile.getAbsolutePath(),
            "--class", className,
            "--package", packageName,
            "--outputdir", tempDir,
            "--username", USER_DN,
            "--password", PASSWORD};

        // Generate the code using SchemaToJava
        SchemaToJava.main(flags);

        tempFile.delete();

        // Java 5 - we'll use the Java 6 Compiler API once we can drop support for Java 5.
        String javaDir = calculateOutputDirectory(tempDir, packageName);

        CompilerInterface.compile(javaDir, className+".java");
        // Java 5

        // OK it compiles so lets load our new class
        URL[] urls = new URL[] { new File(tempDir).toURI().toURL() };
        URLClassLoader ucl = new URLClassLoader(urls, getClass().getClassLoader());
        Class<?> clazz = ucl.loadClass(packageName+"."+className);

        // Create our OdmManager using our new class
        OdmManagerImpl odmManager = new OdmManagerImpl(converterManager, contextSource);
        odmManager.addManagedClass(clazz);

        // And try reading from the directory using it
        DistinguishedName testDn=new DistinguishedName("cn=William Hartnell,cn=Users");
        Object fromDirectory=odmManager.read(clazz, testDn);

        LOG.debug(String.format("Read - %1$s", fromDirectory));

        // Check some returned values
        Method getDnMethod=clazz.getMethod("getDn");
        Object dn=getDnMethod.invoke(fromDirectory);
        assertEquals(testDn, dn);

        Method getCnIteratorMethod=clazz.getMethod("getCn");
        @SuppressWarnings("unchecked")
        String cn=(String)getCnIteratorMethod.invoke(fromDirectory);
        assertEquals("William Hartnell", cn);

        Method telephoneNumberIteratorMethod=clazz.getMethod("getTelephoneNumber");
        @SuppressWarnings("unchecked")
        String telephoneNumber=(String)telephoneNumberIteratorMethod.invoke(fromDirectory);
        assertEquals("1", telephoneNumber);

        // Reread and check whether equals and hashCode are at least sane
        Object fromDirectory2=odmManager.read(clazz, testDn);
        assertEquals(fromDirectory, fromDirectory2);
        assertEquals(fromDirectory.hashCode(), fromDirectory2.hashCode());
    }
}
