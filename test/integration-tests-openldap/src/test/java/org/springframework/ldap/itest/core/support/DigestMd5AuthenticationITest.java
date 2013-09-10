/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.springframework.ldap.itest.core.support;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.naming.directory.DirContext;

/**
 * Integration test to verify DIGEST-MD5 authentication support.
 * 
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateDigestMd5TestContext.xml" })
public class DigestMd5AuthenticationITest extends AbstractJUnit4SpringContextTests {
	@Autowired
	private LdapTemplate ldapTemplate;

    @Autowired
    @Qualifier("populateContextSource")
    private ContextSource contextSource;

    @Before
    public void prepareTestedInstance() throws Exception {
        LdapTestUtils.cleanAndSetup(
                contextSource,
                LdapUtils.newLdapName("ou=People"),
                new ClassPathResource("/setup_data.ldif"));
    }

    @After
    public void cleanup() throws Exception {
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.newLdapName("ou=People"));
    }

    @Test
	public void testAuthenticate() {
        DirContext ctxt = ldapTemplate.getContextSource().getContext("some.person1", "password");
        Assert.assertNotNull(ctxt);
	}
}
