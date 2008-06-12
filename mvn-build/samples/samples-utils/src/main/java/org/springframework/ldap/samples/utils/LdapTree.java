package org.springframework.ldap.samples.utils;

import java.util.LinkedList;
import java.util.List;

import org.springframework.ldap.core.DirContextOperations;

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
