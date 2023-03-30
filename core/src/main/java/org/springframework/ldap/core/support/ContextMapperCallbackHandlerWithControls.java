/*
 * Copyright 2005-2013 the original author or authors.
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

import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.ObjectRetrievalException;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.ldap.HasControls;

/**
 * Currently only per request controls can be inspected via the post process method on a
 * context processor. If a request control gives a different value for each search result,
 * then this cannot be inspected using the existing support classes. An example control
 * that requires this feature would be 1.3.6.1.4.1.42.2.27.9.5.8 Account usability
 * control, that can be used with for example the Sun ONE or the OpenDS directory servers.
 *
 * The extended callback handler can pass hasControls to mapper.
 *
 * @author Tim Terry
 * @author Ulrik Sandberg
 */
public class ContextMapperCallbackHandlerWithControls<T> extends ContextMapperCallbackHandler<T> {

	private ContextMapperWithControls<T> mapper = null;

	public ContextMapperCallbackHandlerWithControls(final ContextMapperWithControls<T> mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	/*
	 * @see org.springframework.ldap.core.ContextMapperCallbackHandler#
	 * getObjectFromNameClassPair(javax.naming.NameClassPair)
	 */
	public T getObjectFromNameClassPair(final NameClassPair nameClassPair) throws NamingException {
		if (!(nameClassPair instanceof Binding)) {
			throw new IllegalArgumentException("Parameter must be an instance of Binding");
		}

		Binding binding = (Binding) nameClassPair;
		Object object = binding.getObject();
		if (object == null) {
			throw new ObjectRetrievalException("Binding did not contain any object.");
		}
		T result;
		if (nameClassPair instanceof HasControls) {
			result = this.mapper.mapFromContextWithControls(object, (HasControls) nameClassPair);
		}
		else {
			result = this.mapper.mapFromContext(object);
		}
		return result;
	}

}
