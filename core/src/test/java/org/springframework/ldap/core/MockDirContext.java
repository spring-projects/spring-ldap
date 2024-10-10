/*
 * Copyright 2002-2024 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.CollectionUtils;

class MockDirContext implements DirContext {

	private boolean closed;

	private Map<String, Map<String, List<Object>>> entries = new HashMap<>();

	private Map<String, Object> objects = new HashMap<>();

	private Map<String, List<Object>> require(Name name) throws NamingException {
		return require(name.toString());
	}

	private Map<String, List<Object>> require(String name) throws NamingException {
		if (!this.entries.containsKey(name)) {
			throw new NameNotFoundException("name not found");
		}
		return this.entries.get(name);
	}

	private NameAwareAttributes toAttributes(Map<String, List<Object>> map) {
		NameAwareAttributes attributes = new NameAwareAttributes();
		for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
			NameAwareAttribute attribute = new NameAwareAttribute(entry.getKey(), true);
			entry.getValue().forEach(attribute::add);
			attributes.put(attribute);
		}
		return attributes;
	}

	private Map<String, List<Object>> toEntry(NameAwareAttributes attributes) {
		Map<String, List<Object>> map = new HashMap<>();
		Iterator<NameAwareAttribute> iterator = CollectionUtils.toIterator(attributes.getAll());
		while (iterator.hasNext()) {
			NameAwareAttribute attribute = iterator.next();
			List<Object> values = new ArrayList<>();
			CollectionUtils.toIterator(attribute.getAll()).forEachRemaining(values::add);
			map.put(attribute.getID(), values);
		}
		return map;
	}

	@Override
	public Attributes getAttributes(Name name) throws NamingException {
		return toAttributes(require(name));
	}

	@Override
	public Attributes getAttributes(String name) throws NamingException {
		return toAttributes(require(name));
	}

	@Override
	public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
		Map<String, List<Object>> entry = require(name);
		NameAwareAttributes toReturn = new NameAwareAttributes();
		for (String attrId : attrIds) {
			entry.get(attrId);
			toReturn.put(attrId, entry.get(attrId));
		}
		return toReturn;
	}

	@Override
	public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
		return getAttributes(LdapUtils.newLdapName(name), attrIds);
	}

	@Override
	public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws NamingException {
		modifyAttributes(name.toString(), mod_op, attrs);
	}

	@Override
	public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
		Map<String, List<Object>> entry = require(name);
		Map<String, List<Object>> toModify = toEntry((NameAwareAttributes) attrs);
		if (mod_op == DirContext.ADD_ATTRIBUTE) {
			for (Map.Entry<String, List<Object>> attribute : toModify.entrySet()) {
				List<Object> toAdd = attribute.getValue();
				if (toAdd.isEmpty()) {
					throw new InvalidAttributeValueException("cannot add empty attribute");
				}
				entry.computeIfPresent(attribute.getKey(), (k, v) -> {
					v.addAll(toAdd);
					return v;
				});
				entry.computeIfAbsent(attribute.getKey(), (k) -> new ArrayList<>()).addAll(toAdd);
			}
			return;
		}
		if (mod_op == DirContext.REPLACE_ATTRIBUTE) {
			for (Map.Entry<String, List<Object>> attribute : toModify.entrySet()) {
				List<Object> toAdd = attribute.getValue();
				if (toAdd.isEmpty()) {
					entry.remove(attribute.getKey());
				}
				else {
					entry.put(attribute.getKey(), toAdd);
				}
			}
			return;
		}
		if (mod_op == DirContext.REMOVE_ATTRIBUTE) {
			for (Map.Entry<String, List<Object>> attribute : toModify.entrySet()) {
				List<Object> toRemove = attribute.getValue();
				entry.computeIfPresent(attribute.getKey(), (k, v) -> {
					v.removeAll(toRemove);
					return v;
				});
				if (entry.get(attribute.getKey()).isEmpty()) {
					entry.remove(attribute.getKey());
				}
			}
		}
	}

	@Override
	public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
		modifyAttributes(name.toString(), mods);
	}

	@Override
	public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
		for (ModificationItem item : mods) {
			NameAwareAttributes attributes = new NameAwareAttributes();
			attributes.put(item.getAttribute());
			modifyAttributes(name, item.getModificationOp(), attributes);
		}
	}

	@Override
	public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
		bind(name.toString(), obj, attrs);
	}

	@Override
	public void bind(String name, Object obj, Attributes attrs) throws NamingException {
		if (this.objects.containsKey(name)) {
			throw new NameAlreadyBoundException("name already bound");
		}
		if (obj != null) {
			this.objects.put(name, obj);
		}
		if (attrs.size() > 0) {
			Map<String, List<Object>> entry = toEntry((NameAwareAttributes) attrs);
			this.entries.put(name, entry);
			return;
		}
		if (obj instanceof DirContext ctx) {
			Map<String, List<Object>> entry = toEntry((NameAwareAttributes) ctx.getAttributes(name));
			this.entries.put(name, entry);
		}
	}

	@Override
	public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {

	}

	@Override
	public void rebind(String name, Object obj, Attributes attrs) throws NamingException {

	}

	@Override
	public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
		return null;
	}

	@Override
	public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
		return null;
	}

	@Override
	public DirContext getSchema(Name name) throws NamingException {
		return null;
	}

	@Override
	public DirContext getSchema(String name) throws NamingException {
		return null;
	}

	@Override
	public DirContext getSchemaClassDefinition(Name name) throws NamingException {
		return null;
	}

	@Override
	public DirContext getSchemaClassDefinition(String name) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
			String[] attributesToReturn) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
			throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
			throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		return null;
	}

	@Override
	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	@Override
	public Object lookup(String name) throws NamingException {
		return this.objects.get(name);
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		bind(name, obj, toAttributes(Map.of(name, Collections.emptyList())));
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		if (!this.objects.containsKey(name)) {
			throw new NameNotFoundException("name not found");
		}
		this.objects.put(name, obj);
		if (obj instanceof DirContext ctx) {
			this.entries.put(name, toEntry((NameAwareAttributes) ctx.getAttributes(name)));
		}
	}

	@Override
	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}

	@Override
	public void unbind(String name) throws NamingException {
		this.entries.remove(name);
		this.objects.remove(name);
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		if (this.entries.containsKey(newName)) {
			throw new NameAlreadyBoundException("name already bound");
		}
		if (!this.entries.containsKey(oldName)) {
			throw new NameNotFoundException("name not found");
		}
		Map<String, List<Object>> attributes = this.entries.remove(oldName);
		Object object = this.entries.remove(oldName);
		this.entries.put(newName, attributes);
		this.objects.put(newName, object);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		return null;
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		return null;
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {

	}

	@Override
	public void destroySubcontext(String name) throws NamingException {

	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		return null;
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		return null;
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		return null;
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		return null;
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		return null;
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		return null;
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		return null;
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException {
		return "";
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		return null;
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		return null;
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return null;
	}

	@Override
	public void close() throws NamingException {
		this.closed = true;
	}

	boolean isClosed() {
		return this.closed;
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		return "";
	}

}
