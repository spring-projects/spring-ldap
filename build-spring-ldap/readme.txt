This is where the master build to create releases of spring-ldap resides.

To build a new release:

1. If you have ivy installed, make sure it is version 1.4 or higher.
   (check your ANT_HOME/lib for ivy-xxx.jar - if it is too old a version,
    remove it or replace it with the one in ../common-build/lib).

2. Update project.properties to contain the new release version, if necessary.

3. From this directory, run:
		ant release
   The release archive will be created and placed in:
		target/release
	
Questions? See http://www.springframework.org/ldap