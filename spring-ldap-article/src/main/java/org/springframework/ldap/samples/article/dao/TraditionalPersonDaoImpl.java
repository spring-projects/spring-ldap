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
package org.springframework.ldap.samples.article.dao;

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
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.ldap.samples.article.domain.Person;

/**
 * Traditional implementation of PersonDao. This implementation uses the basic
 * JNDI interfaces and classes {@link DirContext}, {@link Attributes},
 * {@link Attribute}, and {@link NamingEnumeration}. The purpose is to
 * contrast this implementation with that of {@link PersonDaoImpl}.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class TraditionalPersonDaoImpl implements
         PersonDao {

   private String userName;

   private String password;

   private String url;

   private String base;

   /*
    * @see PersonDao#create(Person)
    */
   public void create(Person person) {
      DirContext ctx = createAuthenticatedContext();
      String dn = buildDn(person);
      try {
         Attributes attrs = getAttributesToBind(person);
         ctx.bind(dn, null, attrs);
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
    * @see PersonDao#update(Person)
    */
   public void update(Person person) {
      DirContext ctx = createAuthenticatedContext();
      String dn = buildDn(person);
      try {
         ctx
            .rebind(
               dn, null,
               getAttributesToBind(person));
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
    * @see PersonDao#delete(Person)
    */
   public void delete(Person person) {
      DirContext ctx = createAuthenticatedContext();
      String dn = buildDn(person);
      try {
         ctx.unbind(dn);
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
    * @see PersonDao#getAllPersonNames()
    */
   public List getAllPersonNames() {
      DirContext ctx = createAnonymousContext();

      LinkedList list = new LinkedList();
      NamingEnumeration results = null;
      try {
         SearchControls controls = new SearchControls();
         controls
            .setSearchScope(SearchControls.SUBTREE_SCOPE);
         results = ctx.search(
            "", "(objectclass=person)", controls);

         while (results.hasMore()) {
            SearchResult searchResult = (SearchResult) results
               .next();
            Attributes attributes = searchResult
               .getAttributes();
            Attribute attr = attributes.get("cn");
            String cn = (String) attr.get();
            list.add(cn);
         }
      } catch (NameNotFoundException e) {
         // The base context was not found.
         // Just clean up and exit.
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
    * @see PersonDao#findAll()
    */
   public List findAll() {
      DirContext ctx = createAnonymousContext();

      LinkedList list = new LinkedList();
      NamingEnumeration results = null;
      try {
         SearchControls controls = new SearchControls();
         controls
            .setSearchScope(SearchControls.SUBTREE_SCOPE);
         results = ctx.search(
            "", "(objectclass=person)", controls);

         while (results.hasMore()) {
            SearchResult searchResult = (SearchResult) results
               .next();
            String dn = searchResult.getName();
            Attributes attributes = searchResult
               .getAttributes();
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
    * @see PersonDao#findByPrimaryKey(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   public Person findByPrimaryKey(String country,
      String company, String fullname) {

      DirContext ctx = createAnonymousContext();
      String dn = buildDn(
         country, company, fullname);
      try {
         Attributes attributes = ctx
            .getAttributes(dn);
         return mapToPerson(dn, attributes);
      } catch (NameNotFoundException e) {
         throw new DataRetrievalFailureException(
            "Did not find entry with primary key '"
               + dn + "'", e);
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

   private String buildDn(Person person) {
      return buildDn(person.getCountry(), person
         .getCompany(), person.getFullName());
   }

   private String buildDn(String country, String company,
      String fullname) {
      StringBuffer sb = new StringBuffer();
      sb.append("cn=");
      sb.append(fullname);
      sb.append(", ");
      sb.append("ou=");
      sb.append(company);
      sb.append(", ");
      sb.append("c=");
      sb.append(country);
      String dn = sb.toString();
      return dn;
   }

   private DirContext createContext(Hashtable env) {
      env.put(
         Context.INITIAL_CONTEXT_FACTORY,
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

   private DirContext createAnonymousContext() {
      Hashtable env = new Hashtable();
      return createContext(env);
   }

   private DirContext createAuthenticatedContext() {
      Hashtable env = new Hashtable();
      env.put(
         Context.SECURITY_AUTHENTICATION,
         "simple");
      env.put(
         Context.SECURITY_PRINCIPAL, userName);
      env.put(
         Context.SECURITY_CREDENTIALS, password);
      return createContext(env);
   }

   private Attributes getAttributesToBind(
      Person person) {
      Attributes attrs = new BasicAttributes();
      BasicAttribute ocattr = new BasicAttribute(
         "objectclass");
      ocattr.add("top");
      ocattr.add("person");
      attrs.put(ocattr);
      attrs.put("cn", person.getFullName());
      attrs.put("sn", person.getLastName());
      attrs.put("description", person
         .getDescription());
      attrs.put("telephoneNumber", person
         .getPhone());
      return attrs;
   }

   private Person mapToPerson(String dn,
      Attributes attributes)
            throws NamingException {
      Person person = new Person();
      person.setFullName((String) attributes.get(
         "cn").get());
      person.setLastName((String) attributes.get(
         "sn").get());
      person.setDescription((String) attributes
         .get("description").get());
      person.setPhone((String) attributes.get(
         "telephoneNumber").get());

      // Remove any trailing spaces after comma
      String cleanedDn = dn
         .replaceAll(", *", ",");

      String countryMarker = ",c=";
      int countryIndex = cleanedDn
         .lastIndexOf(countryMarker);

      String companyMarker = ",ou=";
      int companyIndex = cleanedDn
         .lastIndexOf(companyMarker);

      String country = cleanedDn
         .substring(countryIndex
            + countryMarker.length());
      person.setCountry(country);
      String company = cleanedDn.substring(
         companyIndex + companyMarker.length(),
         countryIndex);
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

   public void setPassword(String credentials) {
      this.password = credentials;
   }

   public void setUserName(String principal) {
      this.userName = principal;
   }
}
