## Spring LDAP

[![Join the chat at https://gitter.im/spring-projects/spring-ldap](https://badges.gitter.im/spring-projects/spring-ldap.svg)](https://gitter.im/spring-projects/spring-ldap?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/spring-projects/spring-ldap.svg?branch=main)](https://travis-ci.org/spring-projects/spring-ldap)

[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.spring.io/scans?search.rootProjectNames=spring-ldap)

## Code of Conduct
This project adheres to the Contributor Covenant [code of conduct](CODE_OF_CONDUCT.adoc).
By participating, you  are expected to uphold this code. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

### INTRODUCTION

Spring LDAP is a library to simplify LDAP programming in Java, built on the same
principles as Spring Jdbc. 

The LdapTemplate class encapsulates all the plumbing work involved in traditional LDAP 
programming, such as creating, looping through NamingEnumerations, handling Exceptions
and cleaning up resources. This leaves the programmer to handle the important stuff - 
where to find data (DNs and Filters) and what to do with it (map to and from domain
objects, bind, modify, unbind, etc.), in the same way that JdbcTemplate relieves the 
programmer of all but the actual SQL and how the data maps to the domain model.

In addition to this, Spring LDAP provides Exception translation from NamingExceptions
to an unchecked exception hierarchy, as well as several utilities for working with filters,
LDAP paths and attributes.

For detailed information on the project, please refer to the [reference documentation](https://docs.spring.io/spring-ldap/docs/current/reference/) and [javadocs](https://docs.spring.io/spring-ldap/docs/2.3.x/apidocs/).
See [the changelog](https://github.com/spring-projects/spring-ldap/milestone/28?closed=1) for detailed information on changes in the latest version.

### MAVEN USERS

All major releases of this library are available in the central Maven repository.
The following components are available:

* spring-ldap-core - the core Spring LDAP library
* spring-ldap-core-tiger - the Spring LDAP Java 5 support library
* spring-ldap-test - support classes that help LDAP with integration testing
* spring-ldap-ldif-core - the Spring LDAP LDIF parsing library
* spring-ldap-ldif-batch - the Spring Batch integration layer for the LDIF parsing library
* spring-ldap-odm - The Object-Directory Mapping (ODM) framework

To include e.g. the spring-ldap-core component in your project, use the following dependency tag:

    <dependency>
      <groupId>org.springframework.ldap</groupId>
      <artifactId>spring-ldap-core</artifactId>
      <version>2.3.2.RELEASE</version>
    </dependency>

Milestone releases (such as release candidates) are available from the Spring
framework milestone repo:

    <repository>
      <id>spring-milestone</id>
      <name>Spring Portfolio Milestone Repository</name>
      <url>https://maven.springframework.org/milestone</url>
    </repository>

Snapshot builds produced by the CI builds are available from the Spring framework snapshot repo:

    <repository>
      <id>spring-milestone</id>
      <name>Spring Portfolio Milestone Repository</name>
      <url>https://maven.springframework.org/snapshot</url>
    </repository>

In order to e.g. try out the latest build snapshot of the core module, include the repository above and
use the following dependency tag:

    <dependency>
      <groupId>org.springframework.ldap</groupId>
      <artifactId>spring-ldap-core</artifactId>
      <version>2.0.M1.CI-SNAPSHOT</version>
    </dependency>

Note that while all milestone and snapshot builds have passed the (quite extensive) test suite,
the artifacts here are obviously still work in progress. Feel free to use them for trying out new functionality
and bug fixes for an upcoming version. Please report any problems or bugs in the [issue tracker](https://github.com/spring-projects/spring-ldap/issues).

### ADDITIONAL RESOURCES

* [Official site](https://spring.io/projects/spring-ldap)
* [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-ldap)
* [Issue tracker](https://github.com/spring-projects/spring-ldap/issues)
