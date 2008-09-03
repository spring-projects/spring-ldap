To run the automatic open LDAP EC2 instance launching tests, do:
mvn -Daws.key=<Your AWS Account Key> -Daws.secret.key=<Your AWS secret key> test

You can optionally tune the EC2 launch using the following parameters:
-Daws.ami=<Your AMI image identifier>
-Daws.key=<The AWS key to use when launching the instance>
-Daws.security.group=<The AWS security group to use when launching the instance>
