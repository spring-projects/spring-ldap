/*
 * Copyright 2005-2023 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.springframework.LdapDataEntry;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.PartialResultException;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

/**
 * An LDAP Client
 *
 * @author Josh Cummings
 * @since 3.1
 */
public interface LdapClient {

	/**
	 * Start building a request for all children of the
	 * given {@code name}.
	 *
	 * @param name the distinguished name to find children for
	 * @return a spec for specifying the list parameters
	 */
	ListSpec list(String name);

	/**
	 * Start building a request for all children of the
	 * given {@code name}.
	 *
	 * @param name the distinguished name to find children for
	 * @return a spec for specifying the list parameters
	 */
	ListSpec list(Name name);

	/**
	 * Start building a request for all children of the
	 * given {@code name}. The result will include the object bound to
	 * the name.
	 *
	 * @param name the distinguished name to find children for
	 * @return a spec for specifying the list parameters
	 */
	ListBindingsSpec listBindings(String name);

	/**
	 * Start building a request for all children of the
	 * given {@code name}. The result will include the object bound to
	 * the name.
	 *
	 * @param name the distinguished name to find children for
	 * @return a spec for specifying the list parameters
	 */
	ListBindingsSpec listBindings(Name name);

	/**
	 * Start building a search request.
	 *
	 * @return a spec for specifying the search parameters
	 */
	SearchSpec search();

	/**
	 * Start building an authentication request.
	 *
	 * @return a spec for specifying the authentication parameters
	 */
	AuthenticateSpec authenticate();

	/**
	 * Start building a bind request, using the given {@code name}
	 * as the identifier.
	 *
	 * @return a spec for specifying the bind parameters
	 */
	BindSpec bind(String name);

	/**
	 * Start building a bind or rebind request, using the given {@code name}
	 * as the identifier.
	 *
	 * @return a spec for specifying the bind parameters
	 */
	BindSpec bind(Name name);

	/**
	 * Start building a request to modify name or attributes of an entry, using the given {@code name}
	 * as the identifier.
	 *
	 * <p>
	 * Note that a {@link #modify(Name)} is different from a rebind in that
	 * entries are changed instead of removed and recreated.
	 *
	 * <p>
	 * A change in name uses LDAP's {@link DirContext#rename} function.
	 * A change in attributes uses LDAP's {@link DirContext#modifyAttributes} function.
	 * The {@code rename} action is optimistically performed before the {@code modify} function.
	 * A rollback of the name is attempted in the event that attribute modification fails.
	 *
	 * @param name the name of the entry to modify
	 * @return a spec for specifying the modify parameters
	 */
	ModifySpec modify(String name);

	/**
	 * Start building a request to modify name or attributes of an entry, using the given {@code name}
	 * as the identifier.
	 *
	 * <p>
	 * Note that a {@link #modify(Name)} is different from a rebind in that
	 * entries are changed instead of removed and recreated.
	 *
	 * <p>
	 * A change in name uses LDAP's {@link DirContext#rename} function.
	 * A change in attributes uses LDAP's {@link DirContext#modifyAttributes} function.
	 * The {@code rename} action is optimistically performed before the {@code modify} function.
	 * A rollback of the name is attempted in the event that attribute modification fails.
	 *
	 * @param name the name of the entry to modify
	 * @return a spec for specifying the modify parameters
	 */
	ModifySpec modify(Name name);

	/**
	 * Start building a request to remove the {@code name} entry.
	 *
	 * @param name the name of the entry to remove
	 * @return a spec for specifying the unbind parameters
	 */
	UnbindSpec unbind(String name);

	/**
	 * Start building a request to remove the {@code name} entry.
	 *
	 * @param name the name of the entry to remove
	 * @return a spec for specifying the unbind parameters
	 */
	UnbindSpec unbind(Name name);

	/**
	 * Return a builder to create a new {@code LdapClient} whose settings are
	 * replicated from the current {@code LdapClient}.
	 */
	Builder mutate();


	// Static, factory methods

	/**
	 * Create an instance of {@link LdapClient}
	 * @param contextSource the {@link ContextSource} for all requests
	 * @see #builder()
	 */
	static LdapClient create(ContextSource contextSource) {
		return new DefaultLdapClientBuilder().contextSource(contextSource).build();
	}

	/**
	 * Obtain a {@code LdapClient} builder.
	 */
	static LdapClient.Builder builder() {
		return new DefaultLdapClientBuilder();
	}


	/**
	 * A mutable builder for creating an {@link LdapClient}.
	 */
	interface Builder {

		/**
		 * Use this {@link ContextSource}
		 * @return the {@link Builder} for further customizations
		 */
		Builder contextSource(ContextSource contextSource);

		/**
		 * Use this {@link Supplier} to generate a {@link SearchControls}.
		 * It should generate a new {@link SearchControls} on each call.
		 * @param searchControlsSupplier the {@link Supplier} to use
		 * @return the {@link Builder} for further customizations
		 */
		Builder defaultSearchControls(Supplier<SearchControls> searchControlsSupplier);

		/**
		 * Whether to ignore the {@link org.springframework.ldap.PartialResultException}.
		 * Defaults to {@code true}.
		 *
		 * @param ignore whether to ignore the {@link PartialResultException}
		 * @return the {@link LdapClient.Builder} for further customizations
		 */
		Builder ignorePartialResultException(boolean ignore);

		/**
		 * Whether to ignore the {@link org.springframework.ldap.NameNotFoundException}.
		 * Defaults to {@code true}.
		 *
		 * @param ignore whether to ignore the {@link NameNotFoundException}
		 * @return the {@link LdapClient.Builder} for further customizations
		 */
		Builder ignoreNameNotFoundException(boolean ignore);

		/**
		 * Whether to ignore the {@link org.springframework.ldap.SizeLimitExceededException}.
		 * Defaults to {@code true}.
		 *
		 * @param ignore whether to ignore the {@link SizeLimitExceededException}
		 * @return the {@link LdapClient.Builder} for further customizations
		 */
		Builder ignoreSizeLimitExceededException(boolean ignore);

		/**
		 * Apply the given {@code Consumer} to this builder instance.
		 * <p>This can be useful for applying pre-packaged customizations.
		 * @param builderConsumer the consumer to apply
		 */
		Builder apply(Consumer<Builder> builderConsumer);

		/**
		 * Clone this {@code LdapClient.Builder}.
		 */
		Builder clone();

		/**
		 * Build the {@link LdapClient} instance.
		 */
		LdapClient build();
	}

	/**
	 * The specifications for the {@link #list} request.
	 */
	interface ListSpec {
		/**
		 * Return the entry's children as a list of mapped results
		 *
		 * @param mapper the {@link NameClassPairMapper} strategy to mapping each search result
		 * @return the entry's children or an empty list
		 */
		<T> List<T> toList(NameClassPairMapper<T> mapper);

		/**
		 * Return the entry's children as a stream of mapped results. Note that
		 * the {@link Stream} must be closed when done reading from it.
		 *
		 * @param mapper the {@link NameClassPairMapper} strategy to mapping each search result
		 * @return the entry's children or an empty stream
		 */
		<T> Stream<T> toStream(NameClassPairMapper<T> mapper);
	}

	/**
	 * The specifications for the {@link #listBindings} request.
	 */
	interface ListBindingsSpec {
		/**
		 * Return the entry's children as a list of mapped results
		 *
		 * @param mapper the {@link NameClassPairMapper} strategy to mapping each search result
		 * @return the entry's children or an empty list
		 */
		<T> List<T> toList(NameClassPairMapper<T> mapper);

		/**
		 * Return the entry's children as a list of mapped results
		 *
		 * @param mapper the {@link ContextMapper} strategy to mapping each search result
		 * @return the entry's children or an empty list
		 */
		<T> List<T> toList(ContextMapper<T> mapper);

		/**
		 * Return the entry's children as a stream of mapped results. Note that
		 * the {@link Stream} must be closed when done reading from it.
		 *
		 * @param mapper the {@link NameClassPairMapper} strategy to mapping each search result
		 * @return the entry's children or an empty stream
		 */
		<T> Stream<T> toStream(NameClassPairMapper<T> mapper);

		/**
		 * Return the entry's children as a stream of mapped results. Note that
		 * the {@link Stream} must be closed when done reading from it.
		 *
		 * @param mapper the {@link ContextMapper} strategy to mapping each search result
		 * @return the entry's children or an empty stream
		 */
		<T> Stream<T> toStream(ContextMapper<T> mapper);
	}

	/**
	 * The specifications for the {@link #search} request.
	 */
	interface SearchSpec {
		/**
		 * The name to search for. This is a convenience method for
		 * creating an {@link LdapQuery} based only on the {@code name}.
		 *
		 * @param name the name to search for
		 * @return the {@link SearchSpec} for further configuration
		 */
		SearchSpec name(String name);

		/**
		 * The name to search for. This is a convenience method for
		 * creating an {@link LdapQuery} based only on the {@code name}.
		 *
		 * @param name the name to search for
		 * @return the {@link SearchSpec} for further configuration
		 */
		SearchSpec name(Name name);

		/**
		 * The no-filter query to execute. Or, that is, the filter is {@code (objectclass=*)}.
		 *
		 * <p>This is helpful when searching by name and needing to customize the {@link SearchControls} or the
		 * returned attribute set.
		 *
		 * @param consumer the consumer to alter a default query
		 * @return the {@link SearchSpec} for further configuration
		 */
		SearchSpec query(Consumer<LdapQueryBuilder> consumer);

		/**
		 * The query to execute.
		 *
		 * @param query the query to execute
		 * @return the {@link SearchSpec} for further configuration
		 */
		SearchSpec query(LdapQuery query);

		default <O extends LdapDataEntry> O toEntry() {
			ContextMapper<O> cast = (ctx) -> (O) ctx;
			return toObject(cast);
		}

		/**
		 * Expect at most one search result, mapped by the given strategy.
		 *
		 * <p>Returns {@code null} if no result is found.
		 *
		 * @param mapper the {@link ContextMapper} strategy to use to map the result
		 * @return the single search result, or {@code null} if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> O toObject(ContextMapper<O> mapper);

		/**
		 * Expect at most one search result, mapped by the given strategy.
		 *
		 * @param mapper the {@link AttributesMapper} strategy to use to map the result
		 * @return the single search result, or {@code null} if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> O toObject(AttributesMapper<O> mapper);

		default <O extends LdapDataEntry> List<O> toEntryList() {
			ContextMapper<O> cast = (ctx) -> (O) ctx;
			return toList(cast);
		}

		/**
		 * Return a list of search results, each mapped by the given strategy.
		 *
		 * @param mapper the {@link ContextMapper} strategy to use to map the result
		 * @return the single search result, or empty list if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> List<O> toList(ContextMapper<O> mapper);

		/**
		 * Return a list of search results, each mapped by the given strategy.
		 *
		 * @param mapper the {@link AttributesMapper} strategy to use to map the result
		 * @return the single search result, or empty list if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> List<O> toList(AttributesMapper<O> mapper);

		default <O extends LdapDataEntry> Stream<O> toEntryStream() {
			ContextMapper<O> cast = (ctx) -> (O) ctx;
			return toStream(cast);
		}

		/**
		 * Return a stream of search results, each mapped by the given strategy.
		 *
		 * @param mapper the {@link ContextMapper} strategy to use to map the result
		 * @return the single search result, or empty stream if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> Stream<O> toStream(ContextMapper<O> mapper);

		/**
		 * Return a stream of search results, each mapped by the given strategy.
		 *
		 * @param mapper the {@link AttributesMapper} strategy to use to map the result
		 * @return the single search result, or empty stream if none was found
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the result
		 * set contains more than one result
		 */
		<O> Stream<O> toStream(AttributesMapper<O> mapper);
	}

	/**
	 * The specifications for the {@link #authenticate} request.
	 */
	interface AuthenticateSpec {
		/**
		 * The query to authenticate
		 *
		 * @param query the query to authenticate
		 * @return the {@link AuthenticateSpec} for further configuration
		 */
		AuthenticateSpec query(LdapQuery query);

		/**
		 * The password to use
		 *
		 * @param password the password to use
		 * @return the {@link AuthenticateSpec} for further configuration
		 */
		AuthenticateSpec password(String password);

		/**
		 * Authenticate the query against the provided password
		 *
		 * @throws org.springframework.ldap.AuthenticationException if authentication fails or the query returns no results
		 */
		void execute();

		/**
		 * Authenticate the query against the provided password.
		 *
		 * @param mapper a strategy for mapping the query results against another datasource
		 * @throws org.springframework.ldap.AuthenticationException if authentication fails or the query returns no results
		 */
		<T> T execute(AuthenticatedLdapEntryContextMapper<T> mapper);
	}

	/**
	 * The specifications for the {@link #bind} request.
	 */
	interface BindSpec {
		/**
		 * The object to associate with this binding.
		 *
		 * <p>
		 * Note that this object is encoded into a set of attributes. If the object is
		 * of type {@link DirContext}, then it will be converted into attributes via
		 * {@link DirContext#getAttributes}.
		 *
		 * @param object the object to associate
		 * @return the {@link BindSpec} for further configuration
		 */
		BindSpec object(Object object);

		/**
		 * The attributes to associate with this binding.
		 * @param attributes the attributes
		 * @return the {@link BindSpec} for further configuration
		 */
		BindSpec attributes(Attributes attributes);

		/**
		 * Replace any existing binding with this one (equivalent to "rebind").
		 *
		 * <p>
		 * If {@code false}, then bind will throw a {@link NameAlreadyBoundException} if the entry
		 * already exists.
		 *
		 * @param replaceExisting whether to replace any existing entry
		 * @return the {@link BindSpec} for further configuration
		 */
		BindSpec replaceExisting(boolean replaceExisting);

		/**
		 * Bind the name, object, and attributes together
		 *
		 * @throws NameAlreadyBoundException if {@code name} is already bound and {@link #replaceExisting} is {@code false}
		 */
		void execute();
	}

	/**
	 * The specifications for the {@link #modify} request.
	 */
	interface ModifySpec {
		/**
		 * The new name for this entry.
		 *
		 * @param name the new name
		 * @return the {@link ModifySpec} for further configuration
		 */
		ModifySpec name(String name);

		/**
		 * The new name for this entry.
		 *
		 * @param name the new name
		 * @return the {@link ModifySpec} for further configuration
		 */
		ModifySpec name(Name name);

		/**
		 * The attribute modifications to apply to this entry
		 *
		 * @param modifications the attribute modifications
		 * @return the {@link ModifySpec} for further configuration
		 */
		ModifySpec attributes(ModificationItem... modifications);

		/**
		 * Modify the name and attributes for this entry
		 */
		void execute();
	}

	/**
	 * The specifications for the {@link #unbind} request.
	 */
	interface UnbindSpec {
		/**
		 * Delete all children related to this entry
		 *
		 * @param recursive whether to delete all children as well
		 * @return the {@link UnbindSpec} for further configuration
		 */
		UnbindSpec recursive(boolean recursive);

		/**
		 * Delete the entry
		 */
		void execute();
	}
}