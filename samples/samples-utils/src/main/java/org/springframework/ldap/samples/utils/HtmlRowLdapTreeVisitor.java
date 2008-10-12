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
