package se.jayway.demo.spring.ldap.service;

import java.util.List;

import org.springframework.ldap.core.DistinguishedName;

import se.jayway.demo.spring.ldap.dao.PersonDao;
import se.jayway.demo.spring.ldap.domain.Person;

public class PersonServiceImpl implements PersonService {

	private PersonDao personDao;

	public final void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public List<Person> findAll() {
		return personDao.findAll();
	}

	public Person findByPrimaryKey(String country, String company, String name) {
		return personDao.findByPrimaryKey(buildDn(country, company, name));
	}

	public void update(Person person) {
		personDao.update(person);
	}

	private String buildDn(String country, String company, String fullName) {
		DistinguishedName dn = new DistinguishedName();
		dn.add("c", country);
		dn.add("ou", company);
		dn.add("cn", fullName);
		return dn.toString();
	}

}
