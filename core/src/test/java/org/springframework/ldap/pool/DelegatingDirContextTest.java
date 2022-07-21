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
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Eric Dalquist <a
 *		 href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingDirContextTest extends AbstractPoolTestCase {
	@Test
	public void testConstructorAssertions() {
		try {
			new DelegatingDirContext(keyedObjectPoolMock, null,
					DirContextType.READ_ONLY);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			new DelegatingDirContext(keyedObjectPoolMock, dirContextMock, null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testHelperMethods() throws Exception {

		// Wrap the DirContext once
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);

		final Context delegateContext = delegatingDirContext
				.getDelegateContext();
		assertThat(delegateContext).isEqualTo(dirContextMock);

		final DirContext delegateDirContext = delegatingDirContext
				.getDelegateDirContext();
		assertThat(delegateDirContext).isEqualTo(dirContextMock);

		final DirContext innerDelegateDirContext = delegatingDirContext
				.getInnermostDelegateDirContext();
		assertThat(innerDelegateDirContext).isEqualTo(dirContextMock);

		delegatingDirContext.assertOpen();

		// Wrap the wrapper
		KeyedObjectPool secondKeyedObjectPoolMock = mock(KeyedObjectPool.class);

		final DelegatingDirContext delegatingDirContext2 = new DelegatingDirContext(
				secondKeyedObjectPoolMock, delegatingDirContext,
				DirContextType.READ_ONLY);

		final DirContext delegateDirContext2 = delegatingDirContext2
				.getDelegateDirContext();
		assertThat(delegateDirContext2).isEqualTo(delegatingDirContext);

		final DirContext innerDelegateDirContext2 = delegatingDirContext2
				.getInnermostDelegateDirContext();
		assertThat(innerDelegateDirContext2).isEqualTo(dirContextMock);

		delegatingDirContext2.assertOpen();

		// Close the outer wrapper
		delegatingDirContext2.close();

		final DirContext delegateContext2closed = delegatingDirContext2
				.getDelegateDirContext();
		assertThat(delegateContext2closed).isNull();

		final DirContext innerDelegateContext2closed = delegatingDirContext2
				.getInnermostDelegateDirContext();
		assertThat(innerDelegateContext2closed).isNull();

		try {
			delegatingDirContext2.assertOpen();
			fail("delegatingDirContext2.assertOpen() should have thrown a NamingException");
		} catch (NamingException ne) {
			// Expected
		}

		// Close the outer wrapper
		delegatingDirContext.close();

		final DirContext delegateDirContextClosed = delegatingDirContext
				.getDelegateDirContext();
		assertThat(delegateDirContextClosed).isNull();

		final DirContext innerDelegateDirContextClosed = delegatingDirContext
				.getInnermostDelegateDirContext();
		assertThat(innerDelegateDirContextClosed).isNull();

		try {
			delegatingDirContext.assertOpen();
			fail("delegatingDirContext.assertOpen() should have thrown a NamingException");
		} catch (NamingException ne) {
			// Expected
		}

		verify(secondKeyedObjectPoolMock)
				.returnObject(DirContextType.READ_ONLY, dirContextMock);
		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, dirContextMock);
	}

	@Test
	public void testObjectMethods() throws Exception {
		// Wrap the DirContext once
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);
		assertThat(delegatingDirContext.toString()).isEqualTo(dirContextMock.toString());
		delegatingDirContext.hashCode(); // Run it to make sure it doesn't
											// fail

		assertThat(delegatingDirContext.equals(delegatingDirContext)).isTrue();
		assertThat(delegatingDirContext.equals(new Object())).isFalse();

		final DelegatingDirContext delegatingDirContext2 = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);
		assertThat(delegatingDirContext.equals(delegatingDirContext2)).isTrue();
		assertThat(delegatingDirContext2.equals(delegatingDirContext)).isTrue();
		assertThat(delegatingDirContext.equals(dirContextMock)).isTrue();

		// Close the context and try again
		delegatingDirContext.close();

		assertThat(delegatingDirContext.toString()).isEqualTo("DirContext is closed");
		assertThat(delegatingDirContext.hashCode()).isEqualTo(0); // Run it to make
															// sure it doesn't
															// fail

		assertThat(delegatingDirContext.equals(delegatingDirContext)).isTrue();
		assertThat(delegatingDirContext.equals(new Object())).isFalse();

		assertThat(delegatingDirContext.equals(delegatingDirContext2)).isFalse();
		assertThat(delegatingDirContext2.equals(delegatingDirContext)).isFalse();
		assertThat(delegatingDirContext.equals(dirContextMock)).isFalse();

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, dirContextMock);
	}

	@Test
	public void testUnsupportedMethods() throws Exception {
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);

		try {
			delegatingDirContext.createSubcontext((Name) null, null);
			fail("DelegatingDirContext.createSubcontext Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingDirContext.createSubcontext((String) null, null);
			fail("DelegatingDirContext.createSubcontext Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingDirContext.getSchema((Name) null);
			fail("DelegatingDirContext.getSchema Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingDirContext.getSchema((String) null);
			fail("DelegatingDirContext.getSchema Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingDirContext.getSchemaClassDefinition((Name) null);
			fail("DelegatingDirContext.getSchemaClassDefinition Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
		try {
			delegatingDirContext.getSchemaClassDefinition((String) null);
			fail("DelegatingDirContext.getSchemaClassDefinition Should have thrown an UnsupportedOperationException");
		} catch (UnsupportedOperationException uoe) {
			// Expected
		}
	}

	@Test
	public void testAllMethodsOpened() throws Exception {
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);

		delegatingDirContext.bind((Name) null, null, null);
		delegatingDirContext.bind((String) null, null, null);
		delegatingDirContext.getAttributes((Name) null, null);
		delegatingDirContext.getAttributes((Name) null);
		delegatingDirContext.getAttributes((String) null, null);
		delegatingDirContext.getAttributes((String) null);
		delegatingDirContext.modifyAttributes((Name) null, 0, null);
		delegatingDirContext.modifyAttributes((Name) null, null);
		delegatingDirContext.modifyAttributes((String) null, 0, null);
		delegatingDirContext.modifyAttributes((String) null, null);
		delegatingDirContext.rebind((Name) null, null, null);
		delegatingDirContext.rebind((String) null, null, null);
		delegatingDirContext.search((Name) null, (Attributes) null, null);
		delegatingDirContext.search((Name) null, null);
		delegatingDirContext.search((Name) null, null, null, null);
		delegatingDirContext.search((Name) null, (String) null, null);
		delegatingDirContext.search((String) null, (Attributes) null, null);
		delegatingDirContext.search((String) null, null);
		delegatingDirContext.search((String) null, null, null, null);
		delegatingDirContext.search((String) null, (String) null, null);
	}

	@Test
	public void testAllMethodsClosed() throws Exception {
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);

		delegatingDirContext.close();

		try {
			delegatingDirContext.bind((Name) null, null, null);
			fail("DelegatingDirContext.bind should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.bind((String) null, null, null);
			fail("DelegatingDirContext.bind should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.getAttributes((Name) null, null);
			fail("DelegatingDirContext.getAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.getAttributes((Name) null);
			fail("DelegatingDirContext.getAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.getAttributes((String) null, null);
			fail("DelegatingDirContext.getAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.getAttributes((String) null);
			fail("DelegatingDirContext.getAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.modifyAttributes((Name) null, 0, null);
			fail("DelegatingDirContext.modifyAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.modifyAttributes((Name) null, null);
			fail("DelegatingDirContext.modifyAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.modifyAttributes((String) null, 0, null);
			fail("DelegatingDirContext.modifyAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.modifyAttributes((String) null, null);
			fail("DelegatingDirContext.modifyAttributes should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.rebind((Name) null, null, null);
			fail("DelegatingDirContext.rebind should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.rebind((String) null, null, null);
			fail("DelegatingDirContext.rebind should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((Name) null, (Attributes) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((Name) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((Name) null, null, null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((Name) null, (String) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((String) null, (Attributes) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((String) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((String) null, null, null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}
		try {
			delegatingDirContext.search((String) null, (String) null, null);
			fail("DelegatingDirContext.search should have thrown a NamingException");
		} catch (NamingException ne) {
			//  Expected
		}

		verify(keyedObjectPoolMock).returnObject(DirContextType.READ_ONLY, dirContextMock);
	}

	@Test
	public void testDoubleClose() throws Exception {
		final DelegatingDirContext delegatingDirContext = new DelegatingDirContext(
				keyedObjectPoolMock, dirContextMock, DirContextType.READ_ONLY);

		delegatingDirContext.close();

		// noop close
		delegatingDirContext.close();

		verify(keyedObjectPoolMock, times(1)).returnObject(DirContextType.READ_ONLY, dirContextMock);
	}
}
