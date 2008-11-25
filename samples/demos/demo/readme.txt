Demo application to be used for demonstrating how to convert a legacy JNDI-based
dao implementation written in Java 1.4 to use Spring LDAP, focussing on the Java5
support in Spring LDAP. For reference purposes, a corresponding implementation
using ordinary Java LDAP/JNDI implementation is available in TraditionalPersonDaoImpl.

How to use:
-----------
'mvn test' will start up an LDAP server before running the integration tests that
verify the dao implementation.

'mvn eclipse:eclipse' will construct an Eclipse project for you to use. Import
that project into Eclipse using File/Import/Existing Project, and select this
directory.

You can start converting the org.springframework.ldap.demo.dao.PersonDaoImpl class.
The original traditional implementation, as well as a "solution", is available in
the org.springframework.ldap.demo.solution package. Run the tests after you have
converted a method.
