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

import javax.naming.directory.DirContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.AbstractPoolTestCase;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

/**
 * @author Eric Dalquist
 */
public class DirContextPoolableObjectFactoryTests extends AbstractPoolTestCase {

	@Test
	public void testProperties() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		assertThatIllegalArgumentException().isThrownBy(() -> objectFactory.setContextSource(null));

		objectFactory.setContextSource(contextSourceMock);
		final ContextSource contextSource2 = objectFactory.getContextSource();
		assertThat(contextSource2).isEqualTo(contextSourceMock);

		assertThatIllegalArgumentException().isThrownBy(() -> objectFactory.setDirContextValidator(null));

		objectFactory.setDirContextValidator(dirContextValidatorMock);
		final DirContextValidator dirContextValidator2 = objectFactory.getDirContextValidator();
		assertThat(dirContextValidator2).isEqualTo(dirContextValidatorMock);
	}

	@Test
	public void testMakeObjectAssertions() throws Exception {
		final DirContextPoolableObjectFactory objectFactory = new DirContextPoolableObjectFactory(contextSourceMock);

		assertThatIllegalArgumentException().isThrownBy(() -> objectFactory.makeObject(DirContextType.READ_ONLY));

		objectFactory.setContextSource(contextSourceMock);

		assertThatIllegalArgumentException().isThrownBy(() -> objectFactory.makeObject(null));
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
		assertThatIllegalArgumentException().isThrownBy(() -> objectFactory.validateObject(null, dirContextMock));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> objectFactory.validateObject(new Object(), dirContextMock));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> objectFactory.validateObject(DirContextType.READ_ONLY, null));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> objectFactory.validateObject(DirContextType.READ_ONLY, new Object()));
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
		assertThatIllegalArgumentException()
			.isThrownBy(() -> objectFactory.destroyObject(DirContextType.READ_ONLY, null));
		assertThatIllegalArgumentException()
			.isThrownBy(() -> objectFactory.validateObject(DirContextType.READ_ONLY, new Object()));
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
