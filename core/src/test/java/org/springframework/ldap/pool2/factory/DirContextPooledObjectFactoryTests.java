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
package org.springframework.ldap.pool2.factory;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.Test;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.ldap.pool2.AbstractPoolTestCase;
import org.springframework.util.ReflectionUtils;

import javax.naming.directory.DirContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class DirContextPooledObjectFactoryTests extends AbstractPoolTestCase {

	@Test
	public void testProperties() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

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
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

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
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

		DirContext readOnlyContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadOnlyContext()).thenReturn(readOnlyContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_ONLY);
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
		assertThat(readOnlyContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testMakeObjectReadWrite() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

		DirContext readWriteContextMock = mock(DirContext.class);

		when(contextSourceMock.getReadWriteContext()).thenReturn(readWriteContextMock);
		objectFactory.setContextSource(contextSourceMock);

		final PooledObject createdDirContext = objectFactory.makeObject(DirContextType.READ_WRITE);

		InvocationHandler invocationHandler = Proxy.getInvocationHandler(createdDirContext.getObject());
		assertThat(readWriteContextMock).isEqualTo(getInternalState(invocationHandler, "target"));
	}

	@Test
	public void testValidateObjectAssertions() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

		try {
			PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
			objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}

		objectFactory.setDirContextValidator(dirContextValidatorMock);

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
		when(dirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock)).thenReturn(true);

		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();
		objectFactory.setDirContextValidator(dirContextValidatorMock);

		PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
		final boolean valid = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
		assertThat(valid).isTrue();

		// Check exception in validator
		DirContextValidator secondDirContextValidatorMock = mock(DirContextValidator.class);

		when(secondDirContextValidatorMock.validateDirContext(DirContextType.READ_ONLY, dirContextMock))
				.thenThrow(new RuntimeException("Failed to validate"));
		objectFactory.setDirContextValidator(secondDirContextValidatorMock);

		final boolean valid2 = objectFactory.validateObject(DirContextType.READ_ONLY, pooledObject);
		assertThat(valid2).isFalse();
	}

	@Test
	public void testDestroyObjectAssertions() throws Exception {
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

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
		final DirContextPooledObjectFactory objectFactory = new DirContextPooledObjectFactory();

		PooledObject pooledObject = new DefaultPooledObject(dirContextMock);
		objectFactory.destroyObject(DirContextType.READ_ONLY, pooledObject);

		DirContext throwingDirContextMock = mock(DirContext.class);

		doThrow(new RuntimeException("Failed to close")).when(throwingDirContextMock).close();

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
