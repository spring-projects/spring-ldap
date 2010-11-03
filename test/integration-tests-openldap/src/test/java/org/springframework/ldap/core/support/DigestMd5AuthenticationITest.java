/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.springframework.ldap.core.support;

import javax.naming.directory.DirContext;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Integration test to verify DIGEST-MD5 authentication support.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateDigestMd5TestContext.xml" })
public class DigestMd5AuthenticationITest extends AbstractJUnit4SpringContextTests {
	@Autowired
	private LdapTemplate ldapTemplate;
	

	@Test
	public void testAuthenticate() {
		try {
			DirContext ctxt = ldapTemplate.getContextSource().getContext("admin", "secret");
			Assert.assertNotNull(ctxt);
		} catch (NamingException e) {
			Assert.fail("DIGEST-MD5 authentication failed: " + e);
		}
		
	}
}
