/*
 * Copyright 2002-2023 the original author or authors.
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
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link LdapClient}.
 *
 * @author Josh Cummings
 * @since 3.1
 */
class DefaultLdapClient implements LdapClient {

	private final Logger logger = LoggerFactory.getLogger(DefaultLdapClient.class);

	private static final boolean DONT_RETURN_OBJ_FLAG = false;

	private static final boolean RETURN_OBJ_FLAG = true;

	private final ContextSource contextSource;

	private final Supplier<SearchControls> searchControlsSupplier;

	private boolean ignorePartialResultException = false;

	private boolean ignoreNameNotFoundException = false;

	private boolean ignoreSizeLimitExceededException = true;

	DefaultLdapClient(ContextSource contextSource, Supplier<SearchControls> searchControlsSupplier) {
		this.contextSource = contextSource;
		this.searchControlsSupplier = searchControlsSupplier;
	}

	@Override
	public ListSpec list(String name) {
		return new DefaultListSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public ListSpec list(Name name) {
		return new DefaultListSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public ListBindingsSpec listBindings(String name) {
		return new DefaultListBindingsSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public ListBindingsSpec listBindings(Name name) {
		return new DefaultListBindingsSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public SearchSpec search() {
		return new DefaultSearchSpec();
	}

	@Override
	public AuthenticateSpec authenticate() {
		return new DefaultAuthenticateSpec();
	}

	@Override
	public BindSpec bind(String name) {
		return new DefaultBindSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public BindSpec bind(Name name) {
		return new DefaultBindSpec(LdapUtils.newLdapName(name));
	}

	@Override
	public ModifySpec modify(String name) {
		return new DefaultModifySpec(new DirContextAdapter(LdapUtils.newLdapName(name)));
	}

	@Override
	public ModifySpec modify(Name name) {
		return new DefaultModifySpec(new DirContextAdapter(LdapUtils.newLdapName(name)));
	}

	@Override
	public UnbindSpec unbind(String name) {
		return new DefaultUnbindSpec(LdapUtils.newLdapName(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UnbindSpec unbind(Name name) {
		return new DefaultUnbindSpec(LdapUtils.newLdapName(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Builder mutate() {
		return new DefaultLdapClientBuilder(this.contextSource, this.searchControlsSupplier);
	}

	/**
	 * Ignore {@link PartialResultException}s.
	 * @param ignorePartialResultException whether to ignore
	 * {@link PartialResultException}s
	 */
	void setIgnorePartialResultException(boolean ignorePartialResultException) {
		this.ignorePartialResultException = ignorePartialResultException;
	}

	/**
	 * Ignore {@link NameNotFoundException}s.
	 * @param ignoreNameNotFoundException whether to ignore {@link NameNotFoundException}s
	 */
	void setIgnoreNameNotFoundException(boolean ignoreNameNotFoundException) {
		this.ignoreNameNotFoundException = ignoreNameNotFoundException;
	}

	/**
	 * Ignore {@link SizeLimitExceededException}s.
	 * @param ignoreSizeLimitExceededException whether to ignore
	 * {@link SizeLimitExceededException}s
	 */
	void setIgnoreSizeLimitExceededException(boolean ignoreSizeLimitExceededException) {
		this.ignoreSizeLimitExceededException = ignoreSizeLimitExceededException;
	}

	private final class DefaultListSpec implements ListSpec {

		private final Name name;

		private DefaultListSpec(Name name) {
			this.name = name;
		}

		@Override
		public <T> List<T> toList(NameClassPairMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<NameClassPair>> executor = (ctx) -> ctx.list(this.name);
			NamingEnumeration<NameClassPair> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toList(results, mapper::mapFromNameClassPair);
		}

		@Override
		public <T> Stream<T> toStream(NameClassPairMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<NameClassPair>> executor = (ctx) -> ctx.list(this.name);
			NamingEnumeration<NameClassPair> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toStream(results, mapper::mapFromNameClassPair);
		}

	}

	private final class DefaultListBindingsSpec implements ListBindingsSpec {

		private final Name name;

		private DefaultListBindingsSpec(Name name) {
			this.name = name;
		}

		@Override
		public <T> List<T> toList(NameClassPairMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<Binding>> executor = (ctx) -> ctx.listBindings(this.name);
			NamingEnumeration<Binding> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toList(results, mapper::mapFromNameClassPair);
		}

		@Override
		public <T> List<T> toList(ContextMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<Binding>> executor = (ctx) -> ctx.listBindings(this.name);
			NamingEnumeration<Binding> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toList(results, function(mapper));
		}

		@Override
		public <T> Stream<T> toStream(NameClassPairMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<Binding>> executor = (ctx) -> ctx.listBindings(this.name);
			NamingEnumeration<Binding> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toStream(results, mapper::mapFromNameClassPair);
		}

		@Override
		public <T> Stream<T> toStream(ContextMapper<T> mapper) {
			ContextExecutor<NamingEnumeration<Binding>> executor = (ctx) -> ctx.listBindings(this.name);
			NamingEnumeration<Binding> results = computeWithReadOnlyContext(executor);
			return DefaultLdapClient.this.toStream(results, function(mapper));
		}

	}

	private final class DefaultAuthenticateSpec implements AuthenticateSpec {

		LdapClient.SearchSpec search = new DefaultSearchSpec();

		char[] password;

		@Override
		public AuthenticateSpec query(LdapQuery query) {
			this.search.query(query);
			return this;
		}

		@Override
		public AuthenticateSpec password(String password) {
			this.password = password.toCharArray();
			return this;
		}

		@Override
		public void execute() {
			execute((ctx, identification) -> ctx);
		}

		@Override
		public <T> T execute(AuthenticatedLdapEntryContextMapper<T> mapper) {
			LdapEntryIdentificationContextMapper m = new LdapEntryIdentificationContextMapper();
			List<LdapEntryIdentification> identification = this.search.toList(m);
			if (identification.size() == 0) {
				throw new EmptyResultDataAccessException(1);
			}
			else if (identification.size() != 1) {
				throw new IncorrectResultSizeDataAccessException(1, identification.size());
			}
			DirContext ctx = null;
			try {
				String password = (this.password != null) ? new String(this.password) : null;
				ctx = contextSource.getContext(identification.get(0).getAbsoluteName().toString(), password);
				return mapper.mapWithContext(ctx, identification.get(0));
			}
			finally {
				this.password = null;
				closeContext(ctx);
			}
		}

	}

	private final class DefaultSearchSpec implements SearchSpec {

		LdapQuery query = LdapQueryBuilder.query().filter("(objectClass=*)");

		SearchControls controls;

		@Override
		public SearchSpec name(String name) {
			return query((builder) -> builder.base(name).searchScope(SearchScope.OBJECT));
		}

		@Override
		public SearchSpec name(Name name) {
			return query((builder) -> builder.base(name).searchScope(SearchScope.OBJECT));
		}

		public SearchSpec query(Consumer<LdapQueryBuilder> consumer) {
			LdapQueryBuilder builder = LdapQueryBuilder.fromQuery(this.query);
			consumer.accept(builder);
			this.query = builder;
			return this;
		}

		@Override
		public SearchSpec query(LdapQuery query) {
			this.query = query;
			return this;
		}

		@Override
		public <T> T toObject(ContextMapper<T> mapper) {
			this.controls = searchControlsForQuery(RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toObject(results, function(mapper));
		}

		@Override
		public <T> T toObject(AttributesMapper<T> mapper) {
			this.controls = searchControlsForQuery(DONT_RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toObject(results, function(mapper));
		}

		@Override
		public <T> List<T> toList(ContextMapper<T> mapper) {
			this.controls = searchControlsForQuery(RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toList(results, function(mapper));
		}

		@Override
		public <T> List<T> toList(AttributesMapper<T> mapper) {
			this.controls = searchControlsForQuery(DONT_RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toList(results, function(mapper));
		}

		@Override
		public <T> Stream<T> toStream(ContextMapper<T> mapper) {
			this.controls = searchControlsForQuery(RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toStream(results, function(mapper));
		}

		@Override
		public <T> Stream<T> toStream(AttributesMapper<T> mapper) {
			this.controls = searchControlsForQuery(DONT_RETURN_OBJ_FLAG);
			NamingEnumeration<SearchResult> results = computeWithReadOnlyContext(this::search);
			return DefaultLdapClient.this.toStream(results, function(mapper));
		}

		private NamingEnumeration<SearchResult> search(DirContext ctx) throws NamingException {
			return ctx.search(this.query.base(), this.query.filter().encode(), this.controls);
		}

		private SearchControls searchControlsForQuery(boolean returnObjFlag) {
			SearchControls controls = DefaultLdapClient.this.searchControlsSupplier.get();
			controls.setReturningObjFlag(returnObjFlag);
			controls.setReturningAttributes(this.query.attributes());
			if (this.query.searchScope() != null) {
				controls.setSearchScope(query.searchScope().getId());
			}
			if (this.query.countLimit() != null) {
				controls.setCountLimit(query.countLimit());
			}
			if (this.query.timeLimit() != null) {
				controls.setTimeLimit(query.timeLimit());
			}
			return controls;
		}

	}

	private final class DefaultBindSpec implements BindSpec {

		private final Name name;

		private Object obj;

		private Attributes attributes;

		private boolean rebind = false;

		private DefaultBindSpec(Name name) {
			this.name = name;
		}

		public BindSpec object(Object obj) {
			if (obj instanceof DirContextOperations) {
				boolean updateMode = ((DirContextOperations) obj).isUpdateMode();
				Assert.isTrue(!updateMode, "DirContextOperations must not be in update mode");
			}
			this.obj = obj;
			return this;
		}

		public BindSpec attributes(Attributes attributes) {
			this.attributes = attributes;
			return this;
		}

		@Override
		public BindSpec replaceExisting(boolean replaceExisting) {
			this.rebind = replaceExisting;
			return this;
		}

		@Override
		public void execute() {
			if (this.rebind) {
				runWithReadWriteContext((ctx) -> ctx.rebind(this.name, this.obj, this.attributes));
			}
			else {
				runWithReadWriteContext((ctx) -> ctx.bind(this.name, this.obj, this.attributes));
			}
		}

	}

	private final class DefaultModifySpec implements ModifySpec {

		private final DirContextOperations entry;

		private Name name;

		private ModificationItem[] items;

		private DefaultModifySpec(DirContextOperations entry) {
			this.entry = entry;
			this.name = entry.getDn();
			this.items = entry.getModificationItems();
		}

		@Override
		public ModifySpec name(String name) {
			this.name = LdapUtils.newLdapName(name);
			return this;
		}

		@Override
		public ModifySpec name(Name name) {
			this.name = LdapUtils.newLdapName(name);
			return this;
		}

		@Override
		public ModifySpec attributes(ModificationItem... modifications) {
			this.items = modifications;
			return this;
		}

		@Override
		public void execute() {
			boolean renamed = false;
			if (!this.entry.getDn().equals(this.name)) {
				runWithReadWriteContext((ctx) -> ctx.rename(this.entry.getDn(), this.name));
				renamed = true;
			}
			try {
				if (this.items.length > 0) {
					runWithReadWriteContext((ctx) -> ctx.modifyAttributes(this.name, this.items));
				}
			}
			catch (Throwable t) {
				if (renamed) {
					// attempt to change the name back
					runWithReadWriteContext((ctx) -> ctx.rename(this.name, this.entry.getDn()));
				}
				throw t;
			}
		}

	}

	private final class DefaultUnbindSpec implements UnbindSpec {

		private final Name name;

		private boolean recursive = false;

		private DefaultUnbindSpec(Name name) {
			this.name = name;
		}

		@Override
		public UnbindSpec recursive(boolean recursive) {
			this.recursive = recursive;
			return this;
		}

		@Override
		public void execute() {
			if (this.recursive) {
				runWithReadWriteContext((ctx) -> unbindRecursive(ctx, this.name));
				return;
			}
			runWithReadWriteContext((ctx) -> ctx.unbind(this.name));
		}

		void unbindRecursive(DirContext ctx, Name name) throws NamingException {
			NamingEnumeration<Binding> bindings = null;
			try {
				bindings = ctx.listBindings(name);
				while (bindings.hasMore()) {
					Binding binding = bindings.next();
					LdapName childName = LdapUtils.newLdapName(binding.getName());
					childName.addAll(0, name);
					unbindRecursive(ctx, childName);
				}
				ctx.unbind(name);
				if (DefaultLdapClient.this.logger.isDebugEnabled()) {
					DefaultLdapClient.this.logger.debug("Entry " + name + " deleted");
				}
			}
			finally {
				closeNamingEnumeration(bindings);
			}
		}

	}

	<T> T computeWithReadOnlyContext(ContextExecutor<T> executor) {
		DirContext context = this.contextSource.getReadOnlyContext();
		try {
			return executor.executeWithContext(context);
		}
		catch (NamingException ex) {
			this.namingExceptionHandler.accept(ex);
			return null;
		}
		finally {
			closeContext(context);
		}
	}

	void runWithReadWriteContext(ContextRunnable runnable) {
		DirContext context = this.contextSource.getReadWriteContext();
		try {
			runnable.run(context);
		}
		catch (NamingException ex) {
			this.namingExceptionHandler.accept(ex);
		}
		finally {
			closeContext(context);
		}
	}

	private <T> NamingExceptionFunction<? extends Binding, T> function(ContextMapper<T> mapper) {
		return (result) -> mapper.mapFromContext(result.getObject());
	}

	private <T> NamingExceptionFunction<? extends SearchResult, T> function(AttributesMapper<T> mapper) {
		return (result) -> mapper.mapFromAttributes(result.getAttributes());
	}

	private <T> Enumeration<T> enumeration(NamingEnumeration<T> enumeration) {
		return new Enumeration<>() {
			@Override
			public boolean hasMoreElements() {
				try {
					return enumeration.hasMore();
				}
				catch (NamingException ex) {
					namingExceptionHandler.accept(ex);
					return false;
				}
			}

			@Override
			public T nextElement() {
				try {
					return enumeration.next();
				}
				catch (NamingException ex) {
					namingExceptionHandler.accept(ex);
					throw new NoSuchElementException("no such element", ex);
				}
			}
		};
	}

	private final Consumer<NamingException> namingExceptionHandler = (ex) -> {
		if (ex instanceof NameNotFoundException) {
			if (!this.ignoreNameNotFoundException) {
				throw LdapUtils.convertLdapException(ex);
			}
			this.logger.warn("Base context not found, ignoring: " + ex.getMessage());
			return;
		}
		if (ex instanceof PartialResultException) {
			// Workaround for AD servers not handling referrals correctly.
			if (!this.ignorePartialResultException) {
				throw LdapUtils.convertLdapException(ex);
			}
			this.logger.debug("PartialResultException encountered and ignored", ex);
			return;
		}
		if (ex instanceof SizeLimitExceededException) {
			if (!this.ignoreSizeLimitExceededException) {
				throw LdapUtils.convertLdapException(ex);
			}
			this.logger.debug("SizeLimitExceededException encountered and ignored", ex);
			return;
		}
		throw LdapUtils.convertLdapException(ex);
	};

	private <S extends NameClassPair, T> T toObject(NamingEnumeration<S> results,
			NamingExceptionFunction<? super S, T> mapper) {
		try {
			Enumeration<S> enumeration = enumeration(results);
			Function<? super S, T> function = mapper.wrap(this.namingExceptionHandler);
			if (!enumeration.hasMoreElements()) {
				return null;
			}
			T result = function.apply(enumeration.nextElement());
			if (enumeration.hasMoreElements()) {
				throw new IncorrectResultSizeDataAccessException(1);
			}
			return result;
		}
		finally {
			closeNamingEnumeration(results);
		}
	}

	private <S extends NameClassPair, T> List<T> toList(NamingEnumeration<S> results,
			NamingExceptionFunction<? super S, T> mapper) {
		if (results == null) {
			return Collections.emptyList();
		}
		try {
			Enumeration<S> enumeration = enumeration(results);
			Function<? super S, T> function = mapper.wrap(this.namingExceptionHandler);
			List<T> mapped = new ArrayList<>();
			while (enumeration.hasMoreElements()) {
				T result = function.apply(enumeration.nextElement());
				if (result != null) {
					mapped.add(result);
				}
			}
			return mapped;
		}
		finally {
			closeNamingEnumeration(results);
		}
	}

	private <S extends NameClassPair, T> Stream<T> toStream(NamingEnumeration<S> results,
			NamingExceptionFunction<? super S, T> mapper) {
		if (results == null) {
			return Stream.empty();
		}
		Enumeration<S> enumeration = enumeration(results);
		Function<? super S, T> function = mapper.wrap(this.namingExceptionHandler);
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(enumeration.asIterator(), Spliterator.ORDERED), false)
				.map(function::apply).filter(Objects::nonNull).onClose(() -> closeNamingEnumeration(results));
	}

	private void closeContext(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (Exception e) {
				// Never mind this.
			}
		}
	}

	private <T> void closeNamingEnumeration(NamingEnumeration<T> results) {
		if (results != null) {
			try {
				results.close();
			}
			catch (Exception e) {
				// Never mind this.
			}
		}
	}

	interface ContextRunnable {

		void run(DirContext ctx) throws NamingException;

	}

	interface NamingExceptionFunction<S, T> {

		T apply(S element) throws NamingException;

		default Function<S, T> wrap(Consumer<NamingException> handler) {
			return (s) -> {
				try {
					return apply(s);
				}
				catch (NamingException ex) {
					handler.accept(ex);
					return null;
				}
			};
		}

	}

}
