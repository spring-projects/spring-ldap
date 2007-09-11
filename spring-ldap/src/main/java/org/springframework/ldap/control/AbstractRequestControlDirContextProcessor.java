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

package org.springframework.ldap.control;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextProcessor;

/**
 * Abstract superclass with responsibility to apply a single RequestControl on
 * an LdapContext, preserving any existing controls. Subclasses should implement
 * {@link DirContextProcessor#postProcess(DirContext)} and template method
 * {@link #createRequestControl()}.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public abstract class AbstractRequestControlDirContextProcessor implements
        DirContextProcessor {
    protected Log log = LogFactory
            .getLog(AbstractRequestControlDirContextProcessor.class);

    /**
     * Get the existing RequestControls from the LdapContext, call
     * {@link #createRequestControl()} to get a new instance, build a new array
     * of Controls and set it on the LdapContext.
     * <p>
     * The {@link Control} feature is specific for LDAP v3 and thus applies only
     * to {@link LdapContext}. However, the generic DirContextProcessor
     * mechanism used for calling <code>preProcess</code> and
     * <code>postProcess</code> uses DirContext, since it also works for LDAP
     * v2. This is the reason that DirContext has to be cast to a LdapContext.
     * 
     * @param ctx
     *            an LdapContext instance.
     * @throws NamingException
     * @throws IllegalArgumentException
     *             if the supplied DirContext is not an LdapContext.
     */
    public void preProcess(DirContext ctx) throws NamingException {
        LdapContext ldapContext;
        if (ctx instanceof LdapContext) {
            ldapContext = (LdapContext) ctx;
        } else {
            throw new IllegalArgumentException(
                    "Request Control operations require LDAPv3 - "
                            + "Context must be of type LdapContext");
        }

        Control[] requestControls = ldapContext.getRequestControls();
        if (requestControls == null) {
            requestControls = new Control[0];
        }
        Control newControl = createRequestControl();

        Control[] newControls = new Control[requestControls.length + 1];
        for (int i = 0; i < requestControls.length; i++) {
            newControls[i] = requestControls[i];
        }

        // Add the new Control at the end of the array.
        newControls[newControls.length - 1] = newControl;

        ldapContext.setRequestControls(newControls);
    }

    /**
     * Create an instance of the appropriate RequestControl.
     * 
     * @return the new instance.
     */
    public abstract Control createRequestControl();

}
