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

package org.springframework.ldap.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirObjectFactory;

/**
 * Default implementation of the DirObjectFactory interface. Creates a
 * DirContextAdapter from the supplied arguments.
 * 
 * @author Mattias Arthursson
 */
public class DefaultDirObjectFactory implements DirObjectFactory {
    /**
     * Key to use in the ContextSource implementation to store the value of the
     * base path suffix, if any, in the Ldap Environment.
     */
    public static final String JNDI_ENV_BASE_PATH_KEY = "org.springframework.ldap.base.path";

    /**
     * Creates a DirContextAdapter from the supplied arguments.
     * 
     * @param obj
     * @param name
     * @param nameCtx
     * @param environment
     * @param attrs
     * @return a new DirContextAdapter from the attributes and name.
     * @throws Exception
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable environment, Attributes attrs) throws Exception {

        try {
            DirContextAdapter dirContextAdapter = new DirContextAdapter(attrs,
                    name, (Name) environment.get(JNDI_ENV_BASE_PATH_KEY));
            dirContextAdapter.setUpdateMode(true);

            return dirContextAdapter;
        } finally {
            // It seems that the object supplied to the obj parameter is a
            // DirContext instance with reference to the same Ldap connection as
            // the original context. Since it is not the same instance (that's
            // the nameCtx parameter) this one really needs to be closed in
            // order to correctly clean up and return the connection to the pool
            // when we're finished with the surrounding operation.
            if (obj instanceof Context) {
                Context ctx = (Context) obj;
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this
                }

            }
        }
    }

    /**
     * Returns null.
     * 
     * @param obj
     * @param name
     * @param nameCtx
     * @param environment
     * @return null.
     * @throws Exception
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable environment) throws Exception {
        return null;
    }

}
