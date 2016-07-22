# Vert.x GraphQL Service discovery

[![Build Status](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery.svg?branch=master)](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery/)&nbsp;&nbsp;
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4bbc6f66bf524c5dbed8cf1a0466d0e2)](https://www.codacy.com/app/arnold_schrijver/vertx-graphql-service-discovery?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=engagingspaces/vertx-graphql-service-discovery&amp;utm_campaign=Badge_Grade)&nbsp;&nbsp;
[![Bintray](https://img.shields.io/bintray/v/engagingspaces/maven/vertx-graphql-core.svg?maxAge=2592000)](https://bintray.com/engagingspaces/maven/vertx-graphql-core)&nbsp;&nbsp;
[![Apache licensed](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

This library allows you to publish GraphQL schema's as independent services in your Vert.x based microservices environment and execute queries from remote service clients over the (local or clustered) Vert.x event bus.
The library deals with the transfer of GraphQL query string to the appropriate service, and the return of query Json-formatted query results, or a list of parse errors if the query failed.

## Table of contents

- [Getting started](#getting-started)
  - [Using with Gradle](#using-with-gradle)
  - [Using with Maven](#using-with-maven)
  - [Building from source](#building-from-source)

## Getting started

### Using with Gradle

Publishers of a GraphQL schema need to add a dependency on `vertx-graphql-publisher`:
```
repositories { 
  maven { 
    url "http://dl.bintray.com/engagingspaces/maven" 
  } 
}

dependencies {
  compile 'io.engagingspaces:vertx-graphql-publisher:0.8.2'
}
```
Consumers of a published GraphQL service that want to execute queries need a dependency on `vertx-graphql-consumer`:
```
repositories { 
  maven { 
    url "http://dl.bintray.com/engagingspaces/maven" 
  } 
}

dependencies {
  compile 'io.engagingspaces:vertx-graphql-consumer:0.8.2'
}
```
### Using with Maven

In order to resolve the Bintray dependencies the following repository settings can be added to your `settings.xml`:
```
<?xml version="1.0" encoding="UTF-8" ?>
<settings xmlns='http://maven.apache.org/SETTINGS/1.0.0' 
          xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
          xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0
                                  http://maven.apache.org/xsd/settings-1.0.0.xsd'>
 
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-engagingspaces-maven</id>
                    <name>bintray</name>
                    <url>http://dl.bintray.com/engagingspaces/maven</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-engagingspaces-maven</id>
                    <name>bintray-plugins</name>
                    <url>http://dl.bintray.com/engagingspaces/maven</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```
Or you can add the repository defintion directly in your `pom.xml`.

When using Maven a publisher of a GraphQL schema needs to add the following dependency to the `pom.xml`:
```
<dependency>
    <groupId>io.engagingspaces</groupId>
    <artifactId>graphql-publisher</artifactId>
    <version>0.8.2</version>
</dependency>
```
And consumers of a GraphQL service need to add the `vertx-graphql-consumer` dependency to their `pom.xml`:
```
<dependency>
    <groupId>io.engagingspaces</groupId>
    <artifactId>graphql-publisher</artifactId>
    <version>0.8.2</version>
</dependency>
```

### Building from source

To build from source, first `git clone https://github.com/engagingspaces/vertx-graphql-service-discovery`, then:
```
./gradlew clean build
```
