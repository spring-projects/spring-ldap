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

package org.springframework.ldap.convert;

import javax.naming.Name;

import org.springframework.core.convert.converter.Converter;

/**
 * A converer from {@link Name} to {@link String}.
 *
 * <p>
 * Helpful for {@link org.springframework.ldap.odm.core.ObjectDirectoryMapper} for
 * converting fields. Also helpful when working with {@link Name} instances in Spring MVC
 * applications.
 *
 * @author Josh Cummings
 * @since 3.3
 */
public final class NameToStringConverter implements Converter<Name, String> {

	@Override
	public String convert(Name source) {
		if (source == null) {
			return null;
		}

		return source.toString();
	}

}
