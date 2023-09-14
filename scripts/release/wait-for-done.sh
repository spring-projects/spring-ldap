#!/bin/bash

type cowsay >/dev/null 2>&1 || cowsay() { :; }

VERSION=$1
until http -h --check-status --ignore-stdin https://repo1.maven.org/maven2/org/springframework/ldap/spring-ldap-core/$VERSION/; do sleep 10; clear; done; spd-say "It is now uploaded" && cowsay "It is now uploaded"
