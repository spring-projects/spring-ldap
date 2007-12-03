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
package org.springframework.ldap.samples.person.dao;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Traditional implementation of PersonDao. This implementation uses the basic
 * JNDI interfaces and classes {@link DirContext}, {@link Attributes},
 * {@link Attribute}, and {@link NamingEnumeration}. The purpose is to
 * contrast this implementation with that of
 * {@link org.springframework.ldap.samples.person.dao.PersonDaoImpl}.
 * 
 * @author Ulrik Sandberg TODO Add search method so we can use a search page
 */
public class TraditionalPersonDaoImpl implements PersonDao {

    private String url;

    private String base;

    Attributes getAttributesToBind(Person person) {
        BasicAttributes attributes = new BasicAttributes();
        BasicAttribute oc = new BasicAttribute("objectclass");
        oc.add("top");
        oc.add("person");
        oc.add("organizationalPerson");
        oc.add("inetOrgPerson");
        attributes.put(oc);
        attributes.put("cn", person.getFullName());
        attributes.put("sn", person.getLastName());
        attributes.put("description", person.getDescription());
        return attributes;
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#create(org.springframework.ldap.samples.person.domain.Person)
     */
    public void create(Person person) {
        DirContext ctx = createContext();
        try {
            ctx.bind(buildDn(person.getCountry(), person.getCompany(), person
                    .getFullName()), null, getAttributesToBind(person));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#update(org.springframework.ldap.samples.person.domain.Person)
     */
    public void update(Person person) {
        DirContext ctx = createContext();
        try {
            ctx.rebind(buildDn(person.getCountry(), person.getCompany(), person
                    .getFullName()), null, getAttributesToBind(person));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#delete(org.springframework.ldap.samples.person.domain.Person)
     */
    public void delete(Person person) {
        DirContext ctx = createContext();
        try {
            ctx.unbind(buildDn(person.getCountry(), person.getCompany(), person
                    .getFullName()));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#findAll()
     */
    public List findAll() {
        DirContext ctx = createContext();

        LinkedList list = new LinkedList();
        NamingEnumeration results = null;
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("", "(objectclass=person)", controls);

            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                String dn = searchResult.getName();
                Attributes attributes = searchResult.getAttributes();
                list.add(mapToPerson(dn, attributes));
            }
        } catch (NameNotFoundException e) {
            // The base context was not found, which basically means
            // that the search did not return any results. Just clean up and
            // exit.
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
        return list;
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#findByPrimaryKey(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Person findByPrimaryKey(String dn) {

        DirContext ctx = createContext();
        try {
            Attributes attributes = ctx.getAttributes(dn);
            return mapToPerson(dn, attributes);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    public Person findByPrimaryKeyData(String country, String company,
            String fullname) {
        DirContext ctx = createContext();
        String dn = buildDn(country, company, fullname);
        try {
            Attributes attributes = ctx.getAttributes(dn);
            return mapToPerson(dn, attributes);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    /*
     * @see org.springframework.ldap.samples.person.dao.PersonDao#find(org.springframework.ldap.samples.person.domain.SearchCriteria)
     */
    public List find(SearchCriteria criteria) {
        return null;
    }

    private String buildDn(String country, String company, String fullname) {
        StringBuffer sb = new StringBuffer();
        sb.append("cn=");
        sb.append(fullname);
        sb.append(",");
        sb.append("ou=");
        sb.append(company);
        sb.append(",");
        sb.append("c=");
        sb.append(country);
        String dn = sb.toString();
        return dn;
    }

    private DirContext createContext() {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        String tempUrl = createUrl();
        env.put(Context.PROVIDER_URL, tempUrl);
        DirContext ctx;
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }

    private Person mapToPerson(String dn, Attributes attributes)
            throws NamingException {
        Person person = new Person();
        person.setFullName((String) attributes.get("cn").get());
        person.setLastName((String) attributes.get("sn").get());
        Attribute descriptionAttribute = attributes.get("description");
        String[] description = new String[descriptionAttribute.size()];
        for (int i = 0; i < description.length; i++) {
            description[i] = (String) descriptionAttribute.get(i);
        }
        person.setDescription(description);
        person.setPhone((String) attributes.get("telephoneNumber").get());
        String countryMarker = ",c=";
        String country = dn.substring(dn.lastIndexOf(countryMarker)
                + countryMarker.length());
        person.setCountry(country);
        String companyMarker = ",ou=";
        String company = dn.substring(dn.lastIndexOf(companyMarker)
                + companyMarker.length(), dn.lastIndexOf(countryMarker));
        person.setCompany(company);
        return person;
    }

    private String createUrl() {
        String tempUrl = url;
        if (!tempUrl.endsWith("/")) {
            tempUrl += "/";
        }
        if (StringUtils.isNotEmpty(base)) {
            tempUrl += base;
        }
        return tempUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBase(String base) {
        this.base = base;
    }
}
