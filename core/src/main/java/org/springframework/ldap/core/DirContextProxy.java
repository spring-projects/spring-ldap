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

package org.springframework.ldap.core;

import javax.naming.directory.DirContext;

/**
 * Helper interface to be able to get hold of the target <code>DirContext</code> from
 * proxies created by <code>ContextSource</code> proxies.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public interface DirContextProxy {

	/**
	 * Get the target <code>DirContext</code> of the proxy.
	 * @return the target <code>DirContext</code>.
	 */
	DirContext getTargetContext();

}
