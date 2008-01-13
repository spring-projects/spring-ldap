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
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.ldap.control.CreateControlFailedException;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ReflectionUtils;

import com.sun.jndi.ldap.ctl.SortControl;
import com.sun.jndi.ldap.ctl.VirtualListViewControl;
import com.sun.jndi.ldap.ctl.VirtualListViewResponseControl;

/**
 * DirContextProcessor implementation for managing a virtual list view.
 * <p>
 * This is the request control syntax:
 * 
 * <pre>
 * VirtualListViewRequest ::= SEQUENCE { 
 *        beforeCount    INTEGER (0..maxInt), 
 *        afterCount     INTEGER (0..maxInt), 
 *        target       CHOICE { 
 *                       byOffset        [0] SEQUENCE {                           
 *                            offset          INTEGER (1 .. maxInt), 
 *                            contentCount    INTEGER (0 .. maxInt) }, 
 *                       greaterThanOrEqual [1] AssertionValue }, 
 *        contextID     OCTET STRING OPTIONAL }
 * </pre>
 * 
 * <p>
 * This is the response control syntax:
 * 
 * <pre>
 * VirtualListViewResponse ::= SEQUENCE { 
 *        targetPosition    INTEGER (0 .. maxInt), 
 *        contentCount     INTEGER (0 .. maxInt), 
 *        virtualListViewResult ENUMERATED { 
 *             success (0), 
 *             operationsError (1), 
 *             protocolError (3), 
 *             unwillingToPerform (53), 
 *             insufficientAccessRights (50), 
 *             timeLimitExceeded (3), 
 *             adminLimitExceeded (11), 
 *             innapropriateMatching (18), 
 *             sortControlMissing (60), 
 *             offsetRangeError (61), 
 *             other(80), 
 *             ... }, 
 *        contextID     OCTET STRING OPTIONAL }
 * </pre>
 * 
 * @author Ulrik Sandberg
 * @see <a href="http://www3.ietf.org/proceedings/02nov/I-D/draft-ietf-ldapext-ldapv3-vlv-09.txt">LDAP Extensions for Scrolling View Browsing of Search Results</a>
 */
public class VirtualListViewControlDirContextProcessor implements
        DirContextProcessor {

    private static final Class DEFAULT_RESPONSE_CONTROL = VirtualListViewResponseControl.class;

    private static final boolean CRITICAL_CONTROL = true;

    private int pageSize;

    private VirtualListViewResultsCookie cookie;

    private int listSize;

    private int targetOffset;

    private NamingException exception;

    private int resultCode;

    private Class responseControlClass = DEFAULT_RESPONSE_CONTROL;

    private boolean offsetPercentage;

    public VirtualListViewControlDirContextProcessor(int pageSize) {
        this(pageSize, 0, 0, null);
    }

    public VirtualListViewControlDirContextProcessor(int pageSize,
            int targetOffset, int listSize, VirtualListViewResultsCookie cookie) {
        this.pageSize = pageSize;
        this.targetOffset = targetOffset;
        this.listSize = listSize;
        this.cookie = cookie;
    }

    public VirtualListViewResultsCookie getCookie() {
        return cookie;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getListSize() {
        return listSize;
    }

    public NamingException getException() {
        return exception;
    }

    public int getResultCode() {
        return resultCode;
    }

    public int getTargetOffset() {
        return targetOffset;
    }

    /**
     * Set the class of the expected ResponseControl for the paged results
     * response. The default is {@link VirtualListViewResponseControl}.
     * 
     * @param responseControlClass
     *            Class of the expected response control.
     */
    public void setResponseControlClass(Class responseControlClass) {
        this.responseControlClass = responseControlClass;
    }

    public void setOffsetPercentage(boolean isPercentage) {
        this.offsetPercentage = isPercentage;
    }

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
        Control newControl = createRequestControl();

        Control[] newControls = new Control[requestControls.length + 2];
        for (int i = 0; i < requestControls.length; i++) {
            newControls[i] = requestControls[i];
        }

        SortControl sortControl;
        try {
            sortControl = new SortControl(new String[] { "cn" }, true);
        } catch (IOException e) {
            throw new CreateControlFailedException(
                    "Couldn't create SortControl", e);
        }
        // Add the new Controls at the end of the array.
        newControls[newControls.length - 2] = sortControl;
        newControls[newControls.length - 1] = newControl;

        ldapContext.setRequestControls(newControls);
    }

    /*
     * @see org.springframework.ldap.control.AbstractRequestControlDirContextProcessor#createRequestControl()
     */
    public Control createRequestControl() {
        try {
            VirtualListViewControl virtualListViewControl;
            if (offsetPercentage) {
                virtualListViewControl = new VirtualListViewControl(
                        targetOffset, pageSize, CRITICAL_CONTROL);
            } else {
                virtualListViewControl = new VirtualListViewControl(
                        targetOffset, listSize, 0, pageSize - 1,
                        CRITICAL_CONTROL);
            }
            if (cookie != null) {
                virtualListViewControl.setContextID(cookie.getCookie());
            }
            return virtualListViewControl;
        } catch (IOException e) {
            throw new CreateControlFailedException(
                    "Error creating VirtualListViewControl", e);
        }
    }

    /*
     * @see org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming.directory.DirContext)
     */
    public void postProcess(DirContext ctx) throws NamingException {
        LdapContext ldapContext = (LdapContext) ctx;
        Control[] responseControls = ldapContext.getResponseControls();

        if (responseControls == null) {
            return;
        }

        // Go through response controls and get info, regardless of class
        for (int i = 0; i < responseControls.length; i++) {
            Control responseControl = responseControls[i];

            // check for match
            if (isVirtualListViewResponseControl(responseControl)) {
                Object control = responseControl;
                byte[] result = (byte[]) invokeMethod("getContextID",
                        responseControlClass, control);
                Integer listSize = (Integer) invokeMethod("getListSize",
                        responseControlClass, control);
                Integer targetOffset = (Integer) invokeMethod(
                        "getTargetOffset", responseControlClass, control);
                this.exception = (NamingException) invokeMethod("getException",
                        responseControlClass, control);

                this.cookie = new VirtualListViewResultsCookie(result,
                        targetOffset.intValue(), listSize.intValue());

                if (exception != null) {
                    throw LdapUtils.convertLdapException(exception);
                }
            }
        }
    }

    /**
     * Check if the given control matches a paged results response control.
     * 
     * @param responseControl
     *            the control to check for a match
     * @return whether the control is a paged results response control
     */
    private boolean isVirtualListViewResponseControl(Control responseControl) {
        if (responseControl.getClass().isAssignableFrom(responseControlClass)) {
            return true;
        }
        return false;
    }

    private Object invokeMethod(String method, Class clazz, Object control) {
        Method m = ReflectionUtils.findMethod(clazz, method, new Class[0]);
        return ReflectionUtils.invokeMethod(m, control);
    }
}
