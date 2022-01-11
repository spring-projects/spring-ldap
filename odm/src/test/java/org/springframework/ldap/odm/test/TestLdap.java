/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.test;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import java.util.Base64;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.odm.core.impl.InvalidEntryException;
import org.springframework.ldap.odm.core.impl.MetaDataException;
import org.springframework.ldap.odm.core.impl.OdmManagerImpl;
import org.springframework.ldap.odm.test.utils.ExecuteRunnable;
import org.springframework.ldap.odm.test.utils.GetFreePort;
import org.springframework.ldap.odm.test.utils.RunnableTest;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.odm.typeconversion.impl.converters.FromStringConverter;
import org.springframework.ldap.odm.typeconversion.impl.converters.ToStringConverter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.util.CollectionUtils;

import javax.naming.Name;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.naming.spi.NamingManager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

// Tests all OdmManager functions
public final class TestLdap {
    private static final Logger LOG = LoggerFactory.getLogger(TestLdap.class);

    // Base DN for test data
    private static final LdapName baseName = LdapUtils.newLdapName("o=Whoniverse");

    // This port MUST be free on local host for these unit tests to function.
    private static int port;

    // Maximum number of objects to return in testing
    private static final long COUNT_LIMIT=20;

    // Maximum time to wait for results in testing (ms)
    private static final int TIME_LIMIT=60000;

    private static SearchControls searchControls=
        new SearchControls(SearchControls.SUBTREE_SCOPE,
            COUNT_LIMIT,
            TIME_LIMIT,
            null,
            true,
            false);

    private ConverterManagerImpl converterManager;

    private ContextSource contextSource;

    private OdmManager odmManager;

    // Base 64 encoded jpeg photo used to test binary reading and writing
    private static byte[] photo;
    static {
        try {
            String photoString="/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkS"+
                    "Ew8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRg"+
                    "yIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wA"+
                    "ARCAAnABoDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAA"+
                    "gEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcY"+
                    "GRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipK"+
                    "TlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8v"+
                    "P09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFB"+
                    "AQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygp"+
                    "KjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJm"+
                    "aoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9"+
                    "oADAMBAAIRAxEAPwDx2z0mK6gV/tSo2SCpUHGD9Qa2vDvgGfX7+eI3ot4EOEmEefMOOcZPQdOvW"+
                    "s3VLGzsWiihUvM0ayONxATPIHXk4wfxFdd4L1aw0rw3eS3V3GHWRswkgugwCpVSckEk9OhB9azu"+
                    "2ro6lCKnyyZwmvaHcaBrV1plxKkkluw+dRkMCAQfbgjiqi27sob1GfuVc1q9n1XVL3UdqxJI/wD"+
                    "q/MXIXGAMd+B2HWoUZ1jUbZeABwKqzMJWT0Ld3cfaJ5ZFYtnCg+oUBf6V2XhvwVpcvhxfFXibVF"+
                    "tNK3lEgjJ3yMGK7SRzkkHhRnHORXJQ6exVXB2r1K9Cfx7V0UWpeD7S3WJ9N1TUPLYssV7cBYkZg"+
                    "AxAU4zwOcZ4HoKtppWQ4tSk5SNPx14o8M3/AITXT/D2kRRxB1jE4RIyuOc7ME84PzcHr2PPmX2y"+
                    "YcZX8q37fQxPpqzsH8xvuqAeeuO/0H3e9Zn2dxwbK5JHXCH/AOJqNOhdSM1ZzVjRNwRbjGRxxzn"+
                    "FdRouijV9Pi1C6toEMqn7MsK7SqBiCxOSSSVI5J4x6miiliW1FMzpbszreUw3H2VcuscpGT3UE9"+
                    "Rn0HStE+IrZSQbEuRxvU4De4yc0UVFKCnuerms2vZryP/Z";

            byte[] photoBytes=photoString.getBytes("US-ASCII");
            photo=Base64.getDecoder().decode(photoBytes);
        } catch (IOException e) {
            throw new RuntimeException("Problem decoding photo", e);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Added because the close down of Apache DS on Linux does
        // not seem to free up its port.
        port=GetFreePort.getFreePort();

        // Start an LDAP server and import test data
        LdapTestUtils.startEmbeddedServer(port, baseName.toString(), "odm-test");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LdapTestUtils.shutdownEmbeddedServer();
    }

    private static ContextSource getContextSource(String url, String username, String password) throws Exception {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(username);
        contextSource.setPassword(password);
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();

        return contextSource;
    }

    // Pulled out of setup method to allow it to be called with parameters from main as
    // an integration test
    public void setUp(String url, String username, String password) throws Exception {
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
        contextSource = getContextSource(url, username, password);

        // Clear out any old data - and load the test data
        LdapTestUtils.cleanAndSetup(contextSource, baseName, new ClassPathResource("testdata.ldif"));

        // Create our OdmManager
        Set<Class<?>> managedClasses=new HashSet<Class<?>>();
        managedClasses.add(Person.class);
        managedClasses.add(PlainPerson.class);
        managedClasses.add(OrganizationalUnit.class);
        odmManager = new OdmManagerImpl(converterManager, contextSource, managedClasses);
    }

    @Before
    public void setUp() throws Exception {
        setUp("ldap://127.0.0.1:" + port, "", "");
    }

    @After
    public void tearDown() throws Exception {
        LdapTestUtils.clearSubContexts(contextSource, baseName);

        odmManager=null;
        contextSource=null;
        converterManager=null;
    }

    private enum PersonName {
        WILLIAM(0), PATRICK(1), JON(2), TOM(3), PETER(4), DAVROS(5), DALEKS(6), MASTER(7);

        private int index;

        private PersonName(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private Person[] personTestData=new Person[] {
            new Person(LdapUtils.newLdapName("cn=William Hartnell,ou=Doctors,o=Whoniverse"), "Hartnell", Arrays
                    .asList(new String[] { "First Doctor", "Grumpy" }), 1, null),
            new Person(LdapUtils.newLdapName("cn=Patrick Troughton,ou=Doctors,o=Whoniverse"), "Troughton", Arrays
                    .asList(new String[] { "Second Doctor", "Clown" }), 2, null),
            new Person(LdapUtils.newLdapName("cn=Jon Pertwee,ou=Doctors,o=Whoniverse"), "Pertwee", Arrays
                    .asList(new String[] { "Third Doctor", "Dandy" }), 3, null),
            new Person(LdapUtils.newLdapName("cn=Tom Baker,ou=Doctors,o=Whoniverse"), "Baker", Arrays
                    .asList(new String[] { "Fourth Doctor", "The one and only!" }), 4, null),
            new Person(LdapUtils.newLdapName("cn=Peter Davison,ou=Doctors,o=Whoniverse"), "Davison", Arrays
                    .asList(new String[] { "Fifth Doctor" }), 5, null),
            new Person(LdapUtils.newLdapName("cn=Davros,ou=Enemies,o=Whoniverse"), "Unknown", Arrays
                    .asList(new String[] { "Creator of the Daleks", "Kaled head scientist" }), 0, null),
            new Person(LdapUtils.newLdapName("cn=Daleks,ou=Enemies,o=Whoniverse"), "NA", Arrays
                    .asList(new String[] { "The Doctor's greatest foe" }), 0, null),
            new Person(LdapUtils.newLdapName("cn=Master,ou=Enemies,o=Whoniverse"), "Unknown", Arrays
                    .asList(new String[] { "An evil Time Lord" }), 0, photo), };


    // Read various entries from the sample data set and check they are what we'd expect.
    @Test
    public void read() throws Exception {
        new ExecuteRunnable<Person>().runTests(new RunnableTest<Person>() {
            public void runTest(Person testData) {
                Name dn = testData.getDn();
                LOG.debug(String.format("reading - %1$s", dn));
                Person personEntry = odmManager.read(Person.class, dn);
                LOG.debug(String.format("read - %1$s", personEntry));
                assertEquals(testData, personEntry);
            }
        }, personTestData);
    }

    private static class SearchTestData {
        private String search;

        private SearchControls searchScope;

        private Person[] people;

        public SearchTestData(String search, SearchControls searchScope, Person[] people) {
            this.search = search;
            this.searchScope = searchScope;
            this.people = people;
        }
    }

    private SearchTestData[] searchTestData = {
            new SearchTestData("(sn=Unknown)",
                    searchControls,
                    new Person[] {
                        personTestData[PersonName.DAVROS.getIndex()],
                        personTestData[PersonName.MASTER.getIndex()] }),
            new SearchTestData("(description=*Doctor)",
                    searchControls,
                    new Person[] {
                        personTestData[PersonName.WILLIAM.getIndex()],
                        personTestData[PersonName.PATRICK.getIndex()],
                        personTestData[PersonName.JON.getIndex()],
                        personTestData[PersonName.TOM.getIndex()],
                        personTestData[PersonName.PETER.getIndex()] }),
            };

    // Carry out various searches against the test data set and check the results are what we'd expect.
    @Test
    public void search() throws Exception {
        new ExecuteRunnable<SearchTestData>().runTests(new RunnableTest<SearchTestData>() {
            public void runTest(SearchTestData testData) {
                String search = testData.search;
                LOG.debug(String.format("searching - %1$s", search));
                List<Person> results = odmManager.search(Person.class, baseName, testData.search, testData.searchScope);
                LOG.debug(String.format("found - %1$s", results));
                assertEquals(new HashSet<Person>(Arrays.asList(testData.people)), new HashSet<Person>(results));
            }
        }, searchTestData);
    }

    private enum OrganizationalName {
        ENEMIES(0), ASSISTANTS(1), DOCTORS(2);

        private int index;

        private OrganizationalName(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private static OrganizationalUnit ouTestData[]=new OrganizationalUnit[] {
    	new OrganizationalUnit(LdapUtils.newLdapName("ou=Enemies,o=Whoniverse"), "Acacia Avenue", "The bad guys"),
    	new OrganizationalUnit(LdapUtils.newLdapName("ou=Assistants,o=Whoniverse"), "Somewhere in space", "The plucky helpers"),
    	new OrganizationalUnit(LdapUtils.newLdapName("ou=Doctors,o=Whoniverse"), "Somewhere in time", "Our hero"),
    };

    // Check everything works OK with a second managed class
    @Test
    public void testSecondOc() {
        LOG.debug("Reading all organizatinalUnits");
    	List<OrganizationalUnit> allOus=odmManager.findAll(OrganizationalUnit.class, baseName, searchControls);
    	LOG.debug(String.format("Found - %1$s", allOus));
    	assertEquals(new HashSet<OrganizationalUnit>(Arrays.asList(ouTestData)), new HashSet<OrganizationalUnit>(allOus));

    	OrganizationalUnit testOu=ouTestData[OrganizationalName.ASSISTANTS.getIndex()];
    	LOG.debug(String.format("Reading - %1$s", testOu.getDn()));
    	OrganizationalUnit ou=odmManager.read(OrganizationalUnit.class, testOu.getDn());
    	LOG.debug(String.format("Found - %1$s", ou));
    	assertEquals(testOu, ou);
    }

    // Find all entries managed by the OdmManager in the test data set and check they are what we expect.
    @Test
    public void findAll() throws Exception {
        LOG.debug("finding all people");
        List<Person> allPeople = odmManager.findAll(Person.class, baseName, searchControls);
        LOG.debug(String.format("found %1$s", allPeople));
        assertEquals(new HashSet<Person>(Arrays.asList(personTestData)), new HashSet<Person>(allPeople));
    }

    @Test
    public void findAllAsPlainPersons() {
        List<PlainPerson> allPeople = odmManager.findAll(PlainPerson.class, baseName, searchControls);

        assertEquals(9, allPeople.size());
        assertFalse("No nulls should have been returned", CollectionUtils.containsInstance(allPeople, null));
    }

    @Test
    public void verifySearchOnPlainPerson() {
        List<PlainPerson> result = odmManager.search(PlainPerson.class, baseName, "(cn=William Hartnell)", searchControls);
        assertEquals(1, result.size());

        PlainPerson foundPerson = result.get(0);
        assertEquals("William Hartnell", foundPerson.getCn());
        assertEquals("Hartnell", foundPerson.getSurname());
    }

    @Test
    public void verifySearchWithLdapQuery() {
        List<Person> result = odmManager.search(Person.class, query().base(baseName).where("cn").is("William Hartnell"));
        assertEquals(1, result.size());

        Person foundPerson = result.get(0);
        assertEquals("William Hartnell", foundPerson.getCn());
        assertEquals("Hartnell", foundPerson.getSurname());
    }

    @Test
    public void updatePlainPerson() {
        List<PlainPerson> result = odmManager.search(PlainPerson.class, baseName, "(cn=William Hartnell)", searchControls);
        assertEquals(1, result.size());

        PlainPerson foundPerson = result.get(0);
        foundPerson.setSurname("Tjolahopp");
        odmManager.update(foundPerson);

        // Verify that the objectclass was not changed on the target object
        List<Person> updatedResult = odmManager.search(Person.class, baseName, "(cn=William Hartnell)", searchControls);
        assertEquals(1, updatedResult.size());
    }

    private Person[] createTestData = {
            new Person(LdapUtils.newLdapName("cn=Colin Baker,ou=Doctors,o=Whoniverse"), "Baker", Arrays
                    .asList(new String[] { "Sixth Doctor" }), 6, null),
            new Person(LdapUtils.newLdapName("cn=Sylvester McCoy,ou=Doctors,o=Whoniverse"), "McCoy", Arrays
                    .asList(new String[] { "Seventh Doctor" }), 7, null),
            new Person(LdapUtils.newLdapName("cn=Paul McGann,ou=Doctors,o=Whoniverse"), "McGann", Arrays
                    .asList(new String[] { "Eigth Doctor" }), 8, photo), };

    // Create some entries, read them back and check they are what we'd expect.
    @Test
    public void create() throws Exception {
        for (Person person : createTestData) {
            LOG.debug(String.format("creating - %1$s", person));
            odmManager.create(person);
        }
        LOG.debug("Created all, reading back");
        new ExecuteRunnable<Person>().runTests(new RunnableTest<Person>() {
            public void runTest(Person testData) {
                Name dn = testData.getDn();
                LOG.debug(String.format("reading - %1$s", dn));
                Person personEntry = odmManager.read(Person.class, dn);
                LOG.debug(String.format("read - %1$s", personEntry));
                assertEquals(testData, personEntry);
            }
        }, createTestData);
    }

    // Update an entry from the test data set, read it back and check it is what we'd expect.
    @Test
    public void update() throws Exception {
        Person william = personTestData[PersonName.WILLIAM.getIndex()];
        william.setTelephoneNumber(666);
        william.setSurname("Harvey");
        odmManager.update(william);
        Person readWilliam = odmManager.read(Person.class, william.getDn());
        assertEquals(william, readWilliam);
    }

    private Person[] deleteData = {
            personTestData[PersonName.JON.getIndex()],
            personTestData[PersonName.TOM.getIndex()], personTestData[PersonName.DAVROS.getIndex()], };

    private Person[] whatsLeft = {
            personTestData[PersonName.WILLIAM.getIndex()],
            personTestData[PersonName.PATRICK.getIndex()], personTestData[PersonName.PETER.getIndex()],
            personTestData[PersonName.DALEKS.getIndex()], personTestData[PersonName.MASTER.getIndex()], };

    // Delete a some entries from the the test data set and check what's left is what we'd expect
    @Test
    public void delete() throws Exception {
        for (Person toDelete : deleteData) {
            LOG.debug(String.format("deleting - %1$s", toDelete.getDn()));
            odmManager.delete(toDelete);
        }

        List<Person> allPeople = odmManager.findAll(Person.class, baseName, searchControls);
        assertEquals(new HashSet<Person>(Arrays.asList(whatsLeft)), new HashSet<Person>(allPeople));
    }

    // Trying to read a non-existant entry should be flagged as an error
    @Test(expected = NameNotFoundException.class)
    public void readNonExistant() throws Exception {
        odmManager.read(Person.class, LdapUtils.newLdapName("cn=Hili Harvey,ou=Doctors,o=Whoniverse"));
    }

    // Read an entry with classes in addition to those supported by the Entry
    @Test(expected = OdmException.class)
    public void readNonMatchingObjectclasses() throws Exception {
        odmManager.read(Person.class, LdapUtils.newLdapName("ou=Doctors,o=Whoniverse"));
    }

    private final static class NoEntry {
        @SuppressWarnings("unused")
		@Id
        Name id;
    }

    // Every class to be managed must be annotated @Entry
    @Test(expected = MetaDataException.class)
    public void noEntryAnnotation() {
        ((OdmManagerImpl)odmManager).addManagedClass(NoEntry.class);
    }

    @Entry(objectClasses="test")
    private final static class NoId {
    }

    // There must be a field with the @Id annotation
    @Test(expected = MetaDataException.class)
    public void noId() {
        ((OdmManagerImpl)odmManager).addManagedClass(NoId.class);
    }

    @Entry(objectClasses="test")
    private final static class TwoIds {
        @SuppressWarnings("unused")
        @Id
        private Name firstId;

        @SuppressWarnings("unused")
        @Id
        private Name secondId;

        @SuppressWarnings("unused")
		public TwoIds() {
        }
    }

    // Only one field may be annotated @Id
    @Test(expected = MetaDataException.class)
    public void twoIds() {
        ((OdmManagerImpl)odmManager).addManagedClass(TwoIds.class);
    }

    @Entry(objectClasses="test")
    public final static class NoConstructor {
        @SuppressWarnings("unused")
        @Id
        private Name id;

        public NoConstructor(String aValue) {
        }
    }

    // All Entry annotated classes must have a zero argument public constructor
    @Test(expected = InvalidEntryException.class)
    public void noConstructor() {
        ((OdmManagerImpl)odmManager).addManagedClass(NoConstructor.class);
    }

    @Entry(objectClasses="test")
    public final static class AttributeOnId {
        @SuppressWarnings("unused")
        @Id
        @Attribute
        private Name id;
    }

    // It is illegal put put both the Id and the Attribute annotation on the same field
    @Test(expected = MetaDataException.class)
    public void attributeOnId() {
        ((OdmManagerImpl)odmManager).addManagedClass(AttributeOnId.class);
    }

    @Entry(objectClasses="test")
    public final static class IdIsNotAName {
        @SuppressWarnings("unused")
        @Id
        private String id;
    }

    // The field annotation with @Id must be of type javax.naming.Name
    @Test(expected = MetaDataException.class)
    public void idIsNotAName() {
        ((OdmManagerImpl)odmManager).addManagedClass(IdIsNotAName.class);
    }

    @Entry(objectClasses="test")
    public final static class MissingConverter {
        @SuppressWarnings("unused")
        @Id
        private Name id;

        @SuppressWarnings("unused")
        private BufferedImage image;
    }

    // The OdmManager should flag any missing converters when it is instantiated
    @Test(expected = InvalidEntryException.class)
    public void missingConverter() {
        ((OdmManagerImpl)odmManager).addManagedClass(MissingConverter.class);
    }

    @Entry(objectClasses="test")
    public final static class WrongClassForOc {
        @SuppressWarnings("unused")
        @Id
        private Name id;

        @SuppressWarnings("unused")
        @Attribute(name="objectClass")
        private int ocs;
    }

    // The OdmManager should flag if the objectClass attribute is not of the appropriate type
    @Test(expected = MetaDataException.class)
    public void wrongClassForOc() {
        ((OdmManagerImpl)odmManager).addManagedClass(WrongClassForOc.class);
    }

    // The OdmManager should flag any attempt to use a "unmanaged" class
    @Test(expected = MetaDataException.class)
    public void unManagedClass() {
        odmManager.read(Integer.class, baseName);
    }

    @Test
    public void updateWithChildren_Ldap235() throws Exception {
        OrganizationalUnit organizationalUnit = odmManager.read(OrganizationalUnit.class, ouTestData[0].getDn());
        organizationalUnit.setStreet("new street");
        odmManager.update(organizationalUnit);

        OrganizationalUnit updated = odmManager.read(OrganizationalUnit.class, organizationalUnit.getDn());
        assertEquals(organizationalUnit, updated);
    }

    private enum Flag {
        URL("l", "url"),
        USERNAME("u", "username"),
        PASSWORD("p", "password"),
        HELP("h", "help");

        private String shortName;
        private String longName;

        private Flag(String shortName, String longName) {
            this.shortName = shortName;
            this.longName = longName;
        }

        public String getShort() {
            return shortName;
        }

        public String getLong() {
            return longName;
        }

        @Override
        public String toString() {
            return String.format("short=%1$s, long=%2$s", shortName, longName);
        }
    }

    private static final String DEFAULT_LDAP_URL="ldap://localhost:389";
    private static final String DEFAULT_USERNAME="";
    private static final String DEFAULT_PASSWORD="";

    private static final Options options = new Options();
    static {
        options.addOption(Flag.URL.getShort(), Flag.URL.getLong(), true, "Ldap url to bind to, defaults to "+DEFAULT_LDAP_URL);
        options.addOption(Flag.USERNAME.getShort(), Flag.USERNAME.getLong(), true, "DN to bind with, defaults to "+DEFAULT_USERNAME);
        options.addOption(Flag.PASSWORD.getShort(), Flag.PASSWORD.getLong(), true, "Password to bind with defaults to "+DEFAULT_PASSWORD);
        options.addOption(Flag.HELP.getShort(), Flag.HELP.getLong(), false, "Print this help message");
    }


    private static void runLdapTestCases(String url, String username, String password, String[] testCases) throws Exception {

        for (String testCase:testCases) {
            LOG.debug(String.format("Starting ldap test case %1$s", testCase));

            // Set up
            TestLdap testLdap=new TestLdap();
            testLdap.setUp(url, username, password);

            // Run the test
            Method testMethod=testLdap.getClass().getMethod(testCase);
            testMethod.invoke(testLdap);

            // Tear down
            testLdap.tearDown();

            LOG.debug(String.format("Test case %1$s completed", testCase));
        }
    }

    /*
     * Run unit tests as an integration test against an external LDAP server.
     *
     * Three flags are required:
     *
     * -l ldap url of target server
     * -u dn to bind with
     * -p password to bind with
     *
     * The organisation o=Whoniverse must already exists and the bound user must have
     * write permission.
     *
     */
    public static void main(String[] argv) throws Exception {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        if (cmd.hasOption(Flag.HELP.getShort())) {
            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp(120, TestLdap.class.getSimpleName(), null, options, null, true);
            System.exit(0);
        }

        String url=cmd.getOptionValue(Flag.URL.getShort(), DEFAULT_LDAP_URL);
        String username=cmd.getOptionValue(Flag.USERNAME.getShort(), DEFAULT_USERNAME);
        String password=cmd.getOptionValue(Flag.PASSWORD.getShort(), DEFAULT_PASSWORD);

        // Run all the tests
        runLdapTestCases(url, username, password, new String[] { "create", "delete", "findAll", "read", "search", "update", "testSecondOc" } );
    }
}
