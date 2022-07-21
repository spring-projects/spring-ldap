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

package org.springframework.ldap.odm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field is to be automatically populated to/from the distinguished name
 * of an entry. Fields annotated with this annotation will be automatically populated with values from
 * the distinguished names of found entries. Annotated fields must be of type <code>String</code>.
 * <p>
 * For automatic calculation of the DN of an entry to work, the {@link #index()} value
 * must be specified on all DnAttribute annotations in that class, and these attribute values,
 * prepended with the {@link org.springframework.ldap.odm.annotations.Entry#base()} value will be used
 * to figure out the distinguished name of entries to create and update.
 * </p>
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DnAttribute {
	/**
	 * The name of the distinguished name attribute.
	 * @return the attribute name.
	 */
	String value();

	/**
	 * The index of this attribute in the calculated distinguished name of an entry.
	 * @return the 0-based index of this attribute.
	 */
	int index() default -1;
}
