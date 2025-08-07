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

package org.springframework.ldap.odm.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a Java class to be persisted in an LDAP directory.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entry {

	/**
	 * A list of LDAP object classes that the annotated Java class represents.
	 * <p>
	 * All fields will be persisted to LDAP unless annotated {@link Transient}.
	 * @return A list of LDAP classes which the annotated Java class represents.
	 */
	String[] objectClasses();

	/**
	 * The base DN of this entry. If specified, this will be prepended to all calculated
	 * distinguished names for entries of the annotated class.
	 * @return the base DN for entries of this class
	 */
	String base() default "";

}
