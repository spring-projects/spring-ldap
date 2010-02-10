Uses Spring 3.0.
Sample application demonstrating how to do the most basic stuff in Spring LDAP.
A very simple dao implementation is provided in
org.springframework.ldap.samples.article.dao.PersonDaoImpl
It demonstrates some basic operations using Spring LDAP. For reference purposes,
a corresponding implementation using ordinary Java LDAP/JNDI implementation is
available in TraditionalPersonDaoImpl.

How to use:
-----------
The project is in a Maven build structure. Make sure you have installed the samples-utils artifact, as this will be
needed for this project to work.

'mvn jetty:run' will start up a web server demonstrating the capabilities. The web application will be available
under http://localhost:8080/spring-ldap-person-article-spring30/

'mvn eclipse:eclipse' will construct an Eclipse project for you to use. Import that project into Eclipse using
File/Import/Existing Project, and select this directory.

'mvn test' will run some integration tests that require the LDAP server to be running. It's recommended to run
'mvn jetty:run' from another terminal window before 'mvn test'.
