/*
 * Copyright 2005-2009 the original author or authors.
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

import javax.naming.Binding;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.core.ContextMapper;

/**
 * Extension of the {@link ContextMapper} interface. Uses Java 5 covariant
 * return types to override the return type of the
 * {@link #mapFromContext(Object)} method to be the type parameter T.
 * 
 * @param <T>
 */
public interface ParameterizedContextMapper<T> extends ContextMapper {

	/**
	 * Map a single LDAP Context to an object. The supplied Object
	 * <code>ctx</code> is the object from a single {@link SearchResult},
	 * {@link Binding}, or a lookup operation.
	 * 
	 * @param ctx the context to map to an object.
	 * @return an object built from the data in the context.
	 */
	public T mapFromContext(Object ctx);
}
