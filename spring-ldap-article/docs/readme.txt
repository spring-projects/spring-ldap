LDAP server setup for samples.

We need to semi-manually set up the server environment. I have used
OpenLDAP (http://www.openldap.org/) with a minimal amount of data set up from an
LDIF file.

There is a Windows version of OpenLDAP available at:

 http://lucas.bergmans.us/hacks/openldap/

The LDIF file and sample application both expect to find the base suffix
"dc=jayway,dc=se" in the LDAP Server.

For help in setting up the LDAP environment, three files are supplied:
1. ldap.conf.example
	Modify paths in this file to suit your installation by replacing
	the placeholder [open_ldap_path].
2. base_data.ldif
	Base domain and Admin data as specified in ldap.conf.
3. setup_data.ldif
	The data expected by the integration test cases.
	
Start the LDAP Server:
slapd -d 1 -f ldap.conf.example

Add the LDIF files (the default password in ldap.conf.example is "secret"):
ldapadd -x -D "cn=Manager,dc=jayway,dc=se" -W -f base_data.ldif
ldapadd -x -D "cn=Manager,dc=jayway,dc=se" -W -f setup_data.ldif

Verify the installation by running this search:

ldapsearch -LLL -b dc=jayway,dc=se "(objectclass=*)" dn

It should result in a list that looks like this:

dn: dc=jayway,dc=se

dn: cn=Manager,dc=jayway,dc=se

dn: c=Sweden,dc=jayway,dc=se

dn: c=Norway,dc=jayway,dc=se
...

You should now be all set to run the integration tests.

If you choose to run the tests on another instance, or specifically with
another suffix than the one mentioned above, you may need to modify data in
the following files:

setup_data.ldif
/src/iutest/config/ldap.properties
