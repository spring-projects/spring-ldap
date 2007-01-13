/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.support.transaction;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapRdnComponent;

/**
 * A {@link CompensatingTransactionRecordingOperation} keeping track of a rebind
 * operation. Creates {@link RebindRollbackOperation} objects in
 * {@link #recordOperation(Object[])}.
 * 
 * @author Mattias Arthursson
 */
public class RebindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private static Log log = LogFactory.getLog(RebindRecordingOperation.class);

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for getting the rollback
     *            information and supply to the {@link RebindRollbackOperation}.
     */
    public RebindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRecordingOperation#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionRollbackOperation recordOperation(
            Object[] args) {
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid arguments for bind operation");
        }
        Name dn = LdapUtils.getFirstArgumentAsName(args);
        Object object = args[1];
        Attributes attributes = null;
        if (args[2] != null && !(args[2] instanceof Attributes)) {
            throw new IllegalArgumentException(
                    "Invalid third argument to bind operation");
        } else if (args[2] != null) {
            attributes = (Attributes) args[2];
        }

        Name temporaryName = getTemporaryName(dn);

        ldapOperations.rename(dn, temporaryName);
        return new RebindRollbackOperation(ldapOperations, dn, temporaryName,
                object, attributes);
    }

    Name getTemporaryName(Name originalName) {
        DistinguishedName temporaryName = new DistinguishedName(originalName);
        List names = temporaryName.getNames();
        LdapRdn rdn = (LdapRdn) names.get(names.size() - 1);
        LdapRdnComponent rdnComponent = rdn.getComponent();
        String value = rdnComponent.getValue();
        rdnComponent.setValue(value + "_temp");

        return temporaryName;
    }

    /**
     * Get the LdapOperations. For testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
