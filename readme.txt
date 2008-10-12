Spring-LDAP 1.2 (Oct 2007)
-----------------------------
http://www.springframework.org/ldap

1. INTRODUCTION

Spring-LDAP is a library to simplify LDAP programming in Java, built on the same
principles as Spring Jdbc. 

The LdapTemplate class encapsulates all the plumbing work involved in traditional LDAP 
programming, such as creating, looping through NamingEnumerations, handling Exceptions
and cleaning up resources. This leaves the programmer to handle the important stuff - 
where to find data (DNs and Filters) and what do do with it (map to and from domain 
objects, bind, modify, unbind, etc.), in the same way that JdbcTemplate relieves the 
programmer of all but the actual SQL and how the data maps to the domain model.

In addition to this, Spring-LDAP provides Exception translation from NamingExceptions
to an unchecked exception hirearchy, as well as several utilities for working with filters,
LDAP paths and Attributes.

As of version 1.2, support for client-side compensating transaction is provided, as well as 
Java 5 generics support with the SimpleLdapTemplate.

2. RELEASE INFO

Spring LDAP requires J2SE 1.4 for running. 
J2SE 1.4, and javacc version 4.0 is required for building the Spring LDAP distributables from source code.
J2EE 1.4 (Servlet 2.3, JSP 1.2) is required for running the example.

The Spring-LDAP release comes in three different distributables:
* spring-ldap-bin-x.x.zip
  ** readme.txt - this readme file.
  ** license.txt - terms and conditions for use, reproduction and distribution
  ** dist/spring-ldap-x.x.jar - the library
  ** dist/spring-ldap-src-x.x.zip - the source code of the library
  ** dist/ivys - ivy configuration
  ** doc/api - api javadoc documentation
  ** doc/reference - full project reference documentation
  ** doc/upgrade - Upgrade guide from 1.1.2
  
* spring-ldap-bin-with-dependencies-x.x.zip
  ** readme.txt - this readme file.
  ** license.txt - terms and conditions for use, reproduction and distribution
  ** dist/spring-ldap-x.x.jar - the library
  ** dist/spring-ldap-src-x.x.zip - the source code of the library
  ** dist/ivys - ivy configuration
  ** lib - all referenced libraries necessary to use spring-LDAP
  ** doc/api - api javadoc documentation
  ** doc/reference - full project reference documentation
  ** doc/upgrade - Upgrade guide from 1.1.2

The sources included in the distributables are for reference use only, however buildable
sources are available in svn on sourceforge on the following URLs:
Sources for this release (1.2 tag):
https://springframework.svn.sourceforge.net/svnroot/springframework/spring-ldap/tags/1.2
Latest sources (trunk): 
https://springframework.svn.sourceforge.net/svnroot/springframework/spring-ldap/trunk

Nightly builds are available for download from:
http://static.springframework.org/downloads/nightly/snapshot-download.php?project=LDAP

Spring-LDAP is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies.

* spring-ldap-x.x.jar
- Contents: The Spring-LDAP library
- Dependencies: Commons Logging, Commons Lang, spring-beans,
                spring-core, spring-context, spring-dao, ldapbp, commons-pool

* spring-ldap-tiger-x.x.jar
- Contents: The Spring-LDAP Java 5 support library
- Dependencies: Commons Logging, Commons Lang, spring-beans,
                spring-core, spring-context, spring-dao, ldapbp, commons-pool

4. WHERE TO START

The distribution contains extensive JavaDoc documentation as well as full reference
documentation and a sample application illustrating different ways to use Spring-LDAP.
The Spring-LDAP homepage can be found at the following URL:

http://www.springframework.org/ldap

There you will find resources related to the project.
