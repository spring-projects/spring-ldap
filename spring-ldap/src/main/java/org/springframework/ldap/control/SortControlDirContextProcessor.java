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

import org.springframework.ldap.control.AbstractRequestControlDirContextProcessor;
import org.springframework.ldap.control.CreateControlFailedException;
import org.springframework.util.ReflectionUtils;

import com.sun.jndi.ldap.ctl.SortControl;
import com.sun.jndi.ldap.ctl.SortResponseControl;

/**
 * DirContextProcessor implementation for managing the {@link SortControl}.
 * Note that this class is stateful, so a new instance needs to be instantiated
 * for each new search.
 * 
 * @author Ulrik Sandberg
 */
public class SortControlDirContextProcessor extends
        AbstractRequestControlDirContextProcessor {

    private static final Class DEFAULT_RESPONSE_CONTROL = SortResponseControl.class;

    private static final boolean CRITICAL_CONTROL = true;

    private static final String JAVA5_RESPONSE_CONTROL = "javax.naming.ldap.SortResponseControl";

    /**
     * What key to sort on.
     */
    private String sortKey;

    /**
     * Whether the search result actually was sorted.
     */
    private boolean sorted;

    /**
     * The result code of the supposedly sorted search.
     */
    private int resultCode;

    private Class responseControlClass = DEFAULT_RESPONSE_CONTROL;

    private Class fallbackResponseControlClass;

    private Class currentResponseControlClass;

    /**
     * Constructs a new instance using the supplied sort key.
     * 
     * @param sortKey
     *            the sort key, i.e. the attribute name to sort on.
     */
    public SortControlDirContextProcessor(String sortKey) {
        this.sortKey = sortKey;
        fallbackResponseControlClass = loadFallbackResponseControlClass();
        setSorted(false);
        setResultCode(-1);
    }

    /**
     * Set the class of the expected ResponseControl for the sorted result
     * response. The default is {@link SortResponseControl}.
     * 
     * @param responseControlClass
     *            Class of the expected response control.
     */
    public void setResponseControlClass(Class responseControlClass) {
        this.responseControlClass = responseControlClass;
    }

    /**
     * Check whether the returned values were actually sorted by the server.
     * 
     * @return <code>true</code> if the result was sorted, <code>false</code>
     *         otherwise.
     */
    public boolean isSorted() {
        return sorted;
    }

    private void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    /**
     * Get the result code returned by the control.
     * 
     * @return result code.
     */
    public int getResultCode() {
        return resultCode;
    }

    private void setResultCode(int sortResult) {
        this.resultCode = sortResult;
    }

    /**
     * Get the sort key.
     * 
     * @return the sort key.
     */
    public String getSortKey() {
        return sortKey;
    }

    /**
     * Set the sort key, i.e. the attribute on which to sort on.
     * 
     * @param sortKey
     *            the sort key.
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /*
     * @see org.springframework.ldap.control.AbstractRequestControlDirContextProcessor#createRequestControl()
     */
    public Control createRequestControl() {
        try {
            return new SortControl(new String[] { sortKey }, CRITICAL_CONTROL);
        } catch (IOException e) {
            throw new CreateControlFailedException(
                    "Error creating SortControl", e);
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
            return;
        }

        // Go through response controls and get info, regardless of class
        for (int i = 0; i < responseControls.length; i++) {
            Control responseControl = responseControls[i];

            // check for match, try fallback otherwise
            if (isSortResponseControl(responseControl)) {
                Object control = responseControl;
                Boolean result = (Boolean) invokeMethod("isSorted",
                        currentResponseControlClass, control);
                setSorted(result.booleanValue());
                Integer code = (Integer) invokeMethod("getResultCode",
                        currentResponseControlClass, control);
                setResultCode(code.intValue());
            }
        }
    }

    /**
     * Check if the given control matches a sort response control. Try the
     * fallback class from Java5 if there is no match. Set the
     * {@link #currentResponseControlClass} to the fallback if it matches.
     * 
     * @param responseControl
     *            the control to check for a match
     * @return whether the control is a paged results response control
     */
    private boolean isSortResponseControl(Control responseControl) {
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
