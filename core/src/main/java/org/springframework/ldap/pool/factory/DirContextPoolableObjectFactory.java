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

package org.springframework.ldap.pool.factory;

import javax.naming.directory.DirContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;

/**
 * Factory that creates {@link DirContext} instances for pooling via a
 * configured {@link ContextSource}. The {@link DirContext}s are keyed based
 * on if they are read only or read/write. The expected key type is the
 * {@link DirContextType} enum.
 * 
 * <br>
 * <br>
 * Configuration: <table border="1">
 * <tr>
 * <th align="left">Property</th>
 * <th align="left">Description</th>
 * <th align="left">Required</th>
 * <th align="left">Default</th>
 * </tr>
 * <tr>
 * <td valign="top">contextSource</td>
 * <td valign="top"> The {@link ContextSource} to get {@link DirContext}s from
 * for adding to the pool. </td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td valign="top">dirContextValidator</td>
 * <td valign="top"> The {@link DirContextValidator} to use to validate
 * {@link DirContext}s. This is only required if the pool has validation of any
 * kind turned on. </td>
 * <td valign="top">No</td>
 * <td valign="top">null</td>
 * </tr>
 * </table>
 * 
 * @author Eric Dalquist <a
 *         href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
class DirContextPoolableObjectFactory extends BaseKeyedPoolableObjectFactory {
    /**
     * Logger for this class and subclasses
     */
    protected final Log logger = LogFactory.getLog(this.getClass());

    private ContextSource contextSource;

    private DirContextValidator dirContextValidator;

    /**
     * @return the contextSource
     */
    public ContextSource getContextSource() {
        return this.contextSource;
    }

    /**
     * @param contextSource
     *            the contextSource to set
     */
    public void setContextSource(ContextSource contextSource) {
        if (contextSource == null) {
            throw new IllegalArgumentException("contextSource may not be null");
        }

        this.contextSource = contextSource;
    }

    /**
     * @return the dirContextValidator
     */
    public DirContextValidator getDirContextValidator() {
        return this.dirContextValidator;
    }

    /**
     * @param dirContextValidator
     *            the dirContextValidator to set
     */
    public void setDirContextValidator(DirContextValidator dirContextValidator) {
        if (dirContextValidator == null) {
            throw new IllegalArgumentException(
                    "dirContextValidator may not be null");
        }

        this.dirContextValidator = dirContextValidator;
    }

    /**
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#makeObject(java.lang.Object)
     */
    public Object makeObject(Object key) throws Exception {
        Validate.notNull(this.contextSource, "ContextSource may not be null");
        Validate.isTrue(key instanceof DirContextType,
                "key must be a DirContextType");

        final DirContextType contextType = (DirContextType) key;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Creating a new " + contextType + " DirContext");
        }

        if (contextType == DirContextType.READ_WRITE) {
            final DirContext readWriteContext = this.contextSource
                    .getReadWriteContext();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Created new " + DirContextType.READ_WRITE
                        + " DirContext='" + readWriteContext + "'");
            }

            return readWriteContext;
        } else if (contextType == DirContextType.READ_ONLY) {

            final DirContext readOnlyContext = this.contextSource
                    .getReadOnlyContext();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Created new " + DirContextType.READ_ONLY
                        + " DirContext='" + readOnlyContext + "'");
            }

            return readOnlyContext;
        } else {
            throw new IllegalArgumentException("Unrecognized ContextType: "
                    + contextType);
        }
    }

    /**
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#validateObject(java.lang.Object,
     *      java.lang.Object)
     */
    public boolean validateObject(Object key, Object obj) {
        Validate.notNull(this.dirContextValidator,
                "DirContextValidator may not be null");
        Validate.isTrue(key instanceof DirContextType,
                "key must be a DirContextType");
        Validate.isTrue(obj instanceof DirContext,
                "The Object to validate must be of type '" + DirContext.class
                        + "'");

        try {
            final DirContextType contextType = (DirContextType) key;
            final DirContext dirContext = (DirContext) obj;
            return this.dirContextValidator.validateDirContext(contextType,
                    dirContext);
        } catch (Exception e) {
            this.logger.warn("Failed to validate '" + obj
                    + "' due to an unexpected exception.", e);
            return false;
        }
    }
    

    /**
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#destroyObject(java.lang.Object,
     *      java.lang.Object)
     */
    public void destroyObject(Object key, Object obj) throws Exception {
        Validate.isTrue(obj instanceof DirContext,
                "The Object to validate must be of type '" + DirContext.class
                        + "'");

        try {
            final DirContext dirContext = (DirContext) obj;
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Closing " + key + " DirContext='"
                        + dirContext + "'");
            }
            dirContext.close();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Closed " + key + " DirContext='"
                        + dirContext + "'");
            }
        } catch (Exception e) {
            this.logger.warn(
                    "An exception occured while closing '" + obj + "'", e);
        }
    }
}
