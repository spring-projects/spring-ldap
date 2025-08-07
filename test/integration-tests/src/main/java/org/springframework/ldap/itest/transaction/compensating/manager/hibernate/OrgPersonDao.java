/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.itest.transaction.compensating.manager.hibernate;

public interface OrgPersonDao {

	void createWithException(OrgPerson person);

	void create(OrgPerson person);

	void update(OrgPerson person);

	void updateWithException(OrgPerson person);

	void updateAndRename(String dn, String newDn, String updatedDescription);

	void updateAndRenameWithException(String dn, String newDn, String updatedDescription);

	void modifyAttributes(String dn, String lastName, String description);

	void modifyAttributesWithException(String dn, String lastName, String description);

	void unbind(OrgPerson person);

	void unbindWithException(OrgPerson person);

}
