/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ldap.odm.typeconversion.impl.converters;

import java.lang.reflect.Constructor;

import org.springframework.ldap.odm.typeconversion.impl.Converter;

/**
 * A Converter from a {@link java.lang.String} to any class which has a single argument
 * public constructor taking a {@link java.lang.String}.
 * <p>
 * This should only be used as a fall-back converter, as a last attempt.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @deprecated please use {@link org.springframework.core.convert.converter.Converter}
 * directly
 */
@Deprecated
public final class FromStringConverter implements Converter {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.odm.typeconversion.impl.Converter#convert(java.lang.
	 * Object, java.lang.Class)
	 */
	public <T> T convert(Object source, Class<T> toClass) throws Exception {
		Constructor<T> constructor = toClass.getConstructor(java.lang.String.class);
		return constructor.newInstance(source);
	}

}
