package org.springframework.ldap.itest.ad;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultIncrementalAttributesMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration("classpath:/incrementalAttributeMapperTest.xml")
public class IncrementalAttributeMapperITest extends AbstractJUnit4SpringContextTests {

    private static final DistinguishedName BASE_DN = new DistinguishedName("ou=dummy,dc=261consulting,dc=local");
    private static final DistinguishedName OU_DN = new DistinguishedName("ou=dummy");
    private static final DistinguishedName GROUP_DN = new DistinguishedName(OU_DN).append("cn", "testgroup");
    private static final String DEFAULT_PASSWORD = "ahcoophah5Oi4oh";

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Before
    public void prepareTestData() throws UnsupportedEncodingException {
        cleanup();

        createBaseOu();
        for (int i = 0; i < 1502; i++) {
            createUser("test" + i);
        }

        createGroup();
    }

    private void createGroup() {
        DirContextAdapter ctx = new DirContextAdapter(GROUP_DN);

        ctx.addAttributeValue("objectclass", "top");
        ctx.addAttributeValue("objectclass", "group");
        ctx.addAttributeValue("cn", "testgroup");
        ctx.addAttributeValue("sAMAccountName", "TESTGROUP");

        for (int i = 0; i < 1501; i++) {
            ctx.addAttributeValue("member", buildUserRefDn("test" + i));
        }

        ldapTemplate.bind(ctx);
    }

    private String buildUserRefDn(String username) {
        return new DistinguishedName(BASE_DN).append("cn", username).toString();
    }

    private void createBaseOu() {
        createOu();
    }

    private void createUser(String username) throws UnsupportedEncodingException {
        DirContextAdapter ctx = new DirContextAdapter(new DistinguishedName(OU_DN).append("cn", username));

        ctx.addAttributeValue("objectclass", "top");
        ctx.addAttributeValue("objectclass", "person");
        ctx.addAttributeValue("objectclass", "organizationalPerson");
        ctx.addAttributeValue("objectclass", "user");

        ctx.setAttributeValue("givenName", username);
        ctx.setAttributeValue("userPrincipalName", username + "@example.com");
        ctx.setAttributeValue("cn", username);
        ctx.setAttributeValue("description", "Dummy user");
        ctx.setAttributeValue("sAMAccountName", username.toUpperCase() + "." + username.toUpperCase());
        ctx.setAttributeValue("userAccountControl", "512");

        String newQuotedPassword = "\"" + DEFAULT_PASSWORD + "\"";
        ctx.setAttributeValue("unicodePwd", newQuotedPassword.getBytes("UTF-16LE"));

        ldapTemplate.bind(ctx);
    }

    private void createOu() {
        DirContextAdapter ctx = new DirContextAdapter(OU_DN);

        ctx.addAttributeValue("objectClass", "top");
        ctx.addAttributeValue("objectClass", "organizationalUnit");

        ctx.setAttributeValue("ou", "dummy");
        ctx.setAttributeValue("description", "dummy description");

        ldapTemplate.bind(ctx);
    }

    @After
    public void cleanup() {
        try {
            ldapTemplate.lookup(OU_DN);
        } catch (NameNotFoundException e) {
            // Nothing to cleanup
            return;
        }

        while (true) {
            try {
                ldapTemplate.unbind(OU_DN, true);
                // Everything is deleted
                return;
            } catch (SizeLimitExceededException e) {
                // There's more to delete
            }
        }
    }


    @Test
    public void verifyRetrievalOfLotsOfAttributeValues() {
        DistinguishedName testgroupDn = new DistinguishedName(OU_DN).append("cn", "testgroup");

        // The 'member' attribute consists of > 1500 entries and will not be returned without range specifier.
        DirContextOperations ctx = ldapTemplate.lookupContext(testgroupDn);
        assertNull(ctx.getStringAttribute("member"));

        DefaultIncrementalAttributesMapper attributeMapper = new DefaultIncrementalAttributesMapper(new String[]{"member", "cn"});
        assertTrue("There should be more results to get", attributeMapper.hasMore());

        String[] attributesArray = attributeMapper.getAttributesForLookup();
        assertEquals(2, attributesArray.length);
        assertEquals("member", attributesArray[0]);
        assertEquals("cn", attributesArray[1]);

        // First iteration - there should now be more members left, but all cn values should have been collected.
        ldapTemplate.lookup(testgroupDn, attributesArray, attributeMapper);

        assertTrue("There should be more results to get", attributeMapper.hasMore());
        // Only member attribute should be requested in this query.
        attributesArray = attributeMapper.getAttributesForLookup();
        assertEquals(1, attributesArray.length);
        assertEquals("member;Range=1500-*", attributesArray[0]);

        // Second iteration - all data should now have been collected.
        ldapTemplate.lookup(testgroupDn, attributeMapper.getAttributesForLookup(), attributeMapper);
        assertFalse("There should be no more results to get", attributeMapper.hasMore());

        List memberValues = attributeMapper.getValues("member");
        assertNotNull(memberValues);
        assertEquals(1501, memberValues.size());

        List cnValues = attributeMapper.getValues("cn");
        assertNotNull(cnValues);
        assertEquals(1, cnValues.size());
    }

    @Test
    public void jiraLdap234ITest() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            transactionTemplate.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    ModificationItem modificationItem = new ModificationItem(
                            DirContext.ADD_ATTRIBUTE,
                            new BasicAttribute("member", buildUserRefDn("test" + 1501)));
                    ldapTemplate.modifyAttributes(GROUP_DN, new ModificationItem[]{modificationItem});

                    // The below should cause a rollback
                    throw new RuntimeException("Simulate some failure");
                }
            });

            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            DefaultIncrementalAttributesMapper attributeMapper = new DefaultIncrementalAttributesMapper(new String[]{"member"});
            while (attributeMapper.hasMore()) {
                ldapTemplate.lookup(GROUP_DN, attributeMapper.getAttributesForLookup(), attributeMapper);
            }

            // LDAP-234: After rollback the attribute values were cleared after rollback
            assertEquals(
                    1501,
                    DefaultIncrementalAttributesMapper.lookupAttributeValues(
                            ldapTemplate, GROUP_DN, "member").size());
        }
    }
}
