/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.samples.utils;

import org.springframework.ldap.core.DirContextOperations;

import java.util.LinkedList;
import java.util.List;

public class LdapTree {
	private final DirContextOperations node;

	private List<LdapTree> subContexts = new LinkedList<LdapTree>();

	public LdapTree(DirContextOperations node) {
		this.node = node;
	}

	public DirContextOperations getNode() {
		return node;
	}

	public List<LdapTree> getSubContexts() {
		return subContexts;
	}
	
	public void setSubContexts(List<LdapTree> subContexts) {
		this.subContexts = subContexts;
	}

	public void addSubTree(LdapTree ldapTree) {
		subContexts.add(ldapTree);
	}

	
	
	public void traverse(LdapTreeVisitor visitor) {
		traverse(visitor, 0);
	}

	private void traverse(LdapTreeVisitor visitor, int currentDepth) {
		visitor.visit(node, currentDepth);
		for (LdapTree subContext : subContexts) {
			subContext.traverse(visitor, currentDepth + 1);
		}
	}
}
