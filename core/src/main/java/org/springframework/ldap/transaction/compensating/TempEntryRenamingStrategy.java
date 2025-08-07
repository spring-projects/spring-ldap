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

package org.springframework.ldap.transaction.compensating;

import javax.naming.Name;

/**
 * Interface for different strategies to rename temporary entries for unbind and rebind
 * operations.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public interface TempEntryRenamingStrategy {

	/**
	 * Get a temporary name for the current entry to be renamed to.
	 * @param originalName The original name of the entry.
	 * @return The name to which the entry should be temporarily renamed according to this
	 * strategy.
	 */
	Name getTemporaryName(Name originalName);

}
