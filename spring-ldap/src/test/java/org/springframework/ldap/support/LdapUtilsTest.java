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
package org.springframework.ldap.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.ldap.Control;

import junit.framework.TestCase;

import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.LdapReferralException;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.LinkLoopException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.SizeLimitExceededException;

/**
 * Unit tests for the LdapUtils class.
 * 
 * @author Ulrik Sandberg
 */
public class LdapUtilsTest extends TestCase {

	// need a concrete subclass for testing
	private final class LdapReferralExceptionExtension extends javax.naming.ldap.LdapReferralException {
		private LdapReferralExceptionExtension(String explanation) {
			super(explanation);
		}

		public Context getReferralContext() throws javax.naming.NamingException {
			return null;
		}

		public Context getReferralContext(Hashtable env) throws javax.naming.NamingException {
			return null;
		}

		public Context getReferralContext(Hashtable env, Control[] reqCtls) throws javax.naming.NamingException {
			return null;
		}

		public Object getReferralInfo() {
			return null;
		}

		public void retryReferral() {
		}

		public boolean skipReferral() {
			return false;
		}
	}

	public void testTranslatesToNarrowestPossibleSubclass1() throws Exception {
		javax.naming.SizeLimitExceededException exception = new javax.naming.SizeLimitExceededException("some error");
		NamingException result = LdapUtils.convertLdapException(exception);
		assertEquals(SizeLimitExceededException.class, result.getClass());
	}

	public void testTranslatesToSuperClass() throws Exception {
		javax.naming.LimitExceededException exception = new javax.naming.LimitExceededException("some error");
		NamingException result = LdapUtils.convertLdapException(exception);
		assertEquals(LimitExceededException.class, result.getClass());
	}

	public void testTranslatesToNarrowestPossibleSubclass2() throws Exception {
		javax.naming.LinkLoopException exception = new javax.naming.LinkLoopException("some error");
		NamingException result = LdapUtils.convertLdapException(exception);
		assertEquals(LinkLoopException.class, result.getClass());
	}

	public void testTranslatesToNarrowestPossibleSubclass3() throws Exception {
		javax.naming.AuthenticationException exception = new javax.naming.AuthenticationException("some error");
		NamingException result = LdapUtils.convertLdapException(exception);
		assertEquals(AuthenticationException.class, result.getClass());
	}

	public void testTranslatesToNarrowestPossibleAbstractSubclass() throws Exception {
		LdapReferralExceptionExtension exception = new LdapReferralExceptionExtension("some error");
		NamingException result = LdapUtils.convertLdapException(exception);
		assertEquals(LdapReferralException.class, result.getClass());
	}
}
