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

import org.springframework.lang.NonNull;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.util.Assert;
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

	public ObservationContextSource(BaseLdapPathContextSource contextSource, ObservationRegistry observationRegistry) {
		Assert.notNull(contextSource, "contextSource cannot be null");
		Assert.notNull(observationRegistry, "observationRegistry cannot be null");
		Assert.isTrue(!(contextSource instanceof ObservationContextSource),
				"contextSource is already wrapped in an ObservationContextSource");
		this.contextSource = contextSource;
		this.registry = observationRegistry;
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
		if (delegate instanceof DirContextOperations) {
			return delegate;
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

		@NonNull
		@Override
		public KeyValues getLowCardinalityKeyValues(DirContextOperationObservationContext context) {
			return KeyValues.of("urls", context.getUrls())
				.and("base", context.getBase())
				.and("operation", context.getOperation());
		}

		@Override
		public boolean supportsContext(@NonNull Observation.Context context) {
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

	private interface DelegatingLdapContext extends LdapContext, DirContextProxy {

		default LdapContext newInstance(Control[] requestControls) throws javax.naming.NamingException {
			return ((LdapContext) getTargetContext()).newInstance(requestControls);
		}

		default Control[] getConnectControls() throws javax.naming.NamingException {
			return ((LdapContext) getTargetContext()).getConnectControls();
		}

		default void setRequestControls(Control[] requestControls) throws javax.naming.NamingException {
			((LdapContext) getTargetContext()).setRequestControls(requestControls);
		}

		default Control[] getRequestControls() throws javax.naming.NamingException {
			return ((LdapContext) getTargetContext()).getRequestControls();
		}

		default Control[] getResponseControls() throws javax.naming.NamingException {
			return ((LdapContext) getTargetContext()).getResponseControls();
		}

	}

	private static class ObservationLdapContext extends ObservationDirContext implements DelegatingLdapContext {

		private final LdapContext delegate;

		ObservationLdapContext(DirContextOperationObservationContext.Builder builder, LdapContext delegate,
				ObservationRegistry registry) {
			super(builder, delegate, registry);
			this.delegate = delegate;
		}

		@Override
		public ExtendedResponse extendedOperation(ExtendedRequest request) throws javax.naming.NamingException {
			Observation observation = observation("extendedOperation");
			observation.highCardinalityKeyValue("request.id", request.getID());
			return observation.observeChecked(() -> this.delegate.extendedOperation(request));
		}

		@Override
		public void reconnect(Control[] controls) throws javax.naming.NamingException {
			Observation observation = observation("reconnect");
			List<String> ids = new ArrayList<>();
			for (Control control : controls) {
				ids.add(control.getID());
			}
			observation.highCardinalityKeyValue("control.ids", ids.toString());
			observation.observeChecked(() -> this.delegate.reconnect(controls));
		}

	}

	private interface DelegatingDirContext extends DirContext, DirContextProxy {

		default NameParser getNameParser(Name name) throws javax.naming.NamingException {
			return getTargetContext().getNameParser(name);
		}

		default NameParser getNameParser(String name) throws javax.naming.NamingException {
			return getTargetContext().getNameParser(name);
		}

		default Name composeName(Name name, Name prefix) throws javax.naming.NamingException {
			return getTargetContext().composeName(name, prefix);
		}

		default String composeName(String name, String prefix) throws javax.naming.NamingException {
			return getTargetContext().composeName(name, prefix);
		}

		default Object addToEnvironment(String propName, Object propVal) throws javax.naming.NamingException {
			return getTargetContext().addToEnvironment(propName, propVal);
		}

		default Object removeFromEnvironment(String propName) throws javax.naming.NamingException {
			return getTargetContext().removeFromEnvironment(propName);
		}

		default Hashtable<?, ?> getEnvironment() throws javax.naming.NamingException {
			return getTargetContext().getEnvironment();
		}

		default void close() throws javax.naming.NamingException {
			getTargetContext().close();
		}

	}

	private static class ObservationDirContext extends AbstractDirContextProxy implements DelegatingDirContext {

		private final ObservationRegistry registry;

		private final DirContextOperationObservationConvention convention = new DirContextOperationObservationConvention();

		private final DirContextOperationObservationContext.Builder builder;

		ObservationDirContext(DirContextOperationObservationContext.Builder builder, DirContext delegate,
				ObservationRegistry registry) {
			super(delegate);
			this.builder = builder;
			this.registry = registry;
		}

		@Override
		public Attributes getAttributes(Name name) throws javax.naming.NamingException {
			Observation observation = observation("getAttributes").highCardinalityKeyValue("name", name.toString());
			return observation.observeChecked(() -> getTargetContext().getAttributes(name));
		}

		@Override
		public Attributes getAttributes(String name) throws javax.naming.NamingException {
			Observation observation = observation("getAttributes").highCardinalityKeyValue("name", name);
			return observation.observeChecked(() -> getTargetContext().getAttributes(name));
		}

		@Override
		public Attributes getAttributes(Name name, String[] attrIds) throws javax.naming.NamingException {
			Observation observation = observation("getAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrIds));
			return observation.observeChecked(() -> getTargetContext().getAttributes(name, attrIds));
		}

		@Override
		public Attributes getAttributes(String name, String[] attrIds) throws javax.naming.NamingException {
			Observation observation = observation("getAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrIds));
			return observation.observeChecked(() -> getTargetContext().getAttributes(name, attrIds));
		}

		@Override
		public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("modifyAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mod_op, attrs));
			observation.observeChecked(() -> getTargetContext().modifyAttributes(name, mod_op, attrs));
		}

		@Override
		public void modifyAttributes(String name, int mod_op, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("modifyAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mod_op, attrs));
			observation.observeChecked(() -> getTargetContext().modifyAttributes(name, mod_op, attrs));
		}

		@Override
		public void modifyAttributes(Name name, ModificationItem[] mods) throws javax.naming.NamingException {
			Observation observation = observation("modifyAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mods));
			observation.observeChecked(() -> getTargetContext().modifyAttributes(name, mods));
		}

		@Override
		public void modifyAttributes(String name, ModificationItem[] mods) throws javax.naming.NamingException {
			Observation observation = observation("modifyAttributes").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(mods));
			observation.observeChecked(() -> getTargetContext().modifyAttributes(name, mods));
		}

		@Override
		public void bind(Name name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observation.observeChecked(() -> getTargetContext().bind(name, obj, attrs));
		}

		@Override
		public void bind(String name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observation.observeChecked(() -> getTargetContext().bind(name, obj, attrs));
		}

		@Override
		public void rebind(Name name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observation.observeChecked(() -> getTargetContext().rebind(name, obj, attrs));
		}

		@Override
		public void rebind(String name, Object obj, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			observation.observeChecked(() -> getTargetContext().rebind(name, obj, attrs));
		}

		@Override
		public DirContext createSubcontext(Name name, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("createSubcontext").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			return observation.observeChecked(() -> getTargetContext().createSubcontext(name, attrs));
		}

		@Override
		public DirContext createSubcontext(String name, Attributes attrs) throws javax.naming.NamingException {
			Observation observation = observation("createSubcontext").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(attrs));
			return observation.observeChecked(() -> getTargetContext().createSubcontext(name, attrs));
		}

		@Override
		public DirContext getSchema(Name name) throws javax.naming.NamingException {
			Observation observation = observation("getSchema").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().getSchema(name));
		}

		@Override
		public DirContext getSchema(String name) throws javax.naming.NamingException {
			Observation observation = observation("getSchema").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().getSchema(name));
		}

		@Override
		public DirContext getSchemaClassDefinition(Name name) throws javax.naming.NamingException {
			Observation observation = observation("getSchemaClassDefinition").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().getSchemaClassDefinition(name));
		}

		@Override
		public DirContext getSchemaClassDefinition(String name) throws javax.naming.NamingException {
			Observation observation = observation("getSchemaClassDefinition").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().getSchemaClassDefinition(name));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes,
				String[] attributesToReturn) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(attributesToReturn));
			return observation
				.observeChecked(() -> getTargetContext().search(name, matchingAttributes, attributesToReturn));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
				String[] attributesToReturn) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes))
				.highCardinalityKeyValue(attributeIdsReturn(attributesToReturn));
			return observation
				.observeChecked(() -> getTargetContext().search(name, matchingAttributes, attributesToReturn));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes));
			return observation.observeChecked(() -> getTargetContext().search(name, matchingAttributes));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIds(matchingAttributes));
			return observation.observeChecked(() -> getTargetContext().search(name, matchingAttributes));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observation.observeChecked(() -> getTargetContext().search(name, filter, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
				throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observation.observeChecked(() -> getTargetContext().search(name, filter, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
				SearchControls cons) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observation.observeChecked(() -> getTargetContext().search(name, filterExpr, filterArgs, cons));
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
				SearchControls cons) throws javax.naming.NamingException {
			Observation observation = observation("search").highCardinalityKeyValue(name(name))
				.highCardinalityKeyValue(attributeIdsReturn(cons))
				.highCardinalityKeyValue(searchControls(cons));
			return observation.observeChecked(() -> getTargetContext().search(name, filterExpr, filterArgs, cons));
		}

		@Override
		public Object lookup(Name name) throws javax.naming.NamingException {
			Observation observation = observation("lookup").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().lookup(name));
		}

		@Override
		public Object lookup(String name) throws javax.naming.NamingException {
			Observation observation = observation("lookup").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().lookup(name));
		}

		@Override
		public void bind(Name name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().bind(name, obj));
		}

		@Override
		public void bind(String name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("bind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().bind(name, obj));
		}

		@Override
		public void rebind(Name name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().rebind(name, obj));
		}

		@Override
		public void rebind(String name, Object obj) throws javax.naming.NamingException {
			Observation observation = observation("rebind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().rebind(name, obj));
		}

		@Override
		public void unbind(Name name) throws javax.naming.NamingException {
			Observation observation = observation("unbind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().unbind(name));
		}

		@Override
		public void unbind(String name) throws javax.naming.NamingException {
			Observation observation = observation("unbind").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().unbind(name));
		}

		@Override
		public void rename(Name oldName, Name newName) throws javax.naming.NamingException {
			Observation observation = observation("rename").highCardinalityKeyValue(name("old.name", oldName))
				.highCardinalityKeyValue(name("newName", newName));
			observation.observeChecked(() -> getTargetContext().rename(oldName, newName));
		}

		@Override
		public void rename(String oldName, String newName) throws javax.naming.NamingException {
			Observation observation = observation("rename").highCardinalityKeyValue(name("old.name", oldName))
				.highCardinalityKeyValue(name("newName", newName));
			observation.observeChecked(() -> getTargetContext().rename(oldName, newName));
		}

		@Override
		public NamingEnumeration<NameClassPair> list(Name name) throws javax.naming.NamingException {
			Observation observation = observation("list").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().list(name));
		}

		@Override
		public NamingEnumeration<NameClassPair> list(String name) throws javax.naming.NamingException {
			Observation observation = observation("list").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().list(name));
		}

		@Override
		public NamingEnumeration<Binding> listBindings(Name name) throws javax.naming.NamingException {
			Observation observation = observation("listBindings").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().listBindings(name));
		}

		@Override
		public NamingEnumeration<Binding> listBindings(String name) throws javax.naming.NamingException {
			Observation observation = observation("listBindings").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().listBindings(name));
		}

		@Override
		public void destroySubcontext(Name name) throws javax.naming.NamingException {
			Observation observation = observation("destroySubcontext").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().destroySubcontext(name));
		}

		@Override
		public void destroySubcontext(String name) throws javax.naming.NamingException {
			Observation observation = observation("destroySubcontext").highCardinalityKeyValue(name(name));
			observation.observeChecked(() -> getTargetContext().destroySubcontext(name));
		}

		@Override
		public Context createSubcontext(Name name) throws javax.naming.NamingException {
			Observation observation = observation("createSubcontext").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().createSubcontext(name));
		}

		@Override
		public Context createSubcontext(String name) throws javax.naming.NamingException {
			Observation observation = observation("createSubcontext").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().createSubcontext(name));
		}

		@Override
		public Object lookupLink(Name name) throws javax.naming.NamingException {
			Observation observation = observation("lookupLink").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().lookupLink(name));
		}

		@Override
		public Object lookupLink(String name) throws javax.naming.NamingException {
			Observation observation = observation("lookupLink").highCardinalityKeyValue(name(name));
			return observation.observeChecked(() -> getTargetContext().lookupLink(name));
		}

		@Override
		public String getNameInNamespace() throws javax.naming.NamingException {
			Observation observation = observation("getNameInNamespace");
			return observation.observeChecked(getTargetContext()::getNameInNamespace);
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
			return KeyValue.of("attribute.ids", Collections.list(attributes.getIDs()).toString());
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
			if (attrs == null) {
				return KeyValue.of("attribute.ids", "all");
			}
			return KeyValue.of("attribute.ids", Arrays.toString(attrs));
		}

		private static KeyValue attributeIdsReturn(SearchControls searchControls) {
			if (searchControls == null) {
				return attributeIdsReturn((String[]) null);
			}
			return attributeIdsReturn(searchControls.getReturningAttributes());
		}

		private static KeyValue searchControls(SearchControls searchControls) {
			if (searchControls == null) {
				return KeyValue.of("search.controls", "default");
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

		Observation observation(String operation) {
			DirContextOperationObservationContext context = this.builder.operation(operation);
			return Observation.createNotStarted(this.convention, () -> context, this.registry);
		}

	}

	private abstract static class AbstractDirContextProxy implements DirContextProxy {

		private final DirContext delegate;

		AbstractDirContextProxy(DirContext delegate) {
			this.delegate = delegate;
		}

		@Override
		public DirContext getTargetContext() {
			return this.delegate;
		}

	}

}
