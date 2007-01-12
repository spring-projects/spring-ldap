/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.ldap.core.support;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.JdkVersion;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DefaultDirObjectFactory;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;

/**
 * Abstract implementation of the ContextSource interface. By default, returns
 * an authenticated DirContext implementation for both read-only and read-write
 * operations. To have an anonymous environment created for read-only
 * operations, set the anonymousReadOnly property to <code>true</code>.
 * <p>
 * Implementing classes need to implement
 * {@link #getDirContextInstance(Hashtable)} to create a DirContext instance of
 * the desired type.
 * <p>
 * If an AuthenticationSource is set, this will be used for getting user name
 * and password for each new connection, otherwise a default one will be created
 * using the specified userDn and password.
 * <p>
 * <b>Note:</b> When using implementations of this class outside of a Spring
 * Context it is necessary to call {@link #afterPropertiesSet()} when all
 * properties are set, in order to finish up initialization.
 * 
 * @see org.springframework.ldap.core.LdapTemplate
 * @see org.springframework.ldap.core.DefaultDirObjectFactory
 * @see org.springframework.ldap.core.support.LdapContextSource
 * @see org.springframework.ldap.core.support.DirContextSource
 * 
 * @author Mattias Arthursson
 * @author Adam Skogman
 * @author Ulrik Sandberg
 */
public abstract class AbstractContextSource implements ContextSource,
        InitializingBean {

    private static final Class DEFAULT_CONTEXT_FACTORY = com.sun.jndi.ldap.LdapCtxFactory.class;

    private static final Class DEFAULT_DIR_OBJECT_FACTORY = DefaultDirObjectFactory.class;

    private Class dirObjectFactory = DEFAULT_DIR_OBJECT_FACTORY;

    private Class contextFactory = DEFAULT_CONTEXT_FACTORY;

    private DistinguishedName base;

    protected String userDn = "";

    protected String password = "";

    private String[] urls;

    private boolean pooled = true;

    private Hashtable baseEnv = new Hashtable();

    private Hashtable anonymousEnv;

    private AuthenticationSource authenticationSource;

    private boolean cacheEnvironmentProperties = true;

    private boolean anonymousReadOnly = false;

    private static final Log log = LogFactory.getLog(LdapContextSource.class);

    public static final String SUN_LDAP_POOLING_FLAG = "com.sun.jndi.ldap.connect.pool";

    private static final String JDK_142 = "1.4.2";

    public DirContext getReadOnlyContext() {
        if (!anonymousReadOnly) {
            return createContext(getAuthenticatedEnv());
        } else {
            return createContext(getAnonymousEnv());
        }
    }

    public DirContext getReadWriteContext() {
        return createContext(getAuthenticatedEnv());
    }

    /**
     * Default implementation of setting the environment up to be authenticated.
     * Override in subclass if necessary. This is needed for Active Directory
     * connectivity, for example.
     * 
     * @param env
     *            the environment to modify.
     */
    protected void setupAuthenticatedEnvironment(Hashtable env) {
        env
                .put(Context.SECURITY_PRINCIPAL, authenticationSource
                        .getPrincipal());
        log.debug("Principal: '" + userDn + "'");
        env.put(Context.SECURITY_CREDENTIALS, authenticationSource
                .getCredentials());
    }

    /**
     * Close the context and swallow any exceptions.
     * 
     * @param ctx
     *            the DirContext to close.
     */
    private void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Assemble a valid url String from all registered urls to add as
     * PROVIDER_URL to the environment.
     * 
     * @param ldapUrls
     *            all individual url Strings.
     * @return the full url String
     */
    protected String assembleProviderUrlString(String[] ldapUrls) {
        StringBuffer providerUrlBuffer = new StringBuffer(1024);
        for (int i = 0; i < ldapUrls.length; i++) {
            providerUrlBuffer.append(ldapUrls[i]);
            if (base != null) {
                if (!ldapUrls[i].endsWith("/")) {
                    providerUrlBuffer.append("/");
                }
                providerUrlBuffer.append(base.toUrl());
            }
            providerUrlBuffer.append(' ');
        }
        return providerUrlBuffer.toString().trim();
    }

    /**
     * Set the base suffix from which all operations should origin. If a base
     * suffix is set, you will not have to (and, indeed, should not) specify the
     * full distinguished names in the operations performed.
     * 
     * @param base
     *            the base suffix.
     */
    public void setBase(String base) {
        this.base = new DistinguishedName(base);
    }

    /**
     * Create a DirContext using the supplied environment.
     * 
     * @param environment
     *            the Ldap environment to use when creating the DirContext.
     * @return a new DirContext implpementation initialized with the supplied
     *         environment.
     */
    DirContext createContext(Hashtable environment) {
        DirContext ctx = null;

        try {
            ctx = getDirContextInstance(environment);

            if (log.isInfoEnabled()) {
                Hashtable ctxEnv = ctx.getEnvironment();
                String ldapUrl = (String) ctxEnv.get(Context.PROVIDER_URL);
                log.debug("Got Ldap context on server '" + ldapUrl + "'");
            }

            return ctx;
        } catch (NamingException e) {
            closeContext(ctx);
            throw LdapUtils.convertLdapException(e);
        }
    }

    /**
     * Set the context factory. Default is com.sun.jndi.ldap.LdapCtxFactory.
     * 
     * @param contextFactory
     *            the context factory used when creating Contexts.
     */
    public void setContextFactory(Class contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * Set the DirObjectFactory to use. Default is
     * {@link DefaultDirObjectFactory}. The specified class needs to be an
     * implementation of javax.naming.spi.DirObjectFactory. <b>Note: </b>Setting
     * this value to null may have cause connection leaks when using
     * ContextMapper methods in LdapTemplate.
     * 
     * @param dirObjectFactory
     *            the DirObjectFactory to be used. Null means that no
     *            DirObjectFactory will be used.
     */
    public void setDirObjectFactory(Class dirObjectFactory) {
        this.dirObjectFactory = dirObjectFactory;
    }

    /**
     * Checks that all necessary data is set and that there is no compatibility
     * issues, after which the instance is initialized. Note that you need to
     * call this method explicitly after setting all desired properties if using
     * the class outside of a Spring Context.
     */
    public void afterPropertiesSet() throws Exception {
        if (ArrayUtils.isEmpty(urls)) {
            throw new IllegalArgumentException(
                    "At least one server url must be set");
        }

        if (base != null && getJdkVersion().compareTo(JDK_142) < 0) {
            throw new IllegalArgumentException(
                    "Base path is not supported for JDK versions < 1.4.2");
        }

        if (authenticationSource == null) {
            log.debug("AuthenticationSource not set - "
                    + "using default implementation");
            if (StringUtils.isBlank(userDn)) {
                log
                        .warn("Property 'userDn' not set - "
                                + "anonymous context will be used for read-write operations");
            } else if (StringUtils.isBlank(password)) {
                log.warn("Property 'password' not set - "
                        + "blank password will be used");
            }
            authenticationSource = new SimpleAuthenticationSource();
        }

        if (cacheEnvironmentProperties) {
            anonymousEnv = setupAnonymousEnv();
        }
    }

    private Hashtable setupAnonymousEnv() {
        if (pooled) {
            baseEnv.put(SUN_LDAP_POOLING_FLAG, "true");
            log.debug("Using LDAP pooling.");
        } else {
            log.debug("Not using LDAP pooling");
        }

        Hashtable env = new Hashtable(baseEnv);

        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory.getName());
        env.put(Context.PROVIDER_URL, assembleProviderUrlString(urls));

        if (dirObjectFactory != null) {
            env.put(Context.OBJECT_FACTORIES, dirObjectFactory.getName());
        }

        if (base != null) {
            // Save the base path for use in the DefaultDirObjectFactory.
            env.put(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY, base);
        }

        log.debug("Trying provider Urls: " + assembleProviderUrlString(urls));

        return env;
    }

    /**
     * Set the password (credentials) to use for getting authenticated contexts.
     * 
     * @param password
     *            the password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the user distinguished name (principal) to use for getting authenticated contexts.
     * 
     * @param userDn
     *            the user distinguished name.
     */
    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    /**
     * Set the user distinguished name (principal) to use for getting authenticated contexts.
     * 
     * @param userName
     *            the user distinguished name.
     * @deprecated Use {@link #setUserDn(String)} instead.
     */
    public void setUserName(String userName) {
        setUserDn(userName);
    }

    /**
     * Set the urls of the LDAP servers. Use this method if several servers are
     * required.
     * 
     * @param urls
     *            the urls of all servers.
     */
    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    /**
     * Set the url of the LDAP server. Utility method if only one server is
     * used.
     * 
     * @param url
     *            the url of the LDAP server.
     */
    public void setUrl(String url) {
        this.urls = new String[] { url };
    }

    /**
     * Set whether the pooling flag should be set. Default is true.
     * 
     * @param pooled
     *            whether Contexts should be pooled.
     */
    public void setPooled(boolean pooled) {
        this.pooled = pooled;
    }

    /**
     * If any custom environment properties are needed, these can be set using
     * this method.
     * 
     * @param baseEnvironmentProperties
     */
    public void setBaseEnvironmentProperties(Map baseEnvironmentProperties) {
        this.baseEnv = new Hashtable(baseEnvironmentProperties);
    }

    String getJdkVersion() {
        return JdkVersion.getJavaVersion();
    }

    protected Hashtable getAnonymousEnv() {
        if (cacheEnvironmentProperties) {
            return anonymousEnv;
        } else {
            return setupAnonymousEnv();
        }
    }

    protected Hashtable getAuthenticatedEnv() {
        // The authenticated environment should always be rebuilt.
        Hashtable env = new Hashtable(getAnonymousEnv());
        setupAuthenticatedEnvironment(env);
        return env;
    }

    public void setAuthenticationSource(
            AuthenticationSource authenticationProvider) {
        this.authenticationSource = authenticationProvider;
    }

    /**
     * Set whether environment properties should be cached between requsts for
     * anonymous environment. Default is true; setting this property to false
     * causes the environment Hashmap to be rebuilt from the current property
     * settings of this instance between each request for an anonymous
     * environment.
     * 
     * @param cacheEnvironmentProperties
     *            true causes that the anonymous environment properties should
     *            be cached, false causes the Hashmap to be rebuilt for each
     *            request.
     */
    public void setCacheEnvironmentProperties(boolean cacheEnvironmentProperties) {
        this.cacheEnvironmentProperties = cacheEnvironmentProperties;
    }

    /**
     * Set whether an anonymous environment should be used for read-only
     * operations. Default is <code>false</code>.
     * 
     * @param anonymousReadOnly
     *            <code>true</code> if and anonymous environment should be
     *            used for read-only operations, <code>false</code> otherwise.
     */
    public void setAnonymousReadOnly(boolean anonymousReadOnly) {
        this.anonymousReadOnly = anonymousReadOnly;
    }

    /**
     * Implement in subclass to create a DirContext of the desired type (e.g.
     * InitialDirContext or InitialLdapContext).
     * 
     * @param environment
     *            the environment to use when creating the instance.
     * @return a new DirContext instance.
     * @throws NamingException
     *             if one is encountered when creating the instance.
     */
    protected abstract DirContext getDirContextInstance(Hashtable environment)
            throws NamingException;

    class SimpleAuthenticationSource implements AuthenticationSource {

        public String getPrincipal() {
            return userDn;
        }

        public String getCredentials() {
            return password;
        }

    }

    protected String[] getUrls() {
        return urls;
    }
}
