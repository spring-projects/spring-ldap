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

package org.springframework.ldap.pool.factory;

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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.AbstractPoolTestCase;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;
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
 */
public class DirContextPoolableObjectFactoryTests extends AbstractPoolTestCase {

	@Test
	public void testRequestControlResetClearsTrackingAndPreservesSnapshot() throws Exception {
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);
		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		Control[] controls = { new PagedResultsControl(10, Control.CRITICAL) };
		context.setRequestControls(controls);
		verify(target).setRequestControls(controls);
		Set<ContextState> snapshot = trackedContext.getChangedState();
		assertThat(snapshot).containsExactly(ContextState.REQUEST_CONTROLS);

		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(snapshot::clear);
		objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject);

		verify(target).setRequestControls(isNull());
		assertThat(trackedContext.getChangedState()).isEmpty();
		assertThat(snapshot).containsExactly(ContextState.REQUEST_CONTROLS);
	}

	@Test
	public void testCustomRequestWithStartTlsIdIsRecorded() throws Exception {
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);
		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
		ExtendedRequest request = mock(ExtendedRequest.class);
		given(request.getID()).willReturn(StartTlsRequest.OID);
		ExtendedResponse response = mock(ExtendedResponse.class);
		given(target.extendedOperation(request)).willReturn(response);

		assertThat(context.extendedOperation(request)).isSameAs(response);

		assertThat(((BorrowerStateAwareContext) context).getChangedState()).containsExactly(ContextState.START_TLS);
		verify(target).extendedOperation(request);
	}

	@Test
	public void testRequestControlChangesAreRecordedWhenSetterFails() throws Exception {
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
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
	public void testStartTlsTrackingIsRetainedWhenOperationFails() throws Exception {
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
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
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
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
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
		BorrowerStateAwareContext trackedContext = (BorrowerStateAwareContext) context;
		StartTlsRequest request = new StartTlsRequest();
		given(target.extendedOperation(request)).willReturn(mock(StartTlsResponse.class));
		context.setRequestControls(new Control[] { new PagedResultsControl(10, Control.CRITICAL) });
		context.extendedOperation(request);

		assertThatExceptionOfType(NamingException.class)
			.isThrownBy(() -> objectFactory.passivateObject(DirContextType.READ_ONLY, pooledObject))
			.withMessageContaining("START_TLS");
		assertThat(trackedContext.getChangedState()).containsExactlyInAnyOrder(ContextState.REQUEST_CONTROLS,
				ContextState.START_TLS);
		verify(target, never()).setRequestControls(isNull());
	}

	@Test
	public void testRequestControlTrackingIsRetainedWhenResetFails() throws Exception {
		DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(this.contextSourceMock);
		LdapContext target = mock(LdapContext.class);
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(target);

		Object pooledObject = objectFactory.makeObject(DirContextType.READ_ONLY);
		LdapContext context = (LdapContext) pooledObject;
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
	public void testProperties() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		try {
			objectFactory.setContextSource(null);
			fail("DirContextPoolableObjectFactory.setContextSource should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
			// Expected
		}

		objectFactory.setContextSource(contextSourceMock);
		final ContextSource contextSource2 = objectFactory.getContextSource();
		assertThat(contextSource2).isEqualTo(contextSourceMock);

		try {
			objectFactory.setDirContextValidator(null);
			fail("DirContextPoolableObjectFactory.setDirContextValidator should have thrown an IllegalArgumentException");
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
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

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
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		DirContext readOnlyContextMock = mock(DirContext.class);

		given(contextSourceMock.getReadOnlyContext()).willReturn(readOnlyContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext);
		assertThat(readOnlyContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testMakeObjectReadWrite() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		DirContext readWriteContextMock = mock(DirContext.class);

		given(contextSourceMock.getReadWriteContext()).willReturn(readWriteContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final Object createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);

		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext);
		assertThat(readWriteContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testValidateObjectAssertions() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		try {
			objectFactory.validateObject(null, dirContextMock);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			objectFactory.validateObject(new Object(), dirContextMock);
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
			objectFactory.validateObject(DirContextType.READ_ONLY, new Object());
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testValidateObject() throws Exception {
		given(dirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock)).willReturn(true);

		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);
		objectFactory.setDirContextValidator(dirContextValidatorMock);

		final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
		assertThat(valid).isTrue();

		// Check exception in validator
		DirContextValidator secondDirContextValidatorMock = mock(DirContextValidator.class);

		given(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock))
			.willThrow(new RuntimeException("Failed to validate"));
		objectFactory.setDirContextValidator(secondDirContextValidatorMock);

		final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, dirContextMock);
		assertThat(valid2).isFalse();
	}

	@Test
	public void testDestroyObjectAssertions() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		try {
			objectFactory.destroyObject(DirContextType.READ_ONLY, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		try {
			objectFactory.validateObject(DirContextType.READ_ONLY, new Object());
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testDestroyObject() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		objectFactory.destroyObject(DirContextType.READ_ONLY, dirContextMock);

		DirContext throwingDirContextMock = Mockito.mock(DirContext.class);

		willThrow(new RuntimeException("Failed to close")).given(throwingDirContextMock).close();

		objectFactory.destroyObject(DirContextType.READ_ONLY, throwingDirContextMock);
		verify(dirContextMock).close();
	}

	private <T> T getInternalState(Object target, String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		field.setAccessible(true);
		return (T) ReflectionUtils.getField(field, target);
	}

}
