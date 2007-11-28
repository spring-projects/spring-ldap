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

package org.springframework.ldap.pool.validation;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.pool.DirContextType;

/**
 * Default {@link DirContext} validator that executes {@link DirContext#search(String, String, SearchControls)}. The
 * name, filter and {@link SearchControls} are all configurable. There is no special handling for read only versus
 * read write {@link DirContext}s.
 * 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td valign="top">base</td>
 *         <td valign="top">
 *             The name parameter to the search method.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">""</td>
 *     </tr>
 *     <tr>
 *         <td valign="top">filter</td>
 *         <td valign="top">
 *             The filter parameter to the search method.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">"objectclass=*"</td>
 *     </tr>
 *     <tr>
 *         <td valign="top">searchControls</td>
 *         <td valign="top">
 *             The {@link SearchControls} parameter to the search method.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">
 *             {@link SearchControls#setCountLimit(long)} = 1<br/>
 *             {@link SearchControls#setReturningAttributes(String[])} = new String[] { "objectclass" }<br/>
 *             {@link SearchControls#setTimeLimit(int)} = 500
 *         </td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist
 */
public class DefaultDirContextValidator implements DirContextValidator {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String base;
    private String filter;
    private SearchControls searchControls;
    
    public DefaultDirContextValidator() {
        this.searchControls = new SearchControls();
        this.searchControls.setCountLimit(1);
        this.searchControls.setReturningAttributes(new String[] { "objectclass" });
        this.searchControls.setTimeLimit(500);

        this.base = "";

        this.filter = "objectclass=*";
    }
    
    /**
     * @return the baseName
     */
    public String getBase() {
        return this.base;
    }
    /**
     * @param base the baseName to set
     */
    public void setBase(String base) {
        this.base = base;
    }
    /**
     * @return the filter
     */
    public String getFilter() {
        return this.filter;
    }
    /**
     * @param filter the filter to set
     */
    public void setFilter(String filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter may not be null");
        }
        
        this.filter = filter;
    }
    /**
     * @return the searchControls
     */
    public SearchControls getSearchControls() {
        return this.searchControls;
    }
    /**
     * @param searchControls the searchControls to set
     */
    public void setSearchControls(SearchControls searchControls) {
        if (searchControls == null) {
            throw new IllegalArgumentException("searchControls may not be null");
        }
        
        this.searchControls = searchControls;
    }


    /**
     * @see edu.wisc.commons.lcp.validation.DirContextValidator#validateDirContext(edu.wisc.commons.lcp.pool.DirContextType, javax.naming.directory.DirContext)
     */
    public boolean validateDirContext(DirContextType contextType, DirContext dirContext) {
        Validate.notNull(contextType, "contextType may not be null");
        Validate.notNull(dirContext, "dirContext may not be null");
        
        try {
            final NamingEnumeration searchResults = dirContext.search(this.base, this.filter, this.searchControls);

            if (searchResults.hasMore()) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("DirContext '" + dirContext + "' passed validation.");
                }

                return true;
            }
        }
        catch (Exception e) {
            this.logger.warn("DirContext '" + dirContext + "' failed validation with an exception.", e);
        }

        if (this.logger.isInfoEnabled()) {
            this.logger.info("DirContext '" + dirContext + "' failed validation.");
        }
        return false;
    }
}
