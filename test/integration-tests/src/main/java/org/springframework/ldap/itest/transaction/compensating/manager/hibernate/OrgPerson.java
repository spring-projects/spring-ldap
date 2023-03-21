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
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OrgPerson other = (OrgPerson) obj;
		if (company == null) {
			if (other.company != null)
				return false;
		}
		else if (!company.equals(other.company))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		}
		else if (!country.equals(other.country))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		}
		else if (!description.equals(other.description))
			return false;
		if (fullname == null) {
			if (other.fullname != null)
				return false;
		}
		else if (!fullname.equals(other.fullname))
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		}
		else if (!lastname.equals(other.lastname))
			return false;
		return true;
	}

}
