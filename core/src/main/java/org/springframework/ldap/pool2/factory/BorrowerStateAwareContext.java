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

package org.springframework.ldap.pool2.factory;

import java.util.Set;

/**
 * Exposes borrower changes on the pooled proxy after delegating wrappers are removed.
 */
interface BorrowerStateAwareContext {

	/**
	 * Return an unmodifiable snapshot of the state kinds changed during this borrow.
	 * @return the recorded changes, or an empty set if none remain
	 */
	Set<ContextState> getChangedState();

	/**
	 * Remove a change record after its underlying state has been successfully reset.
	 * Other change records remain intact. A failed reset must leave its record intact.
	 * @param state the successfully reset state kind
	 */
	void clearChangedState(ContextState state);

}
