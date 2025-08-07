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

package org.springframework.ldap.core.support;

import javax.naming.NamingException;
import javax.naming.ldap.HasControls;

import org.springframework.ldap.core.ContextMapper;

/**
 * Extension of the {@link org.springframework.ldap.core.ContextMapper} interface that
 * allows controls to be passed to the mapper implementation. Uses Java 5 covariant return
 * types to override the return type of the
 * {@link #mapFromContextWithControls(Object, javax.naming.ldap.HasControls)} method to be
 * the type parameter T.
 *
 * @param <T> return type of the
 * {@link #mapFromContextWithControls(Object, javax.naming.ldap.HasControls)} method
 * @author Tim Terry
 * @author Ulrik Sandberg
 */
public interface ContextMapperWithControls<T> extends ContextMapper<T> {

	T mapFromContextWithControls(Object ctx, HasControls hasControls) throws NamingException;

}
