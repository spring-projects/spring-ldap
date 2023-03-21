/*
 * Copyright 2005-2016 the original author or authors.
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

import org.apache.commons.pool.KeyedObjectPool;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Eric Dalquist
 * <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingContextTest extends AbstractPoolTestCase {

	@Test
	public void testConstructorAssertions() {
		try {
			new DelegatingContext(null, contextMock, DirContextType.READ_ONLY);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new DelegatingContext(keyedObjectPoolMock, null, DirContextType.READ_ONLY);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new DelegatingContext(keyedObjectPoolMock, contextMock, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testHelperMethods() throws Exception {
		// Wrap the Context once
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		final Context delegateContext = delegatingContext.getDelegateContext();
		assertThat(delegateContext).isEqualTo(contextMock);

		final Context innerDelegateContext = delegatingContext.getInnermostDelegateContext();
		assertThat(innerDelegateContext).isEqualTo(contextMock);

		delegatingContext.assertOpen();

		// Wrap the wrapper
		KeyedObjectPool secondKeyedObjectPoolMock = mock(KeyedObjectPool.class);

		final DelegatingContext delegatingContext2 = new DelegatingContext(secondKeyedObjectPoolMock, delegatingContext,
				DirContextType.READ_ONLY);

		final Context delegateContext2 = delegatingContext2.getDelegateContext();
		assertThat(delegateContext2).isEqualTo(delegatingContext);

		final Context innerDelegateContext2 = delegatingContext2.getInnermostDelegateContext();
		assertThat(innerDelegateContext2).isEqualTo(contextMock);

		delegatingContext2.assertOpen();

		// Close the outer wrapper
		delegatingContext2.close();

		final Context delegateContext2closed = delegatingContext2.getDelegateContext();
		assertThat(delegateContext2closed).isNull();

		final Context innerDelegateContext2closed = delegatingContext2.getInnermostDelegateContext();
		assertThat(innerDelegateContext2closed).isNull();

		try {
			delegatingContext2.assertOpen();
			fail("delegatingContext2.assertOpen() should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		// Close the outer wrapper
		delegatingContext.close();

		final Context delegateContextclosed = delegatingContext.getDelegateContext();
		assertThat(delegateContextclosed).isNull();

		final Context innerDelegateContextclosed = delegatingContext.getInnermostDelegateContext();
		assertThat(innerDelegateContextclosed).isNull();

		try {
			delegatingContext.assertOpen();
			fail("delegatingContext.assertOpen() should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, contextMock);
		verify(secondKeyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, contextMock);
	}

	@Test
	public void testObjectMethods() throws Exception {
		// Wrap the Context once
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);
		assertThat(delegatingContext.toString()).isEqualTo(contextMock.toString());
		delegatingContext.hashCode(); // Run it to make sure it doesn't fail

		assertThat(delegatingContext.equals(delegatingContext)).isTrue();
		assertThat(delegatingContext.equals(new Object())).isFalse();

		final DelegatingContext delegatingContext2 = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);
		assertThat(delegatingContext.equals(delegatingContext2)).isTrue();
		assertThat(delegatingContext2.equals(delegatingContext)).isTrue();
		assertThat(delegatingContext.equals(contextMock)).isTrue();

		// Close the contextMock and try again
		delegatingContext.close();

		assertThat(delegatingContext.toString()).isEqualTo("Context is closed");
		assertThat(delegatingContext.hashCode()).isEqualTo(0); // Run it to make sure
		// it doesn't fail

		assertThat(delegatingContext.equals(delegatingContext)).isTrue();
		assertThat(delegatingContext.equals(new Object())).isFalse();

		assertThat(delegatingContext.equals(delegatingContext2)).isFalse();
		assertThat(delegatingContext2.equals(delegatingContext)).isFalse();
		assertThat(delegatingContext.equals(contextMock)).isFalse();

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, contextMock);
	}

	@Test
	public void testUnsupportedMethods() throws Exception {
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		try {
			delegatingContext.addToEnvironment(null, null);
			fail("DelegatingContext.addToEnvironment Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingContext.createSubcontext((Name) null);
			fail("DelegatingContext.createSubcontext Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingContext.createSubcontext((String) null);
			fail("DelegatingContext.createSubcontext Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingContext.destroySubcontext((Name) null);
			fail("DelegatingContext.destroySubcontext Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingContext.destroySubcontext((String) null);
			fail("DelegatingContext.destroySubcontext Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingContext.removeFromEnvironment(null);
			fail("DelegatingContext.removeFromEnvironment Should have thrown an UnsupportedOperationException");
		}
		catch (UnsupportedOperationException uoe) {
			// Expected
		}
	}

	@Test
	public void testAllMethodsOpened() throws Exception {
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		delegatingContext.bind((Name) null, null);
		delegatingContext.bind((String) null, null);
		delegatingContext.composeName((Name) null, (Name) null);
		delegatingContext.composeName((String) null, (String) null);
		delegatingContext.getEnvironment();
		delegatingContext.getNameInNamespace();
		delegatingContext.getNameParser((Name) null);
		delegatingContext.getNameParser((String) null);
		delegatingContext.list((Name) null);
		delegatingContext.list((String) null);
		delegatingContext.listBindings((Name) null);
		delegatingContext.listBindings((String) null);
		delegatingContext.lookup((Name) null);
		delegatingContext.lookup((String) null);
		delegatingContext.lookupLink((Name) null);
		delegatingContext.lookupLink((String) null);
		delegatingContext.rebind((Name) null, null);
		delegatingContext.rebind((String) null, null);
		delegatingContext.rename((Name) null, (Name) null);
		delegatingContext.rename((String) null, (String) null);
		delegatingContext.unbind((Name) null);
		delegatingContext.unbind((String) null);
	}

	@Test
	public void testAllMethodsClosed() throws Exception {
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		delegatingContext.close();

		try {
			delegatingContext.bind((Name) null, null);
			fail("DelegatingContext.bind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.bind((String) null, null);
			fail("DelegatingContext.bind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.composeName((Name) null, (Name) null);
			fail("DelegatingContext.composeName should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.composeName((String) null, (String) null);
			fail("DelegatingContext.composeName should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.getEnvironment();
			fail("DelegatingContext.getEnvironment should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.getNameInNamespace();
			fail("DelegatingContext.getNameInNamespace should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.getNameParser((Name) null);
			fail("DelegatingContext.getNameParser should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.getNameParser((String) null);
			fail("DelegatingContext.getNameParser should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.list((Name) null);
			fail("DelegatingContext.list should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.list((String) null);
			fail("DelegatingContext.list should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.listBindings((Name) null);
			fail("DelegatingContext.listBindings should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.listBindings((String) null);
			fail("DelegatingContext.listBindings should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.lookup((Name) null);
			fail("DelegatingContext.lookup should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.lookup((String) null);
			fail("DelegatingContext.lookup should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.lookupLink((Name) null);
			fail("DelegatingContext.lookupLink should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.lookupLink((String) null);
			fail("DelegatingContext.lookupLink should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.rebind((Name) null, null);
			fail("DelegatingContext.rebind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.rebind((String) null, null);
			fail("DelegatingContext.rebind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.rename((Name) null, (Name) null);
			fail("DelegatingContext.rename should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.rename((String) null, (String) null);
			fail("DelegatingContext.rename should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.unbind((Name) null);
			fail("DelegatingContext.unbind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
		try {
			delegatingContext.unbind((String) null);
			fail("DelegatingContext.unbind should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, contextMock);
	}

	@Test
	public void testDoubleClose() throws Exception {
		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		delegatingContext.close();

		// noop close
		delegatingContext.close();

		verify(keyedObjectPoolMock, times(1)).returnObject(DirContextType.READ_ONLY, contextMock);
	}

	@Test
	public void testPoolExceptionOnClose() throws Exception {
		doThrow(new Exception("Fake Pool returnObject Exception")).when(keyedObjectPoolMock)
				.returnObject(DirContextType.READ_ONLY, contextMock);

		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		try {
			delegatingContext.close();
			fail("DelegatingContext.close should have thrown a NamingException");
		}
		catch (NamingException ne) {
			// Expected
		}
	}

}
