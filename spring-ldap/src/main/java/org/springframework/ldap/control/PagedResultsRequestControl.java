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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.util.ReflectionUtils;

import com.sun.jndi.ldap.ctl.PagedResultsControl;
import com.sun.jndi.ldap.ctl.PagedResultsResponseControl;

/**
 * DirContextProcessor implementation for managing the paged results control.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultsRequestControl extends
        AbstractRequestControlDirContextProcessor {

    private static final Class DEFAULT_RESPONSE_CONTROL = PagedResultsResponseControl.class;

    private static final boolean CRITICAL_CONTROL = true;

    private static final String JAVA5_RESPONSE_CONTROL = "javax.naming.ldap.PagedResultsResponseControl";

    private int pageSize;

    private PagedResultsCookie cookie;

    private int resultSize;

    private Class responseControlClass = DEFAULT_RESPONSE_CONTROL;

    private Class fallbackResponseControlClass;

    private Class currentResponseControlClass;

    /**
     * Constructs a new instance. This constructor should be used when
     * performing the first paged search operation, when no other results have
     * been retrieved.
     * 
     * @param pageSize
     *            the page size.
     */
    public PagedResultsRequestControl(int pageSize) {
        this(pageSize, null);
    }

    /**
     * Constructs a new instance with the supplied page size and cookie. The
     * cookie must be the exact same instance as received from a previous paged
     * resullts search, or <code>null</code> if it is the first in an
     * operation sequence.
     * 
     * @param pageSize
     *            the page size.
     * @param cookie
     *            the cookie, as received from a previous search.
     */
    public PagedResultsRequestControl(int pageSize, PagedResultsCookie cookie) {
        this.pageSize = pageSize;
        this.cookie = cookie;
        fallbackResponseControlClass = loadFallbackResponseControlClass();
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public PagedResultsCookie getCookie() {
        return cookie;
    }

    /**
     * Get the page size.
     * 
     * @return the page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get the total estimated number of entries that matches the issued search.
     * Note that this value is optional for the LDAP server to return, so it
     * does not always contain any valid data.
     * 
     * @return the estimated result size, if returned from the server.
     */
    public int getResultSize() {
        return resultSize;
    }

    /**
     * Set the class of the expected ResponseControl for the paged results
     * response. The default is {@link PagedResultsResponseControl}.
     * 
     * @param responseControlClass
     *            Class of the expected response control.
     */
    public void setResponseControlClass(Class responseControlClass) {
        this.responseControlClass = responseControlClass;
    }

    /*
     * @see org.springframework.ldap.control.AbstractRequestControlDirContextProcessor#createRequestControl()
     */
    public Control createRequestControl() {
        try {
            if (cookie != null) {
                return new PagedResultsControl(pageSize, cookie.getCookie(),
                        CRITICAL_CONTROL);
            } else {
                return new PagedResultsControl(pageSize);
            }
        } catch (IOException e) {
            throw new CreateControlFailedException(
                    "Error creating PagedResultsControl", e);
        }
    }

    /*
     * @see org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming.directory.DirContext)
     */
    public void postProcess(DirContext ctx) throws NamingException {
        // initialize from property
        currentResponseControlClass = responseControlClass;

        LdapContext ldapContext = (LdapContext) ctx;
        Control[] responseControls = ldapContext.getResponseControls();
        if (responseControls == null) {
            responseControls = new Control[0];
        }

        // Go through response controls and get info, regardless of class
        for (int i = 0; i < responseControls.length; i++) {
            Control responseControl = responseControls[i];

            // check for match, try fallback otherwise
            if (isPagedResultsResponseControl(responseControl)) {
                Object control = responseControl;
                byte[] result = (byte[]) invokeMethod("getCookie",
                        currentResponseControlClass, control);
                this.cookie = new PagedResultsCookie(result);
                Integer wrapper = (Integer) invokeMethod("getResultSize",
                        currentResponseControlClass, control);
                this.resultSize = wrapper.intValue();
            }
        }
    }

    /**
     * Check if the given control matches a paged results response control. Try
     * the fallback class from Java5 if there is no match. Set the
     * {@link #currentResponseControlClass} to the fallback if it matches.
     * 
     * @param responseControl
     *            the control to check for a match
     * @return whether the control is a paged results response control
     */
    private boolean isPagedResultsResponseControl(Control responseControl) {
        if (responseControl.getClass().isAssignableFrom(
                currentResponseControlClass)) {
            return true;
        }
        if (fallbackResponseControlClass != null
                && responseControl.getClass().isAssignableFrom(
                        fallbackResponseControlClass)) {
            currentResponseControlClass = fallbackResponseControlClass;
            return true;
        }
        return false;
    }

    private Class loadFallbackResponseControlClass() {
        Class fallbackResponseControlClass = null;
        try {
            fallbackResponseControlClass = Class
                    .forName(JAVA5_RESPONSE_CONTROL);
        } catch (ClassNotFoundException e) {
            log.debug("Could not load Java5 response control class "
                    + JAVA5_RESPONSE_CONTROL);
        }
        return fallbackResponseControlClass;
    }

    private Object invokeMethod(String method, Class clazz, Object control) {
        // For Spring 2.0 ReflectionUtils could be used for all of this, but
        // since we still want to support the 1.2 branch we do it manually and
        // only use the stuff present in 1.2.8.
        Method actualMethod = null;
        Object retval = null;
        try {
            actualMethod = clazz.getMethod(method, new Class[0]);
        } catch (SecurityException e) {
            ReflectionUtils.handleReflectionException(e);
        } catch (NoSuchMethodException e) {
            ReflectionUtils.handleReflectionException(e);
        }

        try {
            retval = actualMethod.invoke(control, new Object[0]);
        } catch (IllegalArgumentException e) {
            ReflectionUtils.handleReflectionException(e);
        } catch (IllegalAccessException e) {
            ReflectionUtils.handleReflectionException(e);
        } catch (InvocationTargetException e) {
            ReflectionUtils.handleReflectionException(e);
        }

        // Retval will be set unless an exception has been thrown.
        return retval;
    }
}
