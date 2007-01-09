/*
 * Copyright 2002-2006 the original author or authors.
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

import javax.naming.Name;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for exception thrown by the framework whenever it encounters a
 * problem related to LDAP.
 * 
 * @author Ulrik Sandberg
 * @since 1.2
 */
public abstract class NamingException extends NestedRuntimeException {

    /**
     * Constructor that takes a message.
     * 
     * @param msg
     *            the detail message
     */
    public NamingException(String msg) {
        super(msg);
    }

    /**
     * Constructor that allows a message and a root cause.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the cause of the exception. This argument is generally
     *            expected to be a proper subclass of
     *            {@link javax.naming.NamingException}.
     */
    public NamingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructor that allows a plain root cause, intended for subclasses
     * mirroring corresponding <code>javax.naming</code> exceptions.
     * 
     * @param cause
     *            the cause of the exception. This argument is generally
     *            expected to be a proper subclass of
     *            {@link javax.naming.NamingException}.
     */
    public NamingException(Throwable cause) {
        super(cause != null ? cause.getMessage() : null, cause);
    }

    /**
     * Convenience method to get the explanation associated with this exception,
     * if the root cause was an instance of {@link javax.naming.NamingException}.
     * 
     * @return a detail string explaining more about this exception if the root
     *         cause is an instance of javax.naming.NamingException, or
     *         <code>null</code> if there is no detail message for this
     *         exception
     */
    public String getExplanation() {
        if (getCause() instanceof javax.naming.NamingException) {
            return ((javax.naming.NamingException) getCause()).getExplanation();
        }
        return null;
    }

    /**
     * Convenience method to get the unresolved part of the name associated with
     * this exception, if the root cause was an instance of
     * {@link javax.naming.NamingException}.
     * 
     * @return a composite name describing the part of the name that has not
     *         been resolved if the root cause is an instance of
     *         javax.naming.NamingException, or <code>null</code> if the
     *         remaining name field has not been set
     */
    public Name getRemainingName() {
        if (getCause() instanceof javax.naming.NamingException) {
            return ((javax.naming.NamingException) getCause())
                    .getRemainingName();
        }
        return null;
    }

    /**
     * Convenience method to get the leading portion of the resolved name
     * associated with this exception, if the root cause was an instance of
     * {@link javax.naming.NamingException}.
     * 
     * @return a composite name describing the the leading portion of the name
     *         that was resolved successfully if the root cause is an instance
     *         of javax.naming.NamingException, or <code>null</code> if the
     *         resolved name field has not been set
     */
    public Name getResolvedName() {
        if (getCause() instanceof javax.naming.NamingException) {
            return ((javax.naming.NamingException) getCause())
                    .getResolvedName();
        }
        return null;
    }

    /**
     * Convenience method to get the resolved object associated with this
     * exception, if the root cause was an instance of
     * {@link javax.naming.NamingException}.
     * 
     * @return the object that was resolved so far if the root cause is an
     *         instance of javax.naming.NamingException, or <code>null</code>
     *         if the resolved object field has not been set
     */
    public Object getResolvedObj() {
        if (getCause() instanceof javax.naming.NamingException) {
            return ((javax.naming.NamingException) getCause()).getResolvedObj();
        }
        return null;
    }
}
