Sample application demonstrating how to do the most basic stuff in Spring LDAP.
A very simple dao implementation is provided in org.springframework.ldap.samples.plain.dao.PersonDaoImpl
It demonstrates some basic operations using Spring LDAP.

The core Spring application context of the sample is defined in resources/applicationContext.xml.
This ApplicationContext will start an in-process Apache Directory Server instance, automatically populated
with some test data. The data will be reset every time the application is restarted.

To run the example, do `gradle jettyRun` or `mvn jetty:run`, and then navigate to `http://localhost:8080/spring-ldap-plain-sample`
