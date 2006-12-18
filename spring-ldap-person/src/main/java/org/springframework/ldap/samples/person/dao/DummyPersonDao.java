package org.springframework.ldap.samples.person.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.EntryNotFoundException;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

public class DummyPersonDao implements PersonDao {
    private static Map PERSONS;

    static {
        Person person1 = new Person();
        person1.setFullName("Dummy Person1");
        person1.setCompany("Dummy Company");
        person1.setCountry("Sweden");
        person1.setDescription(new String[] { "Some dummy person" });
        person1.setLastName("Person1");
        person1.setPhone("555-12312");
        person1.setDn("cn=Dummy Person1");
        Person person2 = new Person();
        person2.setFullName("Dummy Person2");
        person2.setCompany("Dummy Company");
        person2.setCountry("Norway");
        person2.setDescription(new String[] { "Another dummy person" });
        person2.setLastName("Person2");
        person2.setPhone("555-32132");
        person2.setDn("cn=Dummy Person2");
        PERSONS = new HashMap();
        PERSONS.put("Dummy Person1", person1);
        PERSONS.put("Dummy Person2", person2);
    }

    Log log = LogFactory.getLog(DummyPersonDao.class);

    public void create(Person person) {
        log.info("create");
    }

    public void delete(Person person) {
        log.info("delete");
    }

    public List find(SearchCriteria criteria) {
        log.info("find");
        if (PERSONS.get(criteria.getName()) != null) {
            return Collections.singletonList(PERSONS.get(criteria.getName()));
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public List findAll() {
        log.info("findAll");
        return new LinkedList(PERSONS.entrySet());
    }

    public Person findByPrimaryKey(String country, String company,
            String fullname) {
        log.info("findByPrimaryKey");
        if (PERSONS.get(fullname) != null) {
            return (Person) PERSONS.get(fullname);
        } else {
            throw new EntryNotFoundException(
                    "Could not find person with name '" + fullname + "'");
        }
    }

    public void update(Person person) {
        Person actualPerson = findByPrimaryKey(null, null, person.getFullName());
        actualPerson.setCompany(person.getCompany());
        actualPerson.setCountry(person.getCountry());
        actualPerson.setDescription(person.getDescription());
        actualPerson.setDn(person.getDn());
        actualPerson.setPhone(person.getPhone());
    }
}
