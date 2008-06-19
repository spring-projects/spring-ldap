Sample application demonstrating how to do the most basic stuff in Spring LDAP
A very simple dao implementation is provided in
org.springframework.ldap.samples.article.dao.PersonDaoImpl
It demonstrates some basic operations using Spring LDAP. For recerence purposes,
a corresponding implementation using ordinary Java LDAP/JNDI implementation is
available in TraditionalPersonDaoImpl.

How to use:
-----------
The project is in a maven build structure. Make sure you have installed the samples-utils artifact, as this will be
needed for this project to work.

mvn jetty:run will start up a web server demonstrating the capabilities. The web application will be available
under http://localhost:8080/spring-ldap-person-article/

mvn eclipse:eclipse will construct an eclipse project for you to use. Import that project into eclipse using
File/Import/Existing Project, and select this directory.
