package org.springframework.ldap.core.support;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.support.LdapUtils;

public abstract class AbstractTlsDirContextAuthenticationStrategy implements DirContextAuthenticationStrategy {

	private HostnameVerifier hostnameVerifier;

	private boolean shutdownTlsGracefully = false;

	public void setShutdownTlsGracefully(boolean shutdownTlsGracefully) {
		this.shutdownTlsGracefully = shutdownTlsGracefully;
	}

	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	public final void setupEnvironment(Hashtable env, String userDn, String password) {
		// Nothing to do in this implementation - authentication should take
		// place after TLS has been negotiated.
	}

	public final DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
			throws NamingException {

		if (ctx instanceof LdapContext) {
			LdapContext ldapCtx = (LdapContext) ctx;
			StartTlsResponse tlsResponse = (StartTlsResponse) ldapCtx.extendedOperation(new StartTlsRequest());
			try {
				if (hostnameVerifier != null) {
					tlsResponse.setHostnameVerifier(hostnameVerifier);
				}
				tlsResponse.negotiate();
				applyAuthentication(ldapCtx, userDn, password);

				if (shutdownTlsGracefully) {
					// Wrap the target context in a proxy to intercept any calls
					// to 'close', so that we can shut down the TLS connection
					// gracefully first.
					return (DirContext) Proxy.newProxyInstance(DirContextProxy.class.getClassLoader(), new Class[] {
							LdapContext.class, DirContextProxy.class }, new TlsAwareDirContextProxy(ldapCtx,
							tlsResponse));
				}
				else {
					return ctx;
				}
			}
			catch (IOException e) {
				LdapUtils.closeContext(ctx);
				throw new UncategorizedLdapException("Failed to negitiate tls session", e);
			}
		}
		else {
			throw new IllegalArgumentException(
					"Processed Context must be an LDAPv3 context, i.e. an LdapContext implementation");
		}

	}

	/**
	 * Apply the actual authentication to the specified <code>LdapContext</code>.
	 * Typically, this will involve adding stuff to the environment.
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
