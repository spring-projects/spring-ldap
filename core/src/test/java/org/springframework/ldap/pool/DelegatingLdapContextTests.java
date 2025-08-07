/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.pool;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool.KeyedObjectPool;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Eric Dalquist
 * <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingLdapContextTests extends AbstractPoolTestCase {

	@Test
	public void testConstructorAssertions() {
		try {
			new DelegatingLdapContext(keyedObjectPoolMock, null, DirContextType.READ_ONLY);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new DelegatingLdapContext(keyedObjectPoolMock, ldapContextMock, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testHelperMethods() throws Exception {
		// Wrap the LdapContext once
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);

		final DirContext delegateDirContext = delegatingLdapContext.getDelegateDirContext();
		assertThat(delegateDirContext).isEqualTo(ldapContextMock);

		final LdapContext delegateLdapContext = delegatingLdapContext.getDelegateLdapContext();
		assertThat(delegateLdapContext).isEqualTo(ldapContextMock);

		final LdapContext innerDelegateLdapContext = delegatingLdapContext.getInnermostDelegateLdapContext();
		assertThat(innerDelegateLdapContext).isEqualTo(ldapContextMock);

		delegatingLdapContext.assertOpen();

		// Wrap the wrapper
		KeyedObjectPool secondKeyedObjectPoolMock = mock(KeyedObjectPool.class);

		final DelegatingLdapContext delegatingLdapContext2 = new DelegatingLdapContext(secondKeyedObjectPoolMock,
				delegatingLdapContext, DirContextType.READ_ONLY);

		final LdapContext delegateLdapContext2 = delegatingLdapContext2.getDelegateLdapContext();
		assertThat(delegateLdapContext2).isEqualTo(delegatingLdapContext);

		final LdapContext innerDelegateLdapContext2 = delegatingLdapContext2.getInnermostDelegateLdapContext();
		assertThat(innerDelegateLdapContext2).isEqualTo(ldapContextMock);

		delegatingLdapContext2.assertOpen();

		// Close the outer wrapper
		delegatingLdapContext2.close();

		final LdapContext delegateContext2closed = delegatingLdapContext2.getDelegateLdapContext();
		assertThat(delegateContext2closed).isNull();

		final LdapContext innerDelegateContext2closed = delegatingLdapContext2.getInnermostDelegateLdapContext();
		assertThat(innerDelegateContext2closed).isNull();

		try {
			delegatingLdapContext2.assertOpen();
			fail("delegatingLdapContext2.assertOpen() should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		// Close the outer wrapper
		delegatingLdapContext.close();

		final LdapContext delegateLdapContextClosed = delegatingLdapContext.getDelegateLdapContext();
		assertThat(delegateLdapContextClosed).isNull();

		final LdapContext innerDelegateLdapContextClosed = delegatingLdapContext.getInnermostDelegateLdapContext();
		assertThat(innerDelegateLdapContextClosed).isNull();

		try {
			delegatingLdapContext.assertOpen();
			fail("delegatingLdapContext.assertOpen() should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		verify(secondKeyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, ldapContextMock);
		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, ldapContextMock);
	}

	@Test
	public void testObjectMethods() throws Exception {
		// Wrap the LdapContext once
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);
		assertThat(delegatingLdapContext.toString()).isEqualTo(ldapContextMock.toString());
		delegatingLdapContext.hashCode(); // Run it to make sure it doesn't fail

		assertThat(delegatingLdapContext.equals(delegatingLdapContext)).isTrue();
		assertThat(delegatingLdapContext.equals(new Object())).isFalse();

		final DelegatingLdapContext delegatingLdapContext2 = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);
		assertThat(delegatingLdapContext.equals(delegatingLdapContext2)).isTrue();
		assertThat(delegatingLdapContext2.equals(delegatingLdapContext)).isTrue();
		assertThat(delegatingLdapContext.equals(ldapContextMock)).isTrue();

		// Close the context and try again
		delegatingLdapContext.close();

		assertThat(delegatingLdapContext.toString()).isEqualTo("LdapContext is closed");
		assertThat(delegatingLdapContext.hashCode()).isEqualTo(0); // Run it to make
		// sure it doesn't
		// fail

		assertThat(delegatingLdapContext.equals(delegatingLdapContext)).isTrue();
		assertThat(delegatingLdapContext.equals(new Object())).isFalse();

		assertThat(delegatingLdapContext.equals(delegatingLdapContext2)).isFalse();
		assertThat(delegatingLdapContext2.equals(delegatingLdapContext)).isFalse();
		assertThat(delegatingLdapContext.equals(ldapContextMock)).isFalse();

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, ldapContextMock);
	}

	@Test
	public void testUnsupportedMethods() throws Exception {
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);

		try {
			delegatingLdapContext.newInstance(null);
			fail("DelegatingLdapContext.newInstance Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingLdapContext.reconnect(null);
			fail("DelegatingLdapContext.reconnect Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingLdapContext.setRequestControls(null);
			fail("DelegatingLdapContext.setRequestControls Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
	}

	// nice
	@Test
	public void testAllMethodsOpened() throws Exception {
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);

		delegatingLdapContext.extendedOperation(null);
		delegatingLdapContext.getConnectControls();
		delegatingLdapContext.getRequestControls();
		delegatingLdapContext.getResponseControls();
	}

	@Test
	public void testAllMethodsClosed() throws Exception {
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);

		delegatingLdapContext.close();

		try {
			delegatingLdapContext.extendedOperation(null);
			fail("DelegatingLdapContext.extendedOperation should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingLdapContext.getConnectControls();
			fail("DelegatingLdapContext.getConnectControls should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingLdapContext.getRequestControls();
			fail("DelegatingLdapContext.getRequestControls should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingLdapContext.getResponseControls();
			fail("DelegatingLdapContext.getResponseControls should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, ldapContextMock);
	}

	@Test
	public void testDoubleClose() throws Exception {
		final DelegatingLdapContext delegatingLdapContext = new DelegatingLdapContext(keyedObjectPoolMock,
				ldapContextMock, DirContextType.READ_ONLY);

		delegatingLdapContext.close();

		// noop close
		delegatingLdapContext.close();

		verify(keyedObjectPoolMock, times(1)).returnObject(DirContextType.READ_ONLY, ldapContextMock);
	}

}
