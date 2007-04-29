/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

public interface DummyDao {
    void createWithException(String country, String company, String fullname,
            String lastname, String description);

    void create(String country, String company, String fullname,
            String lastname, String description);

    void update(String dn, String fullname, String lastname, String description);

    void updateWithException(String dn, String fullname, String lastname,
            String description);

    void updateAndRename(String dn, String newDn, String description);

    void updateAndRenameWithException(String dn, String newDn,
            String description);

    void modifyAttributes(String dn, String lastName, String description);

    void modifyAttributesWithException(String dn, String lastName,
            String description);

    void unbind(String dn, String fullname);

    void unbindWithException(String dn, String fullname);

}
