/*
 * Copyright 2005-2013 the original author or authors.
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

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

/**
 * Abstract superclass for {@link DirContextAuthenticationStrategy}
 * implementations that apply TLS security to the connections. The supported TLS
 * behavior differs between servers. E.g., some servers expect the TLS
 * connection be shut down gracefully before the actual target context is
 * closed, whereas other servers do not support that. The
 * <code>shutdownTlsGracefully</code> property controls this behavior; the
 * property defaults to <code>false</code>.
 * <p>
 * The <code>SSLSocketFactory</code> used for TLS negotiation can be customized
 * using the <code>sslSocketFactory</code> property. This allows for example a
 * socket factory that can load the keystore/truststore using the Spring
 * Resource abstraction. This provides a much more Spring-like strategy for
 * configuring PKI credentials for authentication, in addition to allowing
 * application-specific keystores and truststores running in the same JVM.
 * <p>
 * In some rare occasions there is a need to supply a
 * <code>HostnameVerifier</code> to the TLS processing instructions in order to
 * have the returned certificate properly validated. If a
 * <code>HostnameVerifier</code> is supplied to
 * {@link #setHostnameVerifier(HostnameVerifier)}, that will be applied to the
 * processing.
 * <p>
 * For further information regarding TLS, refer to <a
 * href="http://java.sun.com/products/jndi/tutorial/ldap/ext/starttls.html">this
 * page</a>.
 * <p>
 * <b>NB:</b> TLS negotiation is an expensive process, which is why you will
 * most likely want to use connection pooling, to make sure new connections are
 * not created for each individual request. It is imperative however, that the
 * built-in LDAP connection pooling is not used in combination with the TLS
 * AuthenticationStrategy implementations - this will not work. You should use
 * the Spring LDAP PoolingContextSource instead.
 * 
 * @author Mattias Hellborg Arthursson
 */
public abstract class AbstractTlsDirContextAuthenticationStrategy implements DirContextAuthenticationStrategy {

	/** Hostname verifier to use for cert subject validation */
	private HostnameVerifier hostnameVerifier;

	/** Flag to cause graceful shutdown required by some LDAP DSAs */
	private boolean shutdownTlsGracefully = false;

	/** SSL socket factory to use for startTLS negotiation */
    private SSLSocketFactory sslSocketFactory;
    
	/**
	 * Specify whether the TLS should be shut down gracefully before the target
	 * context is closed. Defaults to <code>false</code>.
	 * 
	 * @param shutdownTlsGracefully <code>true</code> to shut down the TLS
	 * connection explicitly, <code>false</code> closes the target context
	 * immediately.
	 */
	public void setShutdownTlsGracefully(boolean shutdownTlsGracefully) {
		this.shutdownTlsGracefully = shutdownTlsGracefully;
	}

	/**
	 * Set the optional
	 * <code>HostnameVerifier</code> to use for verifying incoming certificates. Defaults to <code>null</code>
	 * , meaning that the default hostname verification will take place.
	 * 
	 * @param hostnameVerifier The <code>HostnameVerifier</code> to use, if any.
	 */
	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

    /**
     * Sets the optional SSL socket factory used for startTLS negotiation.
     * Defaults to <code>null</code> to indicate that the default socket factory
     * provided by the underlying JSSE provider should be used.
     * @param sslSocketFactory SSL socket factory to use, if any.
     */
    public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }
    
	/* (non-Javadoc)
	 * @see org.springframework.ldap.core.support.DirContextAuthenticationStrategy#setupEnvironment(java.util.Hashtable, java.lang.String, java.lang.String)
	 */
	public final void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) {
		// Nothing to do in this implementation - authentication should take
		// place after TLS has been negotiated.
	}

	/* (non-Javadoc)
	 * @see org.springframework.ldap.core.support.DirContextAuthenticationStrategy#processContextAfterCreation(javax.naming.directory.DirContext, java.lang.String, java.lang.String)
	 */
	public final DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
			throws NamingException {

		if (ctx instanceof LdapContext) {
			final LdapContext ldapCtx = (LdapContext) ctx;
			final StartTlsResponse tlsResponse = (StartTlsResponse) ldapCtx.extendedOperation(new StartTlsRequest());
			try {
				if (hostnameVerifier != null) {
					tlsResponse.setHostnameVerifier(hostnameVerifier);
				}
				tlsResponse.negotiate(sslSocketFactory); // If null, the default SSL socket factory is used
				applyAuthentication(ldapCtx, userDn, password);

				if (shutdownTlsGracefully) {
					// Wrap the target context in a proxy to intercept any calls
					// to 'close', so that we can shut down the TLS connection
					// gracefully first.
					return (DirContext) Proxy.newProxyInstance(DirContextProxy.class.getClassLoader(), new Class<?>[] {
							LdapContext.class, DirContextProxy.class }, new TlsAwareDirContextProxy(ldapCtx,
							tlsResponse));
				}
				else {
					return ctx;
				}
			}
			catch (IOException e) {
				LdapUtils.closeContext(ctx);
				throw new UncategorizedLdapException("Failed to negotiate TLS session", e);
			}
		}
		else {
			throw new IllegalArgumentException(
					"Processed Context must be an LDAPv3 context, i.e. an LdapContext implementation");
		}

	}

	/**
	 * Apply the actual authentication to the specified <code>LdapContext</code>
	 * . Typically, this will involve adding stuff to the environment.
	 * 
	 * @param ctx the <code>LdapContext</code> instance.
	 * @param userDn the user dn of the user to authenticate.
	 * @param password the password of the user to authenticate.
	 * @throws NamingException if any error occurs.
	 */
	protected abstract void applyAuthentication(LdapContext ctx, String userDn, String password) throws NamingException;

	private static final class TlsAwareDirContextProxy implements DirContextProxy, InvocationHandler {

		private static final String GET_TARGET_CONTEXT_METHOD_NAME = "getTargetContext";

		private static final String CLOSE_METHOD_NAME = "close";

		private final LdapContext target;

		private final StartTlsResponse tlsResponse;

		public TlsAwareDirContextProxy(LdapContext target, StartTlsResponse tlsResponse) {
			this.target = target;
			this.tlsResponse = tlsResponse;
		}

		public DirContext getTargetContext() {
			return target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals(CLOSE_METHOD_NAME)) {
				tlsResponse.close();
				return method.invoke(target, args);
			}
			else if (method.getName().equals(GET_TARGET_CONTEXT_METHOD_NAME)) {
				return target;
			}
			else {
				return method.invoke(target, args);
			}
		}
	}
}
