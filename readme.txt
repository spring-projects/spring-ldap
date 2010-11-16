Spring LDAP 1.3.1 (Nov 2010)
----------------------------
http://www.springframework.org/ldap
http://forum.springframework.org/forumdisplay.php?f=40

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

As of version 1.3.1, there is support for LDIF parsing and Object-Directory Mapping (ODM).

See changelog.txt for detailed information on the changes included in the current release.

2. RELEASE INFO

Spring LDAP requires J2SE 1.4 and Spring 2.x for running. J2SE 1.5 is required for building.
J2EE 1.4 (Servlet 2.3, JSP 1.2) is required for running the example.

"." contains Spring LDAP distribution units (jars and source zip archives), readme, and copyright
"dist" contains the Spring LDAP distribution
"dist/modules" contains the Spring LDAP modules

The -with-dependencies distribution contains the following additional content:

"dist/module-sources" contains the Spring LDAP modules
"docs" contains the Spring LDAP reference manual and API Javadocs
"samples" contains buildable Spring LDAP sample application sources
"lib" contains the Spring LDAP dependencies

Nightly builds are available for download from:
http://static.springframework.org/spring-ldap/downloads/1.3-snapshot-download.php

Spring LDAP is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies.

* spring-ldap-core-1.3.1.RELEASE.jar
- Contents: The Spring LDAP library
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp

* spring-ldap-core-tiger-1.3.1.RELEASE.jar
- Contents: The Spring LDAP Java 5 support library
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp

* spring-ldap-test-1.3.1.RELEASE.jar
- Contents: Support classes that helps LDAP with integration testing.
- Dependencies: Commons Logging, Commons Lang, Commons Pool, spring-beans,
                spring-core, spring-context, spring-jdbc, spring-tx, ldapbp

* spring-ldap-ldif-core-1.3.1.RELEASE.jar
- Contents: The Spring LDAP LDIF parsing library.
- Dependencies: Commons Logging, Commons Lang, spring-beans, spring-core,
                spring-ldap-core

* spring-ldap-ldif-batch-1.3.1.RELEASE.jar
- Contents: The Spring Batch integration layer for the LDIF parsing library.
- Dependencies: Commons Logging, spring-batch, spring-beans, spring-core,
                spring-ldap-core, spring-ldap-ldif-core

* spring-ldap-odm-1.3.1.RELEASE.jar
- Contents: The Object-Directory Mapping (ODM) framework.
- Dependencies: Commons Logging, Commons CLI, spring-beans, spring-ldap-core,
                spring-ldap-core-tiger

4. MAVEN USERS

All major releases of this library are available in the central Maven repository.
Note that the artifacts have changed names between the 1.2.x and 1.3 releases:
spring-ldap is now spring-ldap-core
spring-ldap-tiger is now spring-ldap-core-tiger

This means that in order to use the latest release (1.3.1.RELEASE), you need to
include the following dependencies:

<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core</artifactId>
  <version>1.3.1.RELEASE</version>
</dependency>

For Java 1.5 support:

<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core-tiger</artifactId>
  <version>1.3.1.RELEASE</version>
</dependency>

Milestone releases (such as release candidates) are available from the Spring
framework milestone repo:

<repository>
  <id>spring-milestone</id>
  <name>Spring Portfolio Milestone Repository</name>
  <url>http://s3.amazonaws.com/maven.springframework.org/milestone</url>
</repository>
  
This means that in order to use a milestone or release candidate, you need to
specify the repository above and include the following dependencies:

<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core</artifactId>
  <version>2.0.0.RC1</version>
</dependency>

For Java 1.5 support:

<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core-tiger</artifactId>
  <version>2.0.0.RC1</version>
</dependency>

Nighly builds are published to the snapshot repository:

<repository>
  <id>spring-snapshot</id>
  <name>Spring Portfolio Snapshot Repository</name>
  <url>http://s3.amazonaws.com/maven.springframework.org/snapshot</url>
</repository>

<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core</artifactId>
  <version>2.0.0.CI-SNAPSHOT</version>
</dependency>

For Java 1.5 support:
<dependency>
  <groupId>org.springframework.ldap</groupId>
  <artifactId>spring-ldap-core-tiger</artifactId>
  <version>2.0.0.CI-SNAPSHOT</version>
</dependency>

5. WHERE TO START

This distribution contains documentation and a sample application illustrating
the features of Spring LDAP.

A great way to get started is to review and run the sample application,
supplementing with reference manual material as needed. You will require
Maven 2, which can be downloaded from http://maven.apache.org/, for building
Spring LDAP. To build deployable .war files for all samples, simply access the
"samples" directory and execute the "mvn package" command. Alternatively, go
to a sample and run "mvn jetty:run" to run the sample directly in a Jetty 6 Web
container.

6. ADDITIONAL RESOURCES

The Spring LDAP homepage can be found at the following URL:

    http://www.springframework.org/ldap

Spring LDAP support forums are located at:

    http://forum.springframework.org/forumdisplay.php?f=40

The Spring Framework portal is located at:

    http://www.springframework.org
