/*
 * Copyright 2005-2023 the original author or authors.
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
 * Maps a Java field to an LDAP attribute.
 * <p>
 * The containing class must be annotated with {@link Entry}.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @see Entry
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
	/**
	 * The Type attribute indicates whether a field is regarded as binary based 
	 * or string based by the LDAP JNDI provider.
	 */
	enum Type {
		/**
		 * A string field - returned by the JNDI LDAP provider as a {@link java.lang.String}.
		 */
		STRING, /**
		 * A binary field - returned by the JNDI LDAP provider as a <code>byte[]</code>.
		 */
		BINARY
	}

	/**
	 * The LDAP attribute name that this field represents.
	 * <p>
	 * Defaults to "" in which case the Java field name is used as the LDAP attribute name.
	 * 
	 * @return The LDAP attribute name.
	 * 
	 */
	String name() default "";

	/**
	 * Indicates whether this field is returned by the LDAP JNDI provider as a
	 * <code>String</code> (<code>Type.STRING</code>) or as a
	 * <code>byte[]</code> (<code>Type.BINARY</code>).
	 * 
	 * @return Either <code>Type.STRING</code> to indicate a string attribute 
	 * or <code>Type.BINARY</code> to indicate a binary attribute.
	 */
	Type type() default Type.STRING;

	/**
	 * The LDAP syntax of the attribute that this field represents.
	 * <p>
	 * This optional value is typically used to affect the precision of conversion 
	 * of values between LDAP and Java, 
	 * see {@link org.springframework.ldap.odm.typeconversion.ConverterManager} 
	 * and {@link org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl}.
	 * 
	 * @return The LDAP syntax of this attribute.
	 */
	String syntax() default "";

	/**
	 * A boolean parameter to indicate if the attribute should be read only.
	 * <p>
	 * This value allows attributes to be read on read, but not persisted, there
	 * are many operational and read-only ldap attributes which will throw errors
	 * if they are persisted back to ldap.
	 *
	 * @return {@code true} is the attribute should not be written to ldap.
	 */
	boolean readonly() default false;

}
