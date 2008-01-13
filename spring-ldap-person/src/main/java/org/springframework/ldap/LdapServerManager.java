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

package org.springframework.ldap;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.BaseLdapPathAware;

/**
 * Utility class to initialize the apache directory server. This means clearing
 * it of all entries, and initializing it with the startup ldif data.
 * 
 * @author Mattias Arthursson
 */
public class LdapServerManager implements InitializingBean, DisposableBean, BaseLdapPathAware {

    private ContextSource contextSource;

    private String ldifFile;
    
    private DistinguishedName baseLdapPath;

    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public void setLdifFile(String ldifFile) {
        this.ldifFile = ldifFile;
    }

    public void setBaseLdapPath(DistinguishedName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    public void destroy() throws Exception {
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                ServerContextFactory.class.getName());
        env.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
        env.setProperty(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.setProperty(Context.SECURITY_CREDENTIALS, "secret");

        ShutdownConfiguration configuration = new ShutdownConfiguration();
        env.putAll(configuration.toJndiEnvironment());

        new InitialContext(env);
    }

    public void afterPropertiesSet() throws Exception {
        DirContext ctx = contextSource.getReadWriteContext();

        // First of all, make sure the database is empty.
        Name startingPoint = null;

        // Different test cases have different base paths. This means that the
        // starting point will be different.
        if (baseLdapPath.size() != 0) {
            startingPoint = DistinguishedName.EMPTY_PATH;
        } else {
            startingPoint = new DistinguishedName(baseLdapPath);
        }

        try {
            clearSubContexts(ctx, startingPoint);
            // Load the ldif to the recently started server
            System.out.println("loading file '" + ldifFile + "'");
            LdifFileLoader loader = new LdifFileLoader(ctx, new File(ldifFile),
                    Collections.EMPTY_LIST, this.getClass().getClassLoader());
            loader.execute();
        } finally {
            ctx.close();
        }
    }

    private void clearSubContexts(DirContext ctx, Name name)
            throws NamingException {

        NamingEnumeration enumeration = null;
        try {
            enumeration = ctx.listBindings(name);
            while (enumeration.hasMore()) {
                Binding element = (Binding) enumeration.next();
                DistinguishedName childName = new DistinguishedName(element
                        .getName());
                childName.prepend((DistinguishedName) name);

                try {
                    ctx.destroySubcontext(childName);
                } catch (ContextNotEmptyException e) {
                    clearSubContexts(ctx, childName);
                    ctx.destroySubcontext(childName);
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            try {
                enumeration.close();
            } catch (Exception e) {
                // Never mind this
            }
        }
    }
}
