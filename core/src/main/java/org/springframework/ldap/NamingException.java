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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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

    private Throwable cause;

    /**
     * Overrides {@link NestedRuntimeException#getCause()} since serialization
     * always tries to serialize the base class before the subclass. Our
     * <tt>cause</tt> may have a <tt>resolvedObj</tt> that is not
     * serializable. By storing the cause in this class, we get a chance at
     * temporarily nulling the cause before serialization, thus in effect making
     * the current instance serializable.
     */
    public Throwable getCause() {
        // Even if you cannot set the cause of this exception other than through
        // the constructor, we check for the cause being "this" here, as the cause
        // could still be set to "this" via reflection: for example, by a remoting
        // deserializer like Hessian's.
        return (this.cause == this ? null : this.cause);
    }

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
        super(msg);
        this.cause = cause;
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
        this(cause != null ? cause.getMessage() : null, cause);
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

    /**
     * Checks if the <tt>resolvedObj</tt> of the causing exception is
     * suspected to be non-serializable, and if so temporarily nulls it before
     * calling the default serialization mechanism.
     * 
     * @param stream
     *            the stream onto which this object is serialized
     * @throws IOException
     *             if there is an error writing this object to the stream
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        Object resolvedObj = getResolvedObj();
        boolean serializable = resolvedObj instanceof Serializable;
        if (resolvedObj != null && !serializable) {
            // the cause is of this type, since resolvedObj is not null
            javax.naming.NamingException namingException = (javax.naming.NamingException) getCause();
            namingException.setResolvedObj(null);
            try {
                stream.defaultWriteObject();
            } finally {
                namingException.setResolvedObj(resolvedObj);
            }
        } else {
            stream.defaultWriteObject();
        }
    }
}
