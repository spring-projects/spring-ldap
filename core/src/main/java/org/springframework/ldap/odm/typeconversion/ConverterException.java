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

package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.NamingException;

/**
 * Thrown by the conversion framework to indicate an error condition - typically a failed
 * type conversion.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @deprecated please use {@link org.springframework.core.convert.ConversionException}
 */
@Deprecated
@SuppressWarnings("serial")
public final class ConverterException extends NamingException {

	public ConverterException(final String message) {
		super(message);
	}

	public ConverterException(final String message, final Throwable e) {
		super(message, e);
	}

}
