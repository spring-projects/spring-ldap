/*
 * Copyright 2005-2007 the original author or authors.
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

package org.springframework.ldap.support;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * ContextSource implementation which overrides setupAuthenticatedEnvironment to
 * allow keystore authentication.
 * 
 * @author Ben Surgison
 */
public class TlsContextSource extends LdapContextSource {

    private static final Log log = LogFactory.getLog(TlsContextSource.class);

    private static final String KEYSTORE = "javax.net.ssl.keyStore";

    private static final String KEYSTORETYPE = "javax.net.ssl.keyStoreType";

    private static final String KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";

    private static final String TRUSTSTORE = "javax.net.ssl.trustStore";

    private static final String TRUSTSTORETYPE = "javax.net.ssl.trustStoreType";

    private static final String TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";

    private String keyStore;

    private String keyStoreType;

    private String keyStorePassword;

    private String trustStore;

    private String trustStoreType;

    private String trustStorePassword;

    /**
     * Overridden implementation to allow TLS to be started
     */
    public DirContext getReadOnlyContext() {
        LdapContext ctx = (LdapContext) super.getReadOnlyContext();

        // only start tls if keyStore has been set.
        if (this.keyStore == null && this.keyStore.length() > 0) {
            // Start TLS
            StartTlsResponse tls;
            try {
                tls = (StartTlsResponse) ctx
                        .extendedOperation(new StartTlsRequest());
                tls.negotiate();
            } catch (NamingException ne) {
                log.error(ne.getLocalizedMessage(), ne);
            } catch (IOException ioe) {
                log.error(ioe.getLocalizedMessage(), ioe);
            }
        }
        return ctx;
    }

    /**
     * Overridden implementation of setting the environment up to be
     * authenticated. This allows a keyStore certificate to be used for SASL
     * security.
     * 
     * @param env
     *            the environment to modify.
     */
    protected void setupAuthenticatedEnvironment(Hashtable env) {

        // revert to original if keyStore hasn't been set.
        if (this.keyStore == null && this.keyStore.length() > 0) {
            super.setupAuthenticatedEnvironment(env);
            return;
        }

        // Setup keyStore system properties.
        System.setProperty(KEYSTORE, getMyQualifiedName(this.keyStore));
        System.setProperty(KEYSTOREPASSWORD, this.keyStorePassword);
        System.setProperty(KEYSTORETYPE, getMyKeyStoreType(this.keyStoreType));

        // Setup trustStore system properties.
        System.setProperty(TRUSTSTORE, getMyQualifiedName(this.trustStore));
        System.setProperty(TRUSTSTOREPASSWORD, this.trustStorePassword);
        System.setProperty(TRUSTSTORETYPE,
                getMyKeyStoreType(this.trustStoreType));
    }

    private String getMyKeyStoreType(String keyStoreType) {
        if (keyStoreType != null && keyStoreType.length() > 0) {
            return keyStoreType;
        }
        return KeyStore.getDefaultType();
    }

    private String getMyQualifiedName(String filename) {
        String qualifiedName = null;
        if (filename.indexOf("/") > -1 || filename.indexOf("\\") > -1) {
            qualifiedName = filename; // use full name as entered
        } else {
            try {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                qualifiedName = classLoader.getResource(filename)
                        .toExternalForm(); // get full name from classpath
                qualifiedName = qualifiedName.substring(6);
            } catch (Exception e) {
                log.error("Missing file: " + filename, e);
            }
        }
        return qualifiedName;
    }

    /**
     * @param keyStore
     *            the keyStore to set
     */
    public void setKeyStore(String keyStore) {

        this.keyStore = keyStore;
    }

    /**
     * @param passPhrase
     *            the keyStore password to set
     */
    public void setKeyStorePassword(String passPhrase) {
        this.keyStorePassword = passPhrase;
    }

    /**
     * @param keyStoreType
     *            the keyStoreType to set
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * @param trustStore
     *            the trustStore to set
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * @param trustStorePassword
     *            the trustStorePassword to set
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * @param trustStoreType
     *            the trustStoreType to set
     */
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}
