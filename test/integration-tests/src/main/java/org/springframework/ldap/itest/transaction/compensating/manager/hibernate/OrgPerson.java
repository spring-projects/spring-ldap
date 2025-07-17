/*
 * Copyright 2005-2023 the original author or authors.
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

/**
 * Pojo for use with the ContextSourceAndHibernateTransactionManager integration tests
 *
 * @author Hans Westerbeek
 *
 */
public class OrgPerson {

	private Integer id;

	private String fullname;

	private String lastname;

	private String company;

	private String country;

	private String description;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullname() {
		return this.fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OrgPerson other = (OrgPerson) obj;
		if (this.company == null) {
			if (other.company != null) {
				return false;
			}
		}
		else if (!this.company.equals(other.company)) {
			return false;
		}
		if (this.country == null) {
			if (other.country != null) {
				return false;
			}
		}
		else if (!this.country.equals(other.country)) {
			return false;
		}
		if (this.description == null) {
			if (other.description != null) {
				return false;
			}
		}
		else if (!this.description.equals(other.description)) {
			return false;
		}
		if (this.fullname == null) {
			if (other.fullname != null) {
				return false;
			}
		}
		else if (!this.fullname.equals(other.fullname)) {
			return false;
		}
		if (this.lastname == null) {
			if (other.lastname != null) {
				return false;
			}
		}
		else if (!this.lastname.equals(other.lastname)) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.company == null) ? 0 : this.company.hashCode());
		result = prime * result + ((this.country == null) ? 0 : this.country.hashCode());
		result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
		result = prime * result + ((this.fullname == null) ? 0 : this.fullname.hashCode());
		result = prime * result + ((this.lastname == null) ? 0 : this.lastname.hashCode());
		return result;
	}

}
