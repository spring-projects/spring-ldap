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

package org.springframework.ldap.core.support;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.DirContextProcessor;

/**
 * Manages a sequence of {@link DirContextProcessor} instances. Applies
 * {@link #preProcess(DirContext)} and {@link #postProcess(DirContext)} respectively in
 * sequence on the managed objects.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class AggregateDirContextProcessor implements DirContextProcessor {

	private List<DirContextProcessor> dirContextProcessors = new LinkedList<DirContextProcessor>();

	/**
	 * Add the supplied DirContextProcessor to the list of managed objects.
	 * @param processor the DirContextpProcessor to add.
	 */
	public void addDirContextProcessor(DirContextProcessor processor) {
		this.dirContextProcessors.add(processor);
	}

	/**
	 * Get the list of managed {@link DirContextProcessor} instances.
	 * @return the managed list of {@link DirContextProcessor} instances.
	 */
	public List<DirContextProcessor> getDirContextProcessors() {
		return this.dirContextProcessors;
	}

	/**
	 * Set the list of managed {@link DirContextProcessor} instances.
	 * @param dirContextProcessors the list of {@link DirContextProcessor} instances to
	 * set.
	 */
	public void setDirContextProcessors(List<DirContextProcessor> dirContextProcessors) {
		this.dirContextProcessors = new ArrayList<DirContextProcessor>(dirContextProcessors);
	}

	/*
	 * @see
	 * org.springframework.ldap.core.DirContextProcessor#preProcess(javax.naming.directory
	 * .DirContext)
	 */
	public void preProcess(DirContext ctx) throws NamingException {
		for (DirContextProcessor processor : this.dirContextProcessors) {
			processor.preProcess(ctx);
		}
	}

	/*
	 * @see org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming.
	 * directory.DirContext)
	 */
	public void postProcess(DirContext ctx) throws NamingException {
		for (DirContextProcessor processor : this.dirContextProcessors) {
			processor.postProcess(ctx);
		}
	}

}
