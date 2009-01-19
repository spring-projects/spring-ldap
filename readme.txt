Spring LDAP 1.3.0.RC1 (Oct 2008)
--------------------------------
http://www.springframework.org/ldap

1. INTRODUCTION

Spring LDAP is a library to simplify LDAP programming in Java, built on the same
principles as Spring Jdbc. 

The LdapTemplate class encapsulates all the plumbing work involved in traditional LDAP 
programming, such as creating, looping through NamingEnumerations, handling Exceptions
and cleaning up resources. This leaves the programmer to handle the important stuff - 
where to find data (DNs and Filters) and what do do with it (map to and from domain 
objects, bind, modify, unbind, etc.), in the same way that JdbcTemplate relieves the 
programmer of all but the actual SQL and how the data maps to the domain model.

In addition to this, Spring LDAP provides Exception translation from NamingExceptions
to an unchecked exception hirearchy, as well as several utilities for working with filters,
LDAP paths and Attributes.

As of version 1.2, support for client-side compensating transaction is provided, as well as 
Java 5 generics support with the SimpleLdapTemplate.

See changelog for detailed information on the thanges included in the current release.

2. RELEASE INFO

Spring LDAP requires J2SE 1.4 and Spring 2.x for running.
J2SE 1.5, and javacc version 4.0 is required for building the Spring LDAP distributables from source code.
J2EE 1.4 (Servlet 2.3, JSP 1.2) is required for running the example.

The sources included in the distributables are for reference use only, however buildable
sources are available in svn on sourceforge on the following URLs:
Sources for this release (1.3-rc1 tag):
https://springframework.svn.sourceforge.net/svnroot/springframework/spring-ldap/tags/1.3-rc1
Latest sources (trunk): 
https://springframework.svn.sourceforge.net/svnroot/springframework/spring-ldap/trunk

Nightly builds are available for download from:
http://static.springframework.org/downloads/nightly/snapshot-download.php?project=LDAP

Spring LDAP is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies.

* spring-ldap-core-x.x.jar
- Contents: The Spring LDAP library
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp

* spring-ldap-core-tiger-x.x.jar
- Contents: The Spring LDAP Java 5 support library
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp

* spring-ldap-test-x.x.jar
- Contents: Support classes that helps LDAP withintegration testing.
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp
4. MAVEN USERS

All major releases of this library are available in the central maven repository. Note that the artifacts
have changed names between the 1.2.x and 1.3 releases:
spring-ldap is now spring-ldap-core
spring-ldap-tiger is now spring-ldap-core-tiger

Milestone releases (such as release candidates) are available from the spring framework milestone repo:
<repository>
  <id>spring-milestone</id>
  <name>Spring Portfolio Milestone Repository</name>
  <url>http://s3.amazonaws.com/maven.springframework.org/milestone</url>
</repository>
  
This means that in order to use the latest release (1.3.0.RC1), you need to specify the 
repository above and include the following dependencies:
<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core</artifactId>
  <version>1.3.0.RC1</version>
</dependency>

For java 1.5 support:
<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core-tiger</artifactId>
  <version>1.3.0.RC1</version>
</dependency>

Nighly builds are published to the snapshot repository:

<repository>
  <id>spring-snapshot</id>
  <name>Spring Portfolio Snapshot Repository</name>
  <url>http://s3.amazonaws.com/maven.springframework.org/snapshot</url>
</repository>

5. WHERE TO START

The distribution contains extensive JavaDoc documentation as well as full reference
documentation and a sample application illustrating different ways to use Spring LDAP.
The Spring LDAP homepage can be found at the following URL:

http://www.springframework.org/ldap

There you will find resources related to the project.
