/*
 * Copyright 2005-2021 the original author or authors.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract implementation of the {@link ContextSource} interface. By default, returns an
 * authenticated <code>DirContext</code> implementation for both read-only and read-write
 * operations. To have an anonymous environment created for read-only operations, set the
 * <code>anonymousReadOnly</code> property to <code>true</code>.
 * <p>
 * Implementing classes need to implement {@link #getDirContextInstance(Hashtable)} to
 * create a <code>DirContext</code> instance of the desired type.
 * <p>
 * If an {@link AuthenticationSource} is set, this will be used for getting user principal
 * and password for each new connection, otherwise a default one will be created using the
 * specified <code>userDn</code> and <code>password</code>.
 * <p>
 * <b>Note:</b> When using implementations of this class outside of a Spring Context it is
 * necessary to call {@link #afterPropertiesSet()} when all properties are set, in order
 * to finish up initialization.
 *
 * @author Mattias Hellborg Arthursson
 * @author Adam Skogman
 * @author Ulrik Sandberg
 * @see org.springframework.ldap.core.LdapTemplate
 * @see org.springframework.ldap.core.support.DefaultDirObjectFactory
 * @see org.springframework.ldap.core.support.LdapContextSource
 * @see org.springframework.ldap.core.support.DirContextSource
 */
public abstract class AbstractContextSource implements BaseLdapPathContextSource, InitializingBean {

	private static final String DEFAULT_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	private static final Class<DefaultDirObjectFactory> DEFAULT_DIR_OBJECT_FACTORY = DefaultDirObjectFactory.class;

	private static final boolean DONT_DISABLE_POOLING = false;

	private static final boolean EXPLICITLY_DISABLE_POOLING = true;

	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private Class<?> dirObjectFactory = DEFAULT_DIR_OBJECT_FACTORY;

	private Class<?> contextFactory;

	private LdapName base = LdapUtils.emptyLdapName();

	/**
	 * @deprecated use {@link #getUserDn()} and {@link #setUserDn(String)} instead
	 */
	@Deprecated
	protected String userDn = "";

	/**
	 * @deprecated use {@link #getPassword()} and {@link #setPassword(String)} instead
	 */
	@Deprecated
	protected String password = "";

	private String[] urls;

	private boolean pooled = false;

	private Hashtable<String, Object> baseEnv = new Hashtable<String, Object>();

	private Hashtable<String, Object> anonymousEnv;

	private AuthenticationSource authenticationSource;

	private boolean cacheEnvironmentProperties = true;

	private boolean anonymousReadOnly = false;

	private String referral = null;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractContextSource.class);

	public static final String SUN_LDAP_POOLING_FLAG = "com.sun.jndi.ldap.connect.pool";

	private static final String JDK_142 = "1.4.2";

	private DirContextAuthenticationStrategy authenticationStrategy = new SimpleDirContextAuthenticationStrategy();

	public AbstractContextSource() {
		try {
			this.contextFactory = Class.forName(DEFAULT_CONTEXT_FACTORY);
		}
		catch (ClassNotFoundException ex) {
			LOG.trace("The default for contextFactory cannot be resolved", ex);
		}
	}

	public DirContext getContext(String principal, String credentials) {
		// This method is typically called for authentication purposes, which means that
		// we
		// should explicitly disable pooling in case passwords are changed (LDAP-183).
		return doGetContext(principal, credentials, EXPLICITLY_DISABLE_POOLING);
	}

	private DirContext doGetContext(String principal, String credentials, boolean explicitlyDisablePooling) {
		Hashtable<String, Object> env = getAuthenticatedEnv(principal, credentials);
		if (explicitlyDisablePooling) {
			env.remove(SUN_LDAP_POOLING_FLAG);
		}

		DirContext ctx = createContext(env);

		try {
			DirContext processedDirContext = this.authenticationStrategy.processContextAfterCreation(ctx, principal,
					credentials);
			return processedDirContext;
		}
		catch (NamingException ex) {
			closeContext(ctx);
			throw LdapUtils.convertLdapException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.ContextSource#getReadOnlyContext()
	 */
	public DirContext getReadOnlyContext() {
		if (!this.anonymousReadOnly) {
			return doGetContext(this.authenticationSource.getPrincipal(), this.authenticationSource.getCredentials(),
					DONT_DISABLE_POOLING);
		}
		else {
			return createContext(getAnonymousEnv());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.ContextSource#getReadWriteContext()
	 */
	public DirContext getReadWriteContext() {
		return doGetContext(this.authenticationSource.getPrincipal(), this.authenticationSource.getCredentials(),
				DONT_DISABLE_POOLING);
	}

	/**
	 * Default implementation of setting the environment up to be authenticated. This
	 * method should typically NOT be overridden; any customization to the authentication
	 * mechanism should be managed by setting a different
	 * {@link DirContextAuthenticationStrategy} on this instance.
	 * @param env the environment to modify.
	 * @param principal the principal to authenticate with.
	 * @param credentials the credentials to authenticate with.
	 * @see DirContextAuthenticationStrategy
	 * @see #setAuthenticationStrategy(DirContextAuthenticationStrategy)
	 */
	protected void setupAuthenticatedEnvironment(Hashtable<String, Object> env, String principal, String credentials) {
		try {
			this.authenticationStrategy.setupEnvironment(env, principal, credentials);
		}
		catch (NamingException ex) {
			throw LdapUtils.convertLdapException(ex);
		}
	}

	/**
	 * Close the context and swallow any exceptions.
	 * @param ctx the DirContext to close.
	 */
	private void closeContext(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (Exception ex) {
				LOG.debug("Exception closing context", ex);
			}
		}
	}

	/**
	 * Assemble a valid url String from all registered urls to add as
	 * <code>PROVIDER_URL</code> to the environment.
	 * @param ldapUrls all individual url Strings.
	 * @return the full url String
	 */
	public String assembleProviderUrlString(String[] ldapUrls) {
		StringBuilder providerUrlBuffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
		for (String ldapUrl : ldapUrls) {
			providerUrlBuffer.append(ldapUrl);
			if (!this.base.isEmpty()) {
				if (!ldapUrl.endsWith("/")) {
					providerUrlBuffer.append("/");
				}
			}
			providerUrlBuffer.append(formatForUrl(this.base));
			providerUrlBuffer.append(' ');
		}
		return providerUrlBuffer.toString().trim();
	}

	static String formatForUrl(LdapName ldapName) {
		StringBuilder sb = new StringBuilder();
		ListIterator<Rdn> it = ldapName.getRdns().listIterator(ldapName.size());
		while (it.hasPrevious()) {
			Rdn component = it.previous();

			Attributes attributes = component.toAttributes();

			// Loop through all attribute of the rdn (usually just one, but more are
			// supported by RFC)
			NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
			while (allAttributes.hasMoreElements()) {
				Attribute oneAttribute = allAttributes.nextElement();
				String encodedAttributeName = nameEncodeForUrl(oneAttribute.getID());

				// Loop through all values of the attribute (usually just one, but more
				// are supported by RFC)
				NamingEnumeration<?> allValues;
				try {
					allValues = oneAttribute.getAll();
				}
				catch (NamingException ex) {
					throw new UncategorizedLdapException("Unexpected error occurred formatting base URL", ex);
				}

				while (allValues.hasMoreElements()) {
					sb.append(encodedAttributeName).append('=');

					Object oneValue = allValues.nextElement();
					if (oneValue instanceof String) {
						String oneString = (String) oneValue;
						sb.append(nameEncodeForUrl(oneString));
					}
					else {
						throw new IllegalArgumentException("Binary attributes not supported for base URL");
					}

					if (allValues.hasMoreElements()) {
						sb.append('+');
					}
				}
				if (allAttributes.hasMoreElements()) {
					sb.append('+');
				}
			}

			if (it.hasPrevious()) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	static String nameEncodeForUrl(String value) {
		try {
			String ldapEncoded = LdapEncoder.nameEncode(value);
			URI valueUri = new URI(null, null, ldapEncoded, null);
			return valueUri.toString();
		}
		catch (URISyntaxException ex) {
			throw new UncategorizedLdapException("This really shouldn't happen - report this", ex);
		}
	}

	/**
	 * Set the base suffix from which all operations should origin. If a base suffix is
	 * set, you will not have to (and, indeed, must not) specify the full distinguished
	 * names in any operations performed.
	 * @param base the base suffix.
	 */
	public void setBase(String base) {
		if (base != null) {
			this.base = LdapUtils.newLdapName(base);
		}
		else {
			this.base = LdapUtils.emptyLdapName();
		}
	}

	/**
	 * @deprecated {@link DistinguishedName} and associated classes and methods are
	 * deprecated as of 2.0.
	 */
	@Override
	public DistinguishedName getBaseLdapPath() {
		return new DistinguishedName(this.base);
	}

	@Override
	public LdapName getBaseLdapName() {
		return (LdapName) this.base.clone();
	}

	@Override
	public String getBaseLdapPathAsString() {
		return getBaseLdapName().toString();
	}

	/**
	 * Create a DirContext using the supplied environment.
	 * @param environment the LDAP environment to use when creating the
	 * <code>DirContext</code>.
	 * @return a new DirContext implementation initialized with the supplied environment.
	 */
	protected DirContext createContext(Hashtable<String, Object> environment) {
		DirContext ctx = null;

		try {
			ctx = getDirContextInstance(environment);

			if (LOG.isInfoEnabled()) {
				Hashtable<?, ?> ctxEnv = ctx.getEnvironment();
				String ldapUrl = (String) ctxEnv.get(Context.PROVIDER_URL);
				LOG.debug("Got Ldap context on server '" + ldapUrl + "'");
			}

			return ctx;
		}
		catch (NamingException ex) {
			closeContext(ctx);
			throw LdapUtils.convertLdapException(ex);
		}
	}

	/**
	 * Set the context factory. Default is com.sun.jndi.ldap.LdapCtxFactory.
	 * @param contextFactory the context factory used when creating Contexts.
	 */
	public void setContextFactory(Class<?> contextFactory) {
		this.contextFactory = contextFactory;
	}

	/**
	 * Get the context factory.
	 * @return the context factory used when creating Contexts.
	 */
	public Class<?> getContextFactory() {
		return this.contextFactory;
	}

	/**
	 * Set the DirObjectFactory to use. Default is {@link DefaultDirObjectFactory}. The
	 * specified class needs to be an implementation of javax.naming.spi.DirObjectFactory.
	 * <b>Note: </b>Setting this value to null may have cause connection leaks when using
	 * ContextMapper methods in LdapTemplate.
	 * @param dirObjectFactory the DirObjectFactory to be used. Null means that no
	 * DirObjectFactory will be used.
	 */
	public void setDirObjectFactory(Class<?> dirObjectFactory) {
		this.dirObjectFactory = dirObjectFactory;
	}

	/**
	 * Get the DirObjectFactory to use.
	 * @return the DirObjectFactory to be used. <code>null</code> means that no
	 * DirObjectFactory will be used.
	 */
	public Class<?> getDirObjectFactory() {
		return this.dirObjectFactory;
	}

	/**
	 * Checks that all necessary data is set and that there is no compatibility issues,
	 * after which the instance is initialized. Note that you need to call this method
	 * explicitly after setting all desired properties if using the class outside of a
	 * Spring Context.
	 */
	public void afterPropertiesSet() {
		if (ObjectUtils.isEmpty(this.urls)) {
			throw new IllegalArgumentException("At least one server url must be set");
		}
		if (this.contextFactory == null) {
			throw new IllegalArgumentException("contextFactory must be set");
		}
		if (this.authenticationSource == null) {
			LOG.debug("AuthenticationSource not set - " + "using default implementation");
			if (!StringUtils.hasText(this.userDn)) {
				LOG.info("Property 'userDn' not set - " + "anonymous context will be used for read-write operations");
				this.anonymousReadOnly = true;
			}
			if (!this.anonymousReadOnly) {
				if (this.password == null) {
					throw new IllegalArgumentException(
							"Property 'password' cannot be null. To use a blank password, please ensure it is set to \"\"");
				}
				if (!StringUtils.hasText(this.password)) {
					LOG.info("Property 'password' not set - " + "blank password will be used");
				}
			}
			this.authenticationSource = new SimpleAuthenticationSource();
		}

		if (this.cacheEnvironmentProperties) {
			this.anonymousEnv = setupAnonymousEnv();
		}
	}

	@SuppressWarnings("deprecation")
	private Hashtable<String, Object> setupAnonymousEnv() {
		if (this.pooled) {
			this.baseEnv.put(SUN_LDAP_POOLING_FLAG, "true");
			LOG.debug("Using LDAP pooling.");
		}
		else {
			this.baseEnv.remove(SUN_LDAP_POOLING_FLAG);
			LOG.debug("Not using LDAP pooling");
		}

		Hashtable<String, Object> env = new Hashtable<String, Object>(this.baseEnv);

		env.put(Context.INITIAL_CONTEXT_FACTORY, this.contextFactory.getName());
		env.put(Context.PROVIDER_URL, assembleProviderUrlString(this.urls));

		if (this.dirObjectFactory != null) {
			env.put(Context.OBJECT_FACTORIES, this.dirObjectFactory.getName());
		}

		if (StringUtils.hasText(this.referral)) {
			env.put(Context.REFERRAL, this.referral);
		}

		if (!this.base.isEmpty()) {
			// Save the base path for use in the DefaultDirObjectFactory.
			env.put(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY, this.base);
		}

		LOG.debug("Trying provider Urls: " + assembleProviderUrlString(this.urls));

		return env;
	}

	/**
	 * Set the password (credentials) to use for getting authenticated contexts.
	 * @param password the password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the password (credentials) to use for getting authenticated contexts.
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Set the user distinguished name (principal) to use for getting authenticated
	 * contexts.
	 * @param userDn the user distinguished name.
	 */
	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

	/**
	 * Gets the user distinguished name (principal) to use for getting authenticated
	 * contexts.
	 * @return the user distinguished name.
	 */
	public String getUserDn() {
		return this.userDn;
	}

	/**
	 * Set the urls of the LDAP servers. Use this method if several servers are required.
	 * @param urls the urls of all servers.
	 */
	public void setUrls(String[] urls) {
		this.urls = urls.clone();
	}

	/**
	 * Get the urls of the LDAP servers.
	 * @return the urls of all servers.
	 */
	public String[] getUrls() {
		return this.urls.clone();
	}

	/**
	 * Set the url of the LDAP server. Utility method if only one server is used.
	 * @param url the url of the LDAP server.
	 */
	public void setUrl(String url) {
		this.urls = new String[] { url };
	}

	/**
	 * Set whether the pooling flag should be set, enabling the built-in LDAP connection
	 * pooling. Default is <code>false</code>. The built-in LDAP connection pooling
	 * suffers from a number of deficiencies, e.g. no connection validation. Also,
	 * enabling this flag when using TLS connections will explicitly not work. Consider
	 * using the Spring LDAP <code>PoolingContextSource</code> as an alternative instead
	 * of enabling this flag.
	 * <p>
	 * Note that since LDAP pooling is system wide, full configuration of this needs be
	 * done using system parameters as specified in the LDAP/JNDI documentation. Also
	 * note, that pooling is done on user dn basis, i.e. each individually authenticated
	 * connection will be pooled separately. This means that LDAP pooling will be most
	 * efficient using anonymous connections or connections authenticated using one single
	 * system user.
	 * @param pooled whether Contexts should be pooled.
	 */
	public void setPooled(boolean pooled) {
		this.pooled = pooled;
	}

	/**
	 * Get whether the pooling flag should be set.
	 * @return whether Contexts should be pooled.
	 */
	public boolean isPooled() {
		return this.pooled;
	}

	/**
	 * If any custom environment properties are needed, these can be set using this
	 * method.
	 * @param baseEnvironmentProperties the base environment properties that should always
	 * be used when creating new Context instances.
	 */
	public void setBaseEnvironmentProperties(Map<String, Object> baseEnvironmentProperties) {
		this.baseEnv = new Hashtable<String, Object>(baseEnvironmentProperties);
	}

	protected Hashtable<String, Object> getAnonymousEnv() {
		if (this.cacheEnvironmentProperties) {
			return this.anonymousEnv;
		}
		else {
			return setupAnonymousEnv();
		}
	}

	protected Hashtable<String, Object> getAuthenticatedEnv(String principal, String credentials) {
		// The authenticated environment should always be rebuilt.
		Hashtable<String, Object> env = new Hashtable<String, Object>(getAnonymousEnv());
		setupAuthenticatedEnvironment(env, principal, credentials);
		return env;
	}

	/**
	 * Set the authentication source to use when retrieving user principal and
	 * credentials.
	 * @param authenticationSource the {@link AuthenticationSource} that will provide user
	 * info.
	 */
	public void setAuthenticationSource(AuthenticationSource authenticationSource) {
		this.authenticationSource = authenticationSource;
	}

	/**
	 * Get the authentication source.
	 * @return the {@link AuthenticationSource} that will provide user info.
	 */
	public AuthenticationSource getAuthenticationSource() {
		return this.authenticationSource;
	}

	/**
	 * Set whether environment properties should be cached between requsts for anonymous
	 * environment. Default is <code>true</code>; setting this property to
	 * <code>false</code> causes the environment Hashmap to be rebuilt from the current
	 * property settings of this instance between each request for an anonymous
	 * environment.
	 * @param cacheEnvironmentProperties <code>true</code> causes that the anonymous
	 * environment properties should be cached, <code>false</code> causes the Hashmap to
	 * be rebuilt for each request.
	 */
	public void setCacheEnvironmentProperties(boolean cacheEnvironmentProperties) {
		this.cacheEnvironmentProperties = cacheEnvironmentProperties;
	}

	/**
	 * Set whether an anonymous environment should be used for read-only operations.
	 * Default is <code>false</code>.
	 * @param anonymousReadOnly <code>true</code> if an anonymous environment should be
	 * used for read-only operations, <code>false</code> otherwise.
	 */
	public void setAnonymousReadOnly(boolean anonymousReadOnly) {
		this.anonymousReadOnly = anonymousReadOnly;
	}

	/**
	 * Get whether an anonymous environment should be used for read-only operations.
	 * @return <code>true</code> if an anonymous environment should be used for read-only
	 * operations, <code>false</code> otherwise.
	 */
	public boolean isAnonymousReadOnly() {
		return this.anonymousReadOnly;
	}

	/**
	 * Set the {@link DirContextAuthenticationStrategy} to use for preparing the
	 * environment and processing the created <code>DirContext</code> instances.
	 * @param authenticationStrategy the {@link DirContextAuthenticationStrategy} to use;
	 * default is {@link SimpleDirContextAuthenticationStrategy}.
	 */
	public void setAuthenticationStrategy(DirContextAuthenticationStrategy authenticationStrategy) {
		this.authenticationStrategy = authenticationStrategy;
	}

	/**
	 * Set the method to handle referrals. Default is 'ignore'; setting this flag to
	 * 'follow' will enable referrals to be automatically followed. Note that this might
	 * require particular name server setup in order to work (the referred URLs will need
	 * to be automatically found using standard DNS resolution).
	 * @param referral the value to set the system property <code>Context.REFERRAL</code>
	 * to, customizing the way that referrals are handled.
	 */
	public void setReferral(String referral) {
		this.referral = referral;
	}

	/**
	 * Implement in subclass to create a DirContext of the desired type (e.g.
	 * InitialDirContext or InitialLdapContext).
	 * @param environment the environment to use when creating the instance.
	 * @return a new DirContext instance.
	 * @throws NamingException if one is encountered when creating the instance.
	 */
	protected abstract DirContext getDirContextInstance(Hashtable<String, Object> environment) throws NamingException;

	class SimpleAuthenticationSource implements AuthenticationSource {

		@Override
		public String getPrincipal() {
			return AbstractContextSource.this.userDn;
		}

		@Override
		public String getCredentials() {
			return AbstractContextSource.this.password;
		}

	}

}
