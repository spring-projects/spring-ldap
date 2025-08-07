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

package org.springframework.ldap.pool2.validation;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool2.DirContextType;

/**
 * A validator for {@link DirContext}s.
 *
 * @author Eric Dalquist
 */
public interface DirContextValidator {

	/**
	 * Validates the {@link DirContext}. A valid {@link DirContext} should be able to
	 * answer queries and if applicable write to the directory.
	 * @param contextType The type of the {@link DirContext}, refers to if
	 * {@link ContextSource#getReadOnlyContext()} or
	 * {@link ContextSource#getReadWriteContext()} was called to create the
	 * {@link DirContext}
	 * @param dirContext The {@link DirContext} to validate.
	 * @return <code>true</code> if the {@link DirContext} operated correctly during
	 * validation.
	 */
	boolean validateDirContext(DirContextType contextType, DirContext dirContext);

}
