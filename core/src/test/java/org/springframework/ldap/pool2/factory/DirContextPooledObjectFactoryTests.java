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

package org.springframework.ldap.pool2.factory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.AbstractPoolTestCase;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

/**
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class DirContextPooledObjectFactoryTests extends AbstractPoolTestCase {

	@Test
	public void testProperties() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		try {
			objectFactory.setContextSource(null);
			fail("DirContextPooledObjectFactory.setContextSource should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}

		objectFactory.setContextSource(contextSourceMock);
		final ContextSource contextSource2 = objectFactory.getContextSource();
		assertThat(contextSource2).isEqualTo(contextSourceMock);

		try {
			objectFactory.setDirContextValidator(null);
			fail("DirContextPooledObjectFactory.setDirContextValidator should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}

		objectFactory.setDirContextValidator(dirContextValidatorMock);
		final DirContextValidator dirContextValidator2 = objectFactory.getDirContextValidator();
		assertThat(dirContextValidator2).isEqualTo(dirContextValidatorMock);
	}

	@Test
	public void testMakeObjectAssertions() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		try {
			objectFactory.makeObject(DirContextType.READ_ONLY);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		objectFactory.setContextSource(contextSourceMock);

		try {
			objectFactory.makeObject(null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testMakeObjectReadOnly() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		DirContext readOnlyContextMock = mock(DirContext.class);

		given(contextSourceMock.getReadOnlyContext()).willReturn(readOnlyContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
		assertThat(readOnlyContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testChangedStateIsAnUnmodifiableSnapshot() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);
		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		context.setRequestControls(new Control[] { new PagedResultsControl(10, Control.CRITICAL) });
		Set<ContextState> snapshot = trackedContext.getChangedState();

		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(snapshot::clear);
		objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject);

		assertThat(trackedContext.getChangedState()).isEmpty();
		assertThat(snapshot).containsExactly(ContextState.REQUEST_CONTROLS);
	}

	@Test
	public void testCustomRequestWithStartTlsIdIsRecorded() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);
		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		ExtendedRequest request = mock(ExtendedRequest.class);
		given(request.getID()).willReturn(StartTlsRequest.OID);
		ExtendedResponse response = mock(ExtendedResponse.class);
		given(target.extendedOperation(request)).willReturn(response);

		assertThat(context.extendedOperation(request)).isSameAs(response);

		assertThat(((BorrowerStateAwareContext) context).getChangedState()).containsExactly(ContextState.START_TLS);
		verify(target).extendedOperation(request);
	}

	@Test
	public void testRequestControlChangesAreRecorded() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;

		assertThat(trackedContext.getChangedState()).isEmpty();

		Control[] controls = { new PagedResultsControl(10, Control.CRITICAL) };
		context.setRequestControls(controls);

		verify(target).setRequestControls(controls);
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.REQUEST_CONTROLS);
	}

	@Test
	public void testRequestControlChangesAreRecordedWhenSetterFails() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		assertThat(trackedContext.getChangedState()).isEmpty();

		Control[] controls = { new PagedResultsControl(10, Control.CRITICAL) };
		NamingException setterFailure = new NamingException("Cannot set request controls");
		willThrow(setterFailure).given(target).setRequestControls(controls);

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.setRequestControls(controls))
			.isSameAs(setterFailure);
		verify(target).setRequestControls(controls);
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.REQUEST_CONTROLS);
	}

	@Test
	public void testStartTlsAttemptsAreRecorded() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		assertThat(trackedContext.getChangedState()).isEmpty();

		StartTlsRequest request = new StartTlsRequest();
		StartTlsResponse response = mock(StartTlsResponse.class);
		given(target.extendedOperation(request)).willReturn(response);

		assertThat(context.extendedOperation(request)).isSameAs(response);

		verify(target).extendedOperation(request);
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.START_TLS);
	}

	@Test
	public void testStartTlsTrackingIsRetainedWhenOperationFails() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		StartTlsRequest request = new StartTlsRequest();
		NamingException operationFailure = new NamingException("StartTLS operation failed");
		given(target.extendedOperation(request)).willThrow(operationFailure);

		assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.extendedOperation(request))
			.isSameAs(operationFailure);
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.START_TLS);

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject))
			.withMessageContaining("START_TLS");
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.START_TLS);
		verify(target).extendedOperation(request);
	}

	@Test
	public void testOtherExtendedOperationsDoNotRecordStartTlsState() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		ExtendedRequest request = mock(ExtendedRequest.class);
		given(request.getID()).willReturn("1.3.6.1.4.1.4203.1.11.3");
		ExtendedResponse response = mock(ExtendedResponse.class);
		given(target.extendedOperation(request)).willReturn(response);

		assertThat(context.extendedOperation(request)).isSameAs(response);
		verify(target).extendedOperation(request);
		assertThat(trackedContext.getChangedState()).isEmpty();

		objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject);

		verify(target).setRequestControls(isNull());
		assertThat(trackedContext.getChangedState()).isEmpty();
	}

	@Test
	public void testPassivationRejectsStartTlsState() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		StartTlsRequest request = new StartTlsRequest();
		given(target.extendedOperation(request)).willReturn(mock(StartTlsResponse.class));
		context.extendedOperation(request);

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject))
			.withMessageContaining("START_TLS");
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.START_TLS);
		verify(target, never()).setRequestControls(isNull());
	}

	@Test
	public void testRequestControlTrackingIsClearedAfterPassivation() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);

		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);

		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;

		context.setRequestControls(new Control[] { new PagedResultsControl(10, Control.CRITICAL) });

		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.REQUEST_CONTROLS);

		objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject);

		verify(target).setRequestControls(isNull());
		assertThat(trackedContext.getChangedState()).isEmpty();
	}

	@Test
	public void testRequestControlTrackingIsRetainedWhenResetFails() throws Exception {
		DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		PooledObject<Object> pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject.getObject();
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;

		context.setRequestControls(new Control[] { new PagedResultsControl(10, Control.CRITICAL) });
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.REQUEST_CONTROLS);

		NamingException resetFailure = new NamingException("Cannot clear request controls");
		willThrow(resetFailure).given(target).setRequestControls(isNull());

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject))
			.isSameAs(resetFailure);
		assertThat(trackedContext.getChangedState()).containsExactly(ContextState.REQUEST_CONTROLS);
	}

	@Test
	public void testMakeObjectReadWrite() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		DirContext readWriteContextMock = mock(DirContext.class);

		given(contextSourceMock.getReadWriteContext()).willReturn(readWriteContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);

		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
		assertThat(readWriteContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testValidateObjectAssertions() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		try {
			PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
			objectFactory.validateObject(null, pooledObject);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
			objectFactory.validateObject(new Object(), pooledObject);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			objectFactory.validateObject(DirContextType.READ_ONLY, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			PooledObject pooledObject = new DefaultPooledObject(new Object());
			objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testValidateObject() throws Exception {
		given(dirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock)).willReturn(true);

		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);
		objectFactory.setDirContextValidator(dirContextValidatorMock);

		PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
		final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
		assertThat(valid).isTrue();

		// Check exception in validator
		DirContextValidator secondDirContextValidatorMock = mock(DirContextValidator.class);

		given(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock))
			.willThrow(new RuntimeException("Failed to validate"));
		objectFactory.setDirContextValidator(secondDirContextValidatorMock);

		final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
		assertThat(valid2).isFalse();
	}

	@Test
	public void testDestroyObjectAssertions() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		try {
			objectFactory.destroyObject(DirContextType.READ_ONLY, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			PooledObject pooledObject = new DefaultPooledObject(new Object());
			objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testDestroyObject() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory(contextSourceMock);

		PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
		objectFactory.destroyObject(DirContextType.READ_ONLY, pooledObject);

		DirContext throwingDirContextMock = mock(DirContext.class);

		willThrow(new RuntimeException("Failed to close")).given(throwingDirContextMock).close();

		pooledObject = new DefaultPooledObject(throwingDirContextMock);
		objectFactory.destroyObject(DirContextType.READ_ONLY, pooledObject);
		verify(dirContextMock).close();
	}

	private <T> T getInternalState(Object target, String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		field.setAccessible(true);
		return (T) ReflectionUtils.getField(field, target);
	}

}
