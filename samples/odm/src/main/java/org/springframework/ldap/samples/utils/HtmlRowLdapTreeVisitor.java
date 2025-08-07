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

package org.springframework.ldap.samples.utils;

import java.util.LinkedList;
import java.util.List;

import org.springframework.ldap.core.DirContextOperations;

public class HtmlRowLdapTreeVisitor implements LdapTreeVisitor {

	private List<String> rows = new LinkedList<String>();

	public void visit(DirContextOperations node, int currentDepth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < currentDepth; i++) {
			sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		}

		sb.append("<a href='").append(getLinkForNode(node)).append("'>").append(node.getDn()).append("</a>")
				.append("<br>\n");

		rows.add(sb.toString());
	}

	protected String getLinkForNode(DirContextOperations node) {
		return "#";
	}

	public List<String> getRows() {
		return rows;
	}

}
