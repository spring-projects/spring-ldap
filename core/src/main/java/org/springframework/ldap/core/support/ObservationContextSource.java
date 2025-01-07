/*
 * Copyright 2005-2025 the original author or authors.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * An implementation of {@link ContextSource} that returns {@link DirContext} instances
 * that record the time each operation takes.
 *
 * @author Josh Cummings
 * @since 3.3
 */
public final class ObservationContextSource implements BaseLdapPathContextSource {

	private final BaseLdapPathContextSource contextSource;

	private final DirContextOperationObservationContext.Builder builder;

	private final ObservationRegistry registry;

	public ObservationContextSource(BaseLdapPathContextSource contextSource, ObservationRegistry registry) {
		this.contextSource = contextSource;
		this.registry = registry;
		this.builder = DirContextOperationObservationContext.withContextSource(this.contextSource);
	}

	@Override
	public DirContext getReadOnlyContext() throws NamingException {
		return wrapDirContext(this.contextSource.getReadOnlyContext());
	}

	@Override
	public DirContext getReadWriteContext() throws NamingException {
		return wrapDirContext(this.contextSource.getReadWriteContext());
	}

	@Override
	public DirContext getContext(String principal, String credentials) throws NamingException {
		return wrapDirContext(this.contextSource.getContext(principal, credentials));
	}

	private DirContext wrapDirContext(DirContext delegate) {
		if (delegate instanceof DirContextOperations operations) {
			return operations;
		}
		if (delegate instanceof LdapContext ldap) {
			return new ObservationLdapContext(this.builder, ldap, this.registry);
		}
		return new ObservationDirContext(this.builder, delegate, this.registry);
	}

	@Deprecated
	@Override
	public DistinguishedName getBaseLdapPath() {
		return this.contextSource.getBaseLdapPath();
	}

	@Override
	public LdapName getBaseLdapName() {
		return this.contextSource.getBaseLdapName();
	}

	@Override
	public String getBaseLdapPathAsString() {
		return this.contextSource.getBaseLdapPathAsString();
	}

	private static class DirContextOperationObservationContext extends Observation.Context {

		private final String urls;

		private final String base;

		private final String operation;

		DirContextOperationObservationContext(String urls, String base, String operation) {
			this.urls = urls;
			this.base = base;
			this.operation = operation;
		}

		static Builder withContextSource(BaseLdapPathContextSource contextSource) {
			return new Builder(contextSource);
		}

		String getUrls() {
			return this.urls;
		}

		String getBase() {
			return this.base;
		}

		String getOperation() {
			return this.operation;
		}

		private static class Builder {

			private final String urls;

			private final String base;

			Builder(BaseLdapPathContextSource contextSource) {
				this.base = contextSource.getBaseLdapPathAsString();
				if (contextSource instanceof AbstractContextSource acs) {
					this.urls = Arrays.toString(acs.getUrls());
				}
				else {
					this.urls = "unknown";
				}
			}

			DirContextOperationObservationContext operation(String operation) {
				return new DirContextOperationObservationContext(this.urls, this.base, operation);
			}

		}

	}

	private static class DirContextOperationObservationConvention
			implements ObservationConvention<DirContextOperationObservationContext> {

		static final String OBSERVATION_NAME = "spring.ldap.dir.context.operations";

		@Override
		public KeyValues getLowCardinalityKeyValues(DirContextOperationObservationContext context) {
			return KeyValues.of("urls", context.getUrls())
				.and("base", context.getBase())
				.and("operation", context.getOperation());
		}

		@Override
		public boolean supportsContext(io.micrometer.observation.Observation.Context context) {
			return context instanceof DirContextOperationObservationContext;
		}

		@Override
		public String getName() {
			return OBSERVATION_NAME;
		}

		@Override
		public String getContextualName(DirContextOperationObservationContext context) {
			return "perform " + context.getOperation();
		}

	}

	private static class ObservationLdapContext extends ObservationDirContext implements LdapContext {

		private final LdapContext delegate;

		ObservationLdapContext(DirContextOperationObservationContext.Builder builder, LdapContext delegate,
				ObservationRegistry registry) {
			super(builder, delegate, registry);
			this.delegate = delegate;
		}

		@Override
		public ExtendedResponse extendedOperation(ExtendedRequest request) throws javax.naming.NamingException {
			Observation observation = observation("extended.operation");
			observation.highCardinalityKeyValue("request.id", request.getID());
			return observe(observation, () -> this.delegate.extendedOperation(request));
		}

		@Override
		public LdapContext newInstance(Control[] requestControls) throws javax.naming.NamingException {
			return this.delegate.newInstance(requestControls);
		}

		@Override
		public void reconnect(Control[] connCtls) throws javax.naming.NamingException {
			Observation observation = observation("reconnect");
			List<String> ids = new ArrayList<>();
			for (Control control : connCtls) {
				ids.add(control.getID());
			}
			observation.highCardinalityKeyValue("control.ids", ids.toString());
			observe(observation, () -> this.delegate.reconnect(connCtls));
		}

		@Override
		public Control[] getConnectControls() throws javax.naming.NamingException {
			return this.delegate.getConnectControls();
		}

		@Override
		public void setRequestControls(Control[] requestControls) throws javax.naming.NamingException {
			this.delegate.setRequestControls(requestControls);
		}

		@Override
		public Control[] getRequestControls() throws javax.naming.NamingException {
			return this.delegate.getRequestControls();
		}

		@Override
		public Control[] getResponseControls() throws javax.naming.NamingException {
			return this.delegate.getResponseControls();
		}

	}

	private static class ObservationDirContext implements DirContext, DirContextProxy {

		private final ObservationRegistry registry;

		private final DirContextOperationObservationConvention convention = new DirContextOperationObservationConvention();

		private final DirContextOperationObservationContext.Builder builder;

		private final DirContext delegate;

		ObservationDirContext(DirContextOperationObservationContext.Builder builder, DirContext delegate,
				ObservationRegistry registry) {
			this.builder = builder;
			this.delegate = delegate;
			this.registry = registry;
		}

		private static KeyValue name(String tagName, Name name) {
			return KeyValue.of(tagName, String.valueOf(name));
		}

		private static KeyValue name(String tagName, String name) {
			return KeyValue.of(tagName, name);
		}

		private static KeyValue name(Name name) {
			return name("name", name);
		}

		private static KeyValue name(String name) {
			return name("name", name);
		}

		private static KeyValue attributeIds(String[] attributes) {
			return KeyValue.of("attribute.ids", Arrays.toString(attributes));
		}

		private static KeyValue attributeIds(Attributes attributes) {
			if (attributes == null) {
				return KeyValue.of("attribute.ids", Collections.emptyList().toString());
			}
			else {
				return KeyValue.of("attribute.ids", Collections.list(attributes.getIDs()).toString());
			}
		}

		private static KeyValue attributeIds(int mod_op, Attributes attrs) {
			List<String> attributes = Collections.list(attrs.getIDs());
			String value = Collections.singletonMap(modify(mod_op), attributes).toString();
			return KeyValue.of("attribute.ids", value);
		}

		private static KeyValue attributeIds(ModificationItem[] items) {
			MultiValueMap<String, String> ids = new LinkedMultiValueMap<>();
			for (ModificationItem item : items) {
				ids.add(modify(item.getModificationOp()), item.getAttribute().getID());
			}
			String value = ids.toString();
			return KeyValue.of("attribute.ids", value);
		}

		private static KeyValue attributeIdsReturn(String[] attrs) {
			return KeyValue.of("attribute.ids", Arrays.toString(attrs));
		}

		private static KeyValue attributeIdsReturn(SearchControls searchControls) {
			return attributeIdsReturn(searchControls.getReturningAttributes());
		}

		private static KeyValue searchControls(SearchControls searchControls) {
			if (searchControls == null) {
				return KeyValue.of("search.controls", "none");
			}
			return KeyValue.of("search.controls", Objects.toString(searchControls));
		}

		private static String modify(int mod_op) {
			return switch (mod_op) {
				case DirContext.ADD_ATTRIBUTE -> "add";
				case DirContext.REPLACE_ATTRIBUTE -> "replace";
				case DirContext.REMOVE_ATTRIBUTE -> "remove";
				default -> throw new IllegalArgumentException("Unsupported operation: " + mod_op);
			};
		}

		@Override
		public DirContext getTargetContext() {
			return this.delegate;
		}

		@Override
		public Attributes getAttributes(Name name) throws javax.naming.NamingException {
			DirContextOperationObservationContext context = this.builder.operation("get.attributes");
			io.micrometer.observation.Observation observation = io.micrometer.observation.Observation
				.createNotStarted(this.convention, () -> context, this.registry)
				.highCardinalityKeyValue("name", name.toString());
			return observe(observation, () -> this.delegate.getAttributes(name));
		}

		@Override
		public Attributes getAttributes(String name) throws javax.naming.NamingException {
			Observation observation = observation("get.attributes").highCardinalityKeyValue("name", name);
			return observe(observation, () -> this.delegate.getAttributes(name));
		}

		Observation observation(String operation) {
			DirContextOperationObservationContext context = this.builder.operation(operation);
			return io.micrometer.observation.Observation.createNotStarted(this.convention, () -> context,
					this.registry);
		}

		@Override
		public Attributes getAttributes(Name name, String[] attrIds) throws javax.naming.NamingException {
			Observation observation = observation("get.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrIds));
			return observe(observation, () -> this.delegate.getAttributes(name, attrIds));
		}

		@Override
		public Attributes getAttributes(String name, String[] attrIds) throws javax.naming.NamingException {
			Observation observation = observation("get.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrIds));
			return observe(observation, () -> this.delegate.getAttributes(name, attrIds));
		}

		@Override
		public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("modify.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mod_op, attrs));
			observe(observation, () -> this.delegate.modifyAttributes(name, mod_op, attrs));
		}

		@Override
		public void modifyAttributes(String name, int mod_op, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("modify.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mod_op, attrs));
			observe(observation, () -> this.delegate.modifyAttributes(name, mod_op, attrs));
		}

		@Override
		public void modifyAttributes(Name name, ModificationItem[] mods) throws javax.naming.NamingException {
			Observation observation = observation("modify.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mods));
			observe(observation, () -> this.delegate.modifyAttributes(name, mods));
		}

		@Override
		public void modifyAttributes(String name, ModificationItem[] mods) throws javax.naming.NamingException {
			Observation observation = observation("modify.attributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mods));
			observe(observation, () -> this.delegate.modifyAttributes(name, mods));
		}

		@Override
		public void bind(Name name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observe(observation, () -> this.delegate.bind(name, obj, attrs));
		}

		@Override
		public void bind(String name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observe(observation, () -> this.delegate.bind(name, obj, attrs));
		}

		@Override
		public void rebind(Name name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observe(observation, () -> this.delegate.rebind(name, obj, attrs));
		}

		@Override
		public void rebind(String name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observe(observation, () -> this.delegate.rebind(name, obj, attrs));
		}

		@Override
		public DirContext createSubcontext(Name name, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("create.subcontext").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			return observe(observation, () -> this.delegate.createSubcontext(name, attrs));
		}

		@Override
		public DirContext createSubcontext(String name, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("create.subcontext").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			return observe(observation, () -> this.delegate.createSubcontext(name, attrs));
		}

		@Override
		public DirContext getSchema(Name name) throws javax.naming.NamingException {
			Observation observation = observation("get.schema").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.getSchema(name));
		}

		@Override
		public DirContext getSchema(String name) throws javax.naming.NamingException {
			Observation observation = observation("get.schema").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.getSchema(name));
		}

		@Override
		public DirContext getSchemaClassDefinition(Name name) throws javax.naming.NamingException {
			Observation observation = observation("get.schema.class.definition").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.getSchemaClassDefinition(name));
		}

		@Override
		public DirContext getSchemaClassDefinition(String name) throws javax.naming.NamingException {
			Observation observation = observation("get.schema.class.definition").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.getSchemaClassDefinition(name));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes,
				String[] attributesToReturn) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(attributesToReturn));
			return observe(observation, () -> this.delegate.search(name, matchingAttributes, attributesToReturn));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
				String[] attributesToReturn) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes))
				.highCardinalityKeyValue(attributeIdsReturn(attributesToReturn));
			return observe(observation, () -> this.delegate.search(name, matchingAttributes, attributesToReturn));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes));
			return observe(observation, () -> this.delegate.search(name, matchingAttributes));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes));
			return observe(observation, () -> this.delegate.search(name, matchingAttributes));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observe(observation, () -> this.delegate.search(name, filter, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observe(observation, () -> this.delegate.search(name, filter, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
				SearchControls cons) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observe(observation, () -> this.delegate.search(name, filterExpr, filterArgs, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
				SearchControls cons) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observe(observation, () -> this.delegate.search(name, filterExpr, filterArgs, cons));
		}

		@Override
		public Object lookup(Name name) throws javax.naming.NamingException {
			Observation observation = observation("lookup").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.lookup(name));
		}

		@Override
		public Object lookup(String name) throws javax.naming.NamingException {
			Observation observation = observation("lookup").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.lookup(name));
		}

		@Override
		public void bind(Name name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.bind(name, obj));
		}

		@Override
		public void bind(String name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.bind(name, obj));
		}

		@Override
		public void rebind(Name name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.rebind(name, obj));
		}

		@Override
		public void rebind(String name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.rebind(name, obj));
		}

		@Override
		public void unbind(Name name) throws javax.naming.NamingException {
			Observation observation = observation("unbind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.unbind(name));
		}

		@Override
		public void unbind(String name) throws javax.naming.NamingException {
			Observation observation = observation("unbind").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.unbind(name));
		}

		@Override
		public void rename(Name oldName, Name newName) throws javax.naming.NamingException {
			Observation observation = observation("rename").highCardinalityKeyValue(name("old.name", oldName))
				.highCardinalityKeyValue(name("new.name", newName));
			observe(observation, () -> this.delegate.rename(oldName, newName));
		}

		@Override
		public void rename(String oldName, String newName) throws javax.naming.NamingException {
			Observation observation = observation("rename").highCardinalityKeyValue(name("old.name", oldName))
				.highCardinalityKeyValue(name("new.name", newName));
			observe(observation, () -> this.delegate.rename(oldName, newName));
		}

		@Override
		public NamingEnumeration<NameClassPair> list(Name name) throws javax.naming.NamingException {
			Observation observation = observation("list").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.list(name));
		}

		@Override
		public NamingEnumeration<NameClassPair> list(String name) throws javax.naming.NamingException {
			Observation observation = observation("list").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.list(name));
		}

		@Override
		public NamingEnumeration<Binding> listBindings(Name name) throws javax.naming.NamingException {
			Observation observation = observation("list.bindings").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.listBindings(name));
		}

		@Override
		public NamingEnumeration<Binding> listBindings(String name) throws javax.naming.NamingException {
			Observation observation = observation("list.bindings").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.listBindings(name));
		}

		@Override
		public void destroySubcontext(Name name) throws javax.naming.NamingException {
			Observation observation = observation("destroy.subcontext").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.destroySubcontext(name));
		}

		@Override
		public void destroySubcontext(String name) throws javax.naming.NamingException {
			Observation observation = observation("destroy.subcontext").highCardinalityKeyValue(name(name));
			observe(observation, () -> this.delegate.destroySubcontext(name));
		}

		@Override
		public Context createSubcontext(Name name) throws javax.naming.NamingException {
			Observation observation = observation("create.subcontext").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.createSubcontext(name));
		}

		@Override
		public Context createSubcontext(String name) throws javax.naming.NamingException {
			Observation observation = observation("create.subcontext").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.createSubcontext(name));
		}

		@Override
		public Object lookupLink(Name name) throws javax.naming.NamingException {
			Observation observation = observation("lookup.link").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.lookupLink(name));
		}

		@Override
		public Object lookupLink(String name) throws javax.naming.NamingException {
			Observation observation = observation("lookup.link").highCardinalityKeyValue(name(name));
			return observe(observation, () -> this.delegate.lookupLink(name));
		}

		@Override
		public NameParser getNameParser(Name name) throws javax.naming.NamingException {
			return this.delegate.getNameParser(name);
		}

		@Override
		public NameParser getNameParser(String name) throws javax.naming.NamingException {
			return this.delegate.getNameParser(name);
		}

		@Override
		public Name composeName(Name name, Name prefix) throws javax.naming.NamingException {
			return this.delegate.composeName(name, prefix);
		}

		@Override
		public String composeName(String name, String prefix) throws javax.naming.NamingException {
			return this.delegate.composeName(name, prefix);
		}

		@Override
		public Object addToEnvironment(String propName, Object propVal) throws javax.naming.NamingException {
			return this.delegate.addToEnvironment(propName, propVal);
		}

		@Override
		public Object removeFromEnvironment(String propName) throws javax.naming.NamingException {
			return this.removeFromEnvironment(propName);
		}

		@Override
		public Hashtable<?, ?> getEnvironment() throws javax.naming.NamingException {
			return this.delegate.getEnvironment();
		}

		@Override
		public void close() throws javax.naming.NamingException {
			this.delegate.close();
		}

		@Override
		public String getNameInNamespace() throws javax.naming.NamingException {
			Observation observation = observation("get.name.in.namespace");
			return observe(observation, () -> this.delegate.getNameInNamespace());
		}

		<T> T observe(Observation observation, ThrowableSupplier<T> supplier) throws javax.naming.NamingException {
			observation.start();
			try (io.micrometer.observation.Observation.Scope scope = observation.openScope()) {
				return supplier.get();
			}
			catch (Throwable ex) {
				observation.error(ex);
				throw ex;
			}
			finally {
				observation.stop();
			}
		}

		void observe(Observation observation, ThrowableRunnable runnable) throws javax.naming.NamingException {
			observation.start();
			try (io.micrometer.observation.Observation.Scope scope = observation.openScope()) {
				runnable.run();
			}
			catch (Throwable ex) {
				observation.error(ex);
				throw ex;
			}
			finally {
				observation.stop();
			}
		}

		interface ThrowableSupplier<T> {

			T get() throws javax.naming.NamingException;

		}

		interface ThrowableRunnable {

			void run() throws javax.naming.NamingException;

		}

	}

}
