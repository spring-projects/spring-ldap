/*
 * Copyright 2005-2015 the original author or authors.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.naming.directory.DirContext;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.FailureAwareContext;
import org.springframework.ldap.pool2.validation.DirContextValidator;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;

/**
 * Factory that creates {@link DirContext} instances for pooling via a configured
 * {@link ContextSource}. The {@link DirContext}s are keyed based on if they are read only
 * or read/write. The expected key type is the
 * {@link org.springframework.ldap.pool2.DirContextType} enum.
 *
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 * <tr>
 * <th align="left">Property</th>
 * <th align="left">Description</th>
 * <th align="left">Required</th>
 * <th align="left">Default</th>
 * </tr>
 * <tr>
 * <td valign="top">contextSource</td>
 * <td valign="top">The {@link ContextSource} to get {@link DirContext}s from for adding
 * to the pool.</td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td valign="top">dirContextValidator</td>
 * <td valign="top">The {@link DirContextValidator} to use to validate
 * {@link DirContext}s. This is only required if the pool has validation of any kind
 * turned on.</td>
 * <td valign="top">No</td>
 * <td valign="top">null</td>
 * </tr>
 * </table>
 *
 * @since 2.0
 * @author Eric Dalquist
 * <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @author Mattias Hellborg Arthursson
 * @author Anindya Chatterjee
 */
class DirContextPooledObjectFactory extends BaseKeyedPooledObjectFactory<Object, Object> {

	/**
	 * Logger for this class and subclasses
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Set<Class<? extends Throwable>> DEFAULT_NONTRANSIENT_EXCEPTIONS = new HashSet<Class<? extends Throwable>>();

	static {
		DEFAULT_NONTRANSIENT_EXCEPTIONS.add(CommunicationException.class);
	};

	private ContextSource contextSource;

	private DirContextValidator dirContextValidator;

	private Set<Class<? extends Throwable>> nonTransientExceptions = DEFAULT_NONTRANSIENT_EXCEPTIONS;

	void setNonTransientExceptions(Collection<Class<? extends Throwable>> nonTransientExceptions) {
		this.nonTransientExceptions = new HashSet<Class<? extends Throwable>>(nonTransientExceptions);
	}

	/**
	 * @return the contextSource
	 */
	public ContextSource getContextSource() {
		return this.contextSource;
	}

	/**
	 * @param contextSource the contextSource to set
	 */
	public void setContextSource(ContextSource contextSource) {
		if (contextSource == null) {
			throw new IllegalArgumentException("contextSource may not be null");
		}

		this.contextSource = contextSource;
	}

	/**
	 * @return the dirContextValidator
	 */
	public DirContextValidator getDirContextValidator() {
		return this.dirContextValidator;
	}

	/**
	 * @param dirContextValidator the dirContextValidator to set
	 */
	public void setDirContextValidator(DirContextValidator dirContextValidator) {
		if (dirContextValidator == null) {
			throw new IllegalArgumentException("dirContextValidator may not be null");
		}

		this.dirContextValidator = dirContextValidator;
	}

	private Object makeFailureAwareProxy(DirContext readOnlyContext) {
		return Proxy.newProxyInstance(DirContextProxy.class.getClassLoader(), new Class<?>[] {
				LdapUtils.getActualTargetClass(readOnlyContext), DirContextProxy.class, FailureAwareContext.class },
				new FailureAwareContextProxy(readOnlyContext));
	}

	/**
	 * @see BaseKeyedPooledObjectFactory#validateObject(Object, PooledObject)
	 *
	 */
	@Override
	public boolean validateObject(Object key, PooledObject<Object> pooledObject) {
		Assert.notNull(this.dirContextValidator, "DirContextValidator may not be null");
		Assert.isTrue(key instanceof DirContextType, "key must be a DirContextType");
		Assert.notNull(pooledObject, "The Object to validate must not be null");
		Assert.isTrue(pooledObject.getObject() instanceof DirContext,
				"The Object to validate must be of type '" + DirContext.class + "'");

		try {
			final DirContextType contextType = (DirContextType) key;
			final DirContext dirContext = (DirContext) pooledObject.getObject();
			return this.dirContextValidator.validateDirContext(contextType, dirContext);
		}
		catch (Exception e) {
			this.logger.warn("Failed to validate '" + pooledObject.getObject() + "' due to an unexpected exception.",
					e);
			return false;
		}
	}

	/**
	 * @see BaseKeyedPooledObjectFactory#destroyObject(Object, PooledObject)
	 *
	 */
	@Override
	public void destroyObject(Object key, PooledObject<Object> pooledObject) throws Exception {
		Assert.notNull(pooledObject, "The Object to destroy must not be null");
		Assert.isTrue(pooledObject.getObject() instanceof DirContext,
				"The Object to destroy must be of type '" + DirContext.class + "'");

		try {
			final DirContext dirContext = (DirContext) pooledObject.getObject();
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Closing " + key + " DirContext='" + dirContext + "'");
			}
			dirContext.close();
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Closed " + key + " DirContext='" + dirContext + "'");
			}
		}
		catch (Exception e) {
			this.logger.warn("An exception occured while closing '" + pooledObject.getObject() + "'", e);
		}
	}

	/**
	 * @see BaseKeyedPooledObjectFactory#create(Object)
	 *
	 */
	@Override
	public Object create(Object key) throws Exception {
		Assert.notNull(this.contextSource, "ContextSource may not be null");
		Assert.isTrue(key instanceof DirContextType, "key must be a DirContextType");

		final DirContextType contextType = (DirContextType) key;
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Creating a new " + contextType + " DirContext");
		}

		if (contextType == DirContextType.READ_WRITE) {
			final DirContext readWriteContext = this.contextSource.getReadWriteContext();

			if (this.logger.isDebugEnabled()) {
				this.logger
						.debug("Created new " + DirContextType.READ_WRITE + " DirContext='" + readWriteContext + "'");
			}

			return makeFailureAwareProxy(readWriteContext);
		}
		else if (contextType == DirContextType.READ_ONLY) {

			final DirContext readOnlyContext = this.contextSource.getReadOnlyContext();

			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Created new " + DirContextType.READ_ONLY + " DirContext='" + readOnlyContext + "'");
			}

			return makeFailureAwareProxy(readOnlyContext);
		}
		else {
			throw new IllegalArgumentException("Unrecognized ContextType: " + contextType);
		}
	}

	/**
	 * @see BaseKeyedPooledObjectFactory#wrap(Object)
	 *
	 */
	@Override
	public PooledObject<Object> wrap(Object value) {
		return new DefaultPooledObject<Object>(value);
	}

	/**
	 * Invocation handler that checks thrown exceptions against the configured
	 * {@link #nonTransientExceptions}, marking the Context as invalid on match.
	 *
	 * @author Mattias Hellborg Arthursson
	 * @since 2.0
	 */
	private class FailureAwareContextProxy implements InvocationHandler {

		private DirContext target;

		private boolean hasFailed = false;

		public FailureAwareContextProxy(DirContext target) {
			Assert.notNull(target, "Target must not be null");
			this.target = target;
		}

		/*
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 * java.lang.reflect.Method, java.lang.Object[])
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			String methodName = method.getName();
			if (methodName.equals("getTargetContext")) {
				return this.target;
			}
			else if (methodName.equals("hasFailed")) {
				return this.hasFailed;
			}

			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				Class<? extends Throwable> targetExceptionClass = targetException.getClass();

				boolean nonTransientEncountered = false;
				for (Class<? extends Throwable> clazz : DirContextPooledObjectFactory.this.nonTransientExceptions) {
					if (clazz.isAssignableFrom(targetExceptionClass)) {
						if (DirContextPooledObjectFactory.this.logger.isDebugEnabled()) {
							DirContextPooledObjectFactory.this.logger.debug(String.format(
									"A %s - explicitly configured to be a non-transient exception - encountered; eagerly invalidating the target context.",
									targetExceptionClass));
						}
						nonTransientEncountered = true;
						break;
					}
				}

				if (nonTransientEncountered) {
					this.hasFailed = true;
				}
				else {
					if (DirContextPooledObjectFactory.this.logger.isDebugEnabled()) {
						DirContextPooledObjectFactory.this.logger.debug(String.format(
								"A %s - not explicitly configured to be a non-transient exception - encountered; ignoring.",
								targetExceptionClass));
					}
				}

				throw targetException;
			}
		}

	}

}
