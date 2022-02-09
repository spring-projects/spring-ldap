A Hello World Spring LDAP application using Spring Boot

The application is protected by Spring Security and uses an embedded UnboundID container for its LDAP server.

You can authenticate with HTTP basic using `bob`/`bobspassword`:

```bash
curl --user bob:bobspassword localhost:8080
```

And you should see the response:

```bash
Hello, bob
```

Also, you can hit the `cn` endpoint which uses `LdapTemplate` to query the datastore for the user's `cn` attribute value, like so:

```bash
curl --user bob:bobspassword localhost:8080/cn
```

This should result in:

```bash
[
    "Bob Hamilton"
]
```

To run the example, do `./gradlew :bootRun`.
