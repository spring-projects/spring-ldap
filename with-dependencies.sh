#!/bin/sh
export MAVEN_OPTS=-Xmx512m
mvn -DskipTests -P release clean javadoc:javadoc docbkx:generate-html docbkx:generate-pdf install
