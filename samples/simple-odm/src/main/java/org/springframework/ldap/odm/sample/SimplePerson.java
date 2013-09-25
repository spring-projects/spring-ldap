/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.odm.sample;
 
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.springframework.ldap.odm.annotations.Attribute.Type;

/**
*  Automatically generated to represent the LDAP object classes
*  "person", "top".
*/
@Entry(objectClasses={"person", "top"})
public final class SimplePerson {

   @Id
   private Name dn;
   
   @Attribute(name="objectClass", syntax="1.3.6.1.4.1.1466.115.121.1.38")
   private List<String> objectClass=new ArrayList<String>();
       

   @Attribute(name="cn", syntax="1.3.6.1.4.1.1466.115.121.1.15")
   private List<String> cn=new ArrayList<String>();
       

   @Attribute(name="sn", syntax="1.3.6.1.4.1.1466.115.121.1.15")
   private List<String> sn=new ArrayList<String>();
        

   @Attribute(name="description", syntax="1.3.6.1.4.1.1466.115.121.1.15")
   private List<String> description=new ArrayList<String>();
       

   @Attribute(name="userPassword", syntax="1.3.6.1.4.1.1466.115.121.1.40", type=Type.BINARY)
   private List<byte[]> userPassword=new ArrayList<byte[]>();
       

   @Attribute(name="telephoneNumber", syntax="1.3.6.1.4.1.1466.115.121.1.50")
   private List<String> telephoneNumber=new ArrayList<String>();
       

   @Attribute(name="seeAlso", syntax="1.3.6.1.4.1.1466.115.121.1.12")
   private List<String> seeAlso=new ArrayList<String>();
       
   public Name getDn() {
      return dn;
   }
   
   public void setDn(Name dn) {
      this.dn=dn;
   }
   
   public Iterator<String> getObjectClassIterator() {
      return Collections.unmodifiableList(objectClass).iterator();
   }
                                
   public void addCn(String cn) {
      this.cn.add(cn);
   }
                                
   public void removeCn(String cn) {
      this.cn.remove(cn);
   }
                                
   public Iterator<String> getCnIterator() {
      return cn.iterator();
   }
                                
   public void addSn(String sn) {
      this.sn.add(sn);
   }
                                
   public void removeSn(String sn) {
      this.sn.remove(sn);
   }
                                
   public Iterator<String> getSnIterator() {
      return sn.iterator();
   }
                                
   public void addDescription(String description) {
      this.description.add(description);
   }
                                
   public void removeDescription(String description) {
      this.description.remove(description);
   }
                                
   public Iterator<String> getDescriptionIterator() {
      return description.iterator();
   }
                                
   public void addUserPassword(byte[] userPassword) {
      this.userPassword.add(userPassword);
   }
                                
   public void removeUserPassword(byte[] userPassword) {
      this.userPassword.remove(userPassword);
   }
                                
   public Iterator<byte[]> getUserPasswordIterator() {
      return userPassword.iterator();
   }
                                
   public void addTelephoneNumber(String telephoneNumber) {
      this.telephoneNumber.add(telephoneNumber);
   }
                                
   public void removeTelephoneNumber(String telephoneNumber) {
      this.telephoneNumber.remove(telephoneNumber);
   }
                                
   public Iterator<String> getTelephoneNumberIterator() {
      return telephoneNumber.iterator();
   }
                                
   public void addSeeAlso(String seeAlso) {
      this.seeAlso.add(seeAlso);
   }
                                
   public void removeSeeAlso(String seeAlso) {
      this.seeAlso.remove(seeAlso);
   }
                                
   public Iterator<String> getSeeAlsoIterator() {
      return seeAlso.iterator();
   }
                                
   @Override
   public String toString() {
      StringBuilder result=new StringBuilder();
      result.append(String.format("dn=%1$s", dn));
      result.append(String.format(" | objectClass=%1$s", objectClass));
      result.append(String.format(" | cn=%1$s", cn));
      result.append(String.format(" | sn=%1$s", sn));
      result.append(String.format(" | description=%1$s", description));
      result.append(String.format(" | userPassword=%1$s", userPassword));
      result.append(String.format(" | telephoneNumber=%1$s", telephoneNumber));
      result.append(String.format(" | seeAlso=%1$s", seeAlso));
      return result.toString();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((dn == null) ? 0 : dn.hashCode());
      result = prime * result + ((objectClass == null) ? 0 : (new HashSet<String>(objectClass)).hashCode());
      result = prime * result + ((cn == null) ? 0 : (new HashSet<String>(cn)).hashCode());
      result = prime * result + ((sn == null) ? 0 : (new HashSet<String>(sn)).hashCode());
      result = prime * result + ((description == null) ? 0 : (new HashSet<String>(description)).hashCode());
      result = prime * result + ((userPassword == null) ? 0 : (new HashSet<byte[]>(userPassword)).hashCode());
      result = prime * result + ((telephoneNumber == null) ? 0 : (new HashSet<String>(telephoneNumber)).hashCode());
      result = prime * result + ((seeAlso == null) ? 0 : (new HashSet<String>(seeAlso)).hashCode());
      return result;
    }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      SimplePerson other = (SimplePerson) obj;
      if (dn == null) {
          if (other.dn != null)
             return false;
       } else if (!dn.equals(other.dn))
         return false;
      if (objectClass == null) {
         if (other.objectClass != null)
            return false;
      } else if (!(new HashSet<String>(objectClass)).equals(new HashSet<String>(other.objectClass)))
         return false;
      if (cn == null) {
         if (other.cn != null)
            return false;
      } else if (!(new HashSet<String>(cn)).equals(new HashSet<String>(other.cn)))
         return false;
      if (sn == null) {
         if (other.sn != null)
            return false;
      } else if (!(new HashSet<String>(sn)).equals(new HashSet<String>(other.sn)))
         return false;
      if (description == null) {
         if (other.description != null)
            return false;
      } else if (!(new HashSet<String>(description)).equals(new HashSet<String>(other.description)))
         return false;
      if (userPassword == null) {
         if (other.userPassword != null)
            return false;
      } else if (!(new HashSet<byte[]>(userPassword)).equals(new HashSet<byte[]>(other.userPassword)))
         return false;
      if (telephoneNumber == null) {
         if (other.telephoneNumber != null)
            return false;
      } else if (!(new HashSet<String>(telephoneNumber)).equals(new HashSet<String>(other.telephoneNumber)))
         return false;
      if (seeAlso == null) {
         if (other.seeAlso != null)
            return false;
      } else if (!(new HashSet<String>(seeAlso)).equals(new HashSet<String>(other.seeAlso)))
         return false;
      return true;
   }
}
