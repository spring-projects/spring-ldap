/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.core;

import org.springframework.ldap.query.LdapQuery;

import javax.naming.Name;
import javax.naming.directory.SearchControls;
import java.util.List;

/**
 * The OdmManager interface provides generic CRUD (create/read/update/delete) 
 * and searching operations against an LDAP directory.
 * <p>
 * Each managed Java class must be appropriately annotated using
 * {@link org.springframework.ldap.odm.annotations}. 
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @author Mattias Hellborg Arthursson
 * 
 * @see org.springframework.ldap.odm.annotations.Entry
 * @see org.springframework.ldap.odm.annotations.Attribute
 * @see org.springframework.ldap.odm.annotations.Id
 * @see org.springframework.ldap.odm.annotations.Transient
 * @deprecated This functionality is automatically available in LdapTemplate as of version 2.0
 */
public interface OdmManager {   
   
    
    /**
     * Read a named entry from the LDAP directory.
     * 
     * @param <T> The Java type to return
     * @param clazz The Java type to return
     * @param dn The distinguished name of the entry to read from the LDAP directory.
     * @return The entry as read from the directory
     * 
     * @throws org.springframework.ldap.NamingException on error.
     */
    <T> T read(Class<T> clazz, Name dn);

    /**
     * Create the given entry in the LDAP directory.
     *  
     * @param entry The entry to be create, it must <em>not</em> already exist in the directory.
     * 
     * @throws org.springframework.ldap.NamingException on error.
     */
    void create(Object entry);

    /**
     * Update the given entry in the LDAP directory.
     * 
     * @param entry The entry to update, it must already exist in the directory.
     * 
     * @throws org.springframework.ldap.NamingException on error.
     */
    void update(Object entry);

    /**
     * Delete an entry from the LDAP directory.
     * 
     * @param entry The entry to delete, it must already exist in the directory.
     * 
     * @throws org.springframework.ldap.NamingException on error.
     */
    void delete(Object entry);

    /**
     * Find all entries in the LDAP directory of a given type.
     * 
     * @param <T> The Java type to return
     * @param clazz The Java type to return
     * @param base The root of the sub-tree at which to begin the search.
     * @param searchControls The scope of the search.
     * @return All entries that are of the type represented by the given 
     * Java class
     * 
     * @throws org.springframework.ldap.NamingException on error.
     */
    <T> List<T> findAll(Class<T> clazz, Name base, SearchControls searchControls);

    /**
     * Search for entries in the LDAP directory.  
     * <p>
     * Only those entries that both match the given search filter and 
     * are represented by the given Java class are returned
     * 
     * @param <T> The Java type to return
     * @param clazz The Java type to return
     * @param base The root of the sub-tree at which to begin the search.
     * @param filter An LDAP search filter.   
     * @param searchControls The scope of the search.
     * @return All matching entries.
     * 
     * @throws org.springframework.ldap.NamingException on error.
     * 
     * @see <a href="https://java.sun.com/products/jndi/tutorial/basics/directory/filter.html">Sun's JNDI tutorial description of search filters.</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc4515.txt">LDAP: String Representation of Search Filters RFC.</a>
     */
    <T> List<T> search(Class<T> clazz, Name base, String filter, SearchControls searchControls);

    /**
     * Search for entries in the LDAP directory.
     * <p>
     * Only those entries that both match the query search filter and
     * are represented by the given Java class are returned.
     *
     * @param <T> The Java type to return
     * @param clazz The Java type to return
     * @param query the LDAP query specification
     * @return All matching entries.
     *
     * @throws org.springframework.ldap.NamingException on error.
     * @see org.springframework.ldap.query.LdapQueryBuilder
     */
    <T> List<T> search(Class<T> clazz, LdapQuery query);
}
