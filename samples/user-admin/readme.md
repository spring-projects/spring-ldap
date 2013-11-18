## User Admin Sample

Sample application demonstrating how to do some real work with Spring LDAP. This is a fully functional LDAP user
administration application. It uses many of the useful concepts in Spring LDAP and would serve as a
good example for best practices and various useful tricks.

### Users and Groups in LDAP

Users in LDAP are usually represented as `organizationalPerson` or `inetOrgPerson` entries in the LDAP tree.
The attributes in these entries represent the basic information on the users, e.g. (subset of available attributes):

* `cn`: Common Name, or full name of the user
* `sn`: Surname
* `mail`: Email address
* `telephoneNumber`: Phone number
* ...several more attributes are available

Groups in LDAP are typically organized in `groupOfName` or `groupOfUniqueName` entries. These entries have a name,
an optional description, and an attribute containing the distinguished names (i.e. the unique identifiers) of all
members of the group. This attribute is named `member` and `uniqueMember` respectively.

Thus, user administration in LDAP typically involves creating and manipulating `orgalizationalPerson` or `inetOrgPerson`
entries and adding or removing references to these entries in `groupOfName` or `groupOfUniqueName` entries.

This application demonstrates to do this easily and efficiently using Spring LDAP.

### Application Structure

The core Spring application context of the sample is defined in resources/applicationContext.xml.
By default this ApplicationContext will start an in-process Apache Directory Server instance, automatically populated
with some test data. The data will be reset every time the application is restarted.

### Running the Example

To run the example, do `gradle jettyRun`, or `mvn jetty:run`, and then navigate to `http://localhost:8080/spring-ldap-user-admin-sample`

It is also possible to run this sample application against a remote LDAP server as opposed to starting an in-process
LDAP server. To do this, use the following system properties:

* `spring.profiles.active` - set this to `no-apacheds` to prevent the in-process ApacheDs to be launched
* `sample.ldap.url` - the URL of the target LDAP server
* `sample.ldap.userDn` - principal to use for authentication against the LDAP server
* `sample.ldap.password` - authentication password
* `sample.ldap.base` - the directory root to use - note that by default all data under this node will be deleted and replaced with test data
* `sample.ldap.clean` - set this property to false to *not* clear the root node
* `sample.ldap.directory.type` - NORMAL or AD. Specify AD if running against Active Directory in order to enable some particular AD tweaks

Example:

    gradle jettyRun -Dspring.profiles.active=no-apacheds -Dsample.ldap.url=ldaps://127.0.0.1:636 \
     -Dsample.ldap.userDn=CN=ldaptest,CN=Users,DC=261consulting,DC=local -Dsample.ldap.password=secret \
     -Dsample.ldap.base=ou=test,dc=261consulting,dc=local -Dsample.ldap.directory.type=AD

    mvn jetty:run -Dspring.profiles.active=no-apacheds -Dsample.ldap.url=ldaps://127.0.0.1:636 \
     -Dsample.ldap.userDn=CN=ldaptest,CN=Users,DC=261consulting,DC=local -Dsample.ldap.password=secret \
     -Dsample.ldap.base=ou=test,dc=261consulting,dc=local -Dsample.ldap.directory.type=AD

This sample uses Bootstrap to present a decent web design - copyright 2013 Twitter, Inc; distributed under the Apache 2 License.