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
package org.springframework.ldap.core.simple;

import org.springframework.ldap.core.DirContextOperations;

/**
 * Abstract superclass that may be used instead of implementing
 * {@link ParameterizedContextMapper} directly. Subclassing from this superclass,
 * the supplied context will be automatically cast to
 * <code>DirContextOperations</code>. Note that if you use your own
 * <code>DirObjectFactory</code>, this implementation will fail with a
 * <code>ClassCastException</code>.
 * 
 * @author Mattias Arthursson
 * 
 */
public abstract class AbstractParameterizedContextMapper<T> implements ParameterizedContextMapper<T> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ldap.core.ContextMapper#mapFromContext(java.lang.Object)
	 */
	public final T mapFromContext(Object ctx) {
		return doMapFromContext((DirContextOperations) ctx);
	}

	/**
	 * Map a single <code>DirContextOperation</code> to an object. The
	 * supplied instance is the object supplied to
	 * {@link #mapFromContext(Object)} cast to a
	 * <code>DirContextOperations</code>.
	 * 
	 * @param ctx the context to map to an object.
	 * @return an object built from the data in the context.
	 */
	protected abstract T doMapFromContext(DirContextOperations ctx);

}
