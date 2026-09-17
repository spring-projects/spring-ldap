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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.apache.commons.pool.KeyedObjectPool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

/**
 * @author Eric Dalquist
 * <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingContextTests extends AbstractPoolTestCase {

	@Test
	public void testConstructorAssertions() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new DelegatingContext(null, contextMock, DirContextType.READ_ONLY));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new DelegatingContext(this.keyedObjectPoolMock, null, DirContextType.READ_ONLY));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new DelegatingContext(this.keyedObjectPoolMock, this.contextMock, null));
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

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext2.assertOpen());

		// Close the outer wrapper
		delegatingContext.close();

		final Context delegateContextclosed = delegatingContext.getDelegateContext();
		assertThat(delegateContextclosed).isNull();

		final Context innerDelegateContextclosed = delegatingContext.getInnermostDelegateContext();
		assertThat(innerDelegateContextclosed).isNull();

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext2.assertOpen());

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

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.addToEnvironment(null, null));

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.createSubcontext((Name) null));

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.createSubcontext((String) null));

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.destroySubcontext((Name) null));

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.destroySubcontext((String) null));

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> delegatingContext.removeFromEnvironment(null));
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

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.bind((Name) null, null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.bind((String) null, null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.composeName(null, (Name) null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.composeName(null, (String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.getEnvironment());

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.getNameInNamespace());

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.getNameParser((Name) null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.getNameParser((String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.list((Name) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.list((String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.listBindings((Name) null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.listBindings((String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.lookup((Name) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.lookup((String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.lookupLink((Name) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.lookupLink((String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.rebind((Name) null, null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.rebind((String) null, null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.rename(null, (Name) null));

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> delegatingContext.rename(null, (String) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.unbind((Name) null));

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.unbind((String) null));

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
		willThrow(new Exception("Fake Pool returnObject Exception")).given(keyedObjectPoolMock)
			.returnObject(DirContextType.READ_ONLY, contextMock);

		final DelegatingContext delegatingContext = new DelegatingContext(keyedObjectPoolMock, contextMock,
				DirContextType.READ_ONLY);

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> delegatingContext.close());
	}

}
