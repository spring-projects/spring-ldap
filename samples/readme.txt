Sample application demonstrating the very basics in Spring LDAP
A basic data access class for manipulating a person entry is located at:
org.springframework.ldap.samples.article.dao.PersonDaoImpl
For comparison, a corresponding implementation using traditional JNDI programming is provided in:
org.springframework.ldap.samples.article.dao.TraditionalPersonDaoImpl

To verify the functionality, start the sample webapp using
mvn jetty:run
and navigate to http://localhost:8080/spring-ldap-person-article/showTree.do
