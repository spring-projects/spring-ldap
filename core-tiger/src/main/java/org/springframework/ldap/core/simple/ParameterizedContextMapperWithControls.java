/*
 * Copyright 2005-2008 the original author or authors.
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

import javax.naming.ldap.HasControls;

/**
 * Extension of the {@link ParameterizedContextMapper} interface that allows
 * controls to be passed to the mapper implementation. Uses Java 5 covariant
 * return types to override the return type of the
 * {@link #mapFromContextWithControls(Object, HasControls)} method to be the
 * type parameter T.
 * 
 * @author Tim Terry
 * @author Ulrik Sandberg
 * @param <T> return type of the
 * {@link #mapFromContextWithControls(Object, HasControls)} method
 */
public interface ParameterizedContextMapperWithControls<T> extends ParameterizedContextMapper<T> {

	T mapFromContextWithControls(final Object ctx, final HasControls hasControls);
}
