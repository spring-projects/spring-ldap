## Spring LDAP Integration Test Configuration

By default, the Spring LDAP integration tests run against an in-process ApacheDS instance.
To configure the tests to run against an external LDAP server, use the following system properties:

* `-Dspring.profiles.active=no-apacheds` (required to run against external LDAP server)
* `-Durl=<ldap server url>` (defaults to ldap://127.0.0.1:389)
* `-DuserDn=<ldap user dn>`
* `-Dpassword=<ldap password>`
* `-Dbase=<LDAP base DN>` - note that the base node MUST exist and that it will be completely cleared by the tests.
* `-Dadtest` - A number of integration tests uses specific LDAP functionality which is not supported or very hard
  to configure for Active Directory. Use this flag to disable these tests.
