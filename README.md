# Vert.x GraphQL Service discovery

[![Build Status](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery.svg?branch=master)](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery/)&nbsp;&nbsp;
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4bbc6f66bf524c5dbed8cf1a0466d0e2)](https://www.codacy.com/app/arnold_schrijver/vertx-graphql-service-discovery?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=engagingspaces/vertx-graphql-service-discovery&amp;utm_campaign=Badge_Grade)&nbsp;&nbsp;
[![Bintray](https://img.shields.io/bintray/v/engagingspaces/maven/vertx-graphql-core.svg?maxAge=2592000)](https://bintray.com/engagingspaces/maven/vertx-graphql-core)&nbsp;&nbsp;
[![Apache licensed](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

This library allows you to publish GraphQL schema's as independent services in your Vert.x based microservices environment and execute queries from remote service clients over the (local or clustered) Vert.x event bus.
The library deals with the transfer of GraphQL query string to the appropriate service, and the return of query Json-formatted query results, or a list of parse errors if the query failed.

## Table of contents

- [About](#about)
  - [Vert.x](#vertx---building-reactive-polyglot-applications-at-scale)
  - [GraphQL](#graphql---application-layer-query-language-specification)
  - [Vert.x and GraphQL: happy together!](#vertx-and-graphql-happy-together)
- [Getting started](#getting-started)
  - [Using with Gradle](#using-with-gradle)
  - [Using with Maven](#using-with-maven)
  - [Building from source](#building-from-source)

## About

Vert.x GraphQL Service discovery is implemented in Java 8 and based on [Vert.x](http://vertx.io/) from [Eclipse](http://www.eclipse.org/) and [GraphQL](http://graphql.org/) from [Facebook](https://www.facebook.com/). The code is an extension of the recently released (as of version `3.3.0`) [Vert.x Discovery service library](http://vertx.io/docs/vertx-service-discovery/java/) (one of a set of microservices modules that come with this release) 

### Vert.x - Building reactive polyglot applications at scale

Vert.x is a toolkit that allows development of event-driven, non-blocking application code with ease and create highly scalable, concurrent applications.

With Vert.x you run your code in so-called Verticles that are guaranteed to run on the same thread (except when they are workers in a thread pool). This alleviates the developer from creating complex and error-prone concurrent Java core, and makes it easy to scale to all processor cores and communicate between verticles in a cluster (e.g. by using the [Hazelcast Cluster Manager](http://vertx.io/docs/vertx-hazelcast/java/) module or [Apache Ignite](http://vertx.io/docs/vertx-ignite/java/) cluster manager module)

Also Vert.x is polyglot and enables you to write verticles using any JVM-based programming language (e.g. Java, JavaScript, Groovy, Ruby, Ceylon and Scala) after which they can seamlessly interoperate with verticles written in other languages.

Finally Vert.x is very versatile and non-opinionated. Whenever possible it lets you choose your own technologies and practices. The toolkit consists of [many standard modules](http://vertx.io/docs/#explore) offering additional features that you can add as needed. You can already start by adding a dependency on [Vert.x Core](http://vertx.io/docs/vertx-core/java/) which is very light-weight and can run as a stand-alone service or fully embedded and hidden from clients.

#### More information

- The [Vert.x website] has lots of documentation to get you started
- Almost every feature is demonstrated in [vertx-examples](https://github.com/vert-x3/vertx-examples) on Github
- And also check this collection of [awesome Vert.x resources](https://github.com/vert-x3/vertx-awesome)

### GraphQL - Application layer query language specification

The [GraphQL specification](http://graphql.org/) and a [reference implementation in Javascript](https://github.com/graphql/graphql-js) were released to the open-source community by Facebook in 2015 after using it internally in production for several years as data query language and runtime to interact with their many services.

GraphQL provides a new and interesting way of exposing the data in your application layer to clients, that can yield significant benefits over more 'traditional' REST and HATEOAS styles of communication, especially when compared to large and complex REST API designs, with many endpoints and URL parameters.

GraphQL allows you to:

- Expose the data/domain model of your application by defining one or more server-side GraphQL schema's
  - Instead of many REST endpoints, there can be just one endpoint that recieves all client-side queries
- Query the exposed data model by sending queries in a comprehensive query language to receive json response
  - Instead of having many query parameters, or additional endpoints, clients specify the expected data format in the query
- Decouple back-end and front-end development processes, having each programming against a unified data model
  - Instead of a REST API exposing increasingly more denormalized entry points the API data model stays clean, unpolluted and reflects the solution domain
  - Instead of front-end developers having to wait for endpoints to be delivered on the back-end, they can define queries themselves for any additional (denormalized) view they need
- Optimize the communication between clients and servers and greatly simplify caching strategies
 - Instead of many HTTP requests to receive some aggregate data set, a full resultset can be retrieved in a single query call
 - Instead of complex cache storage and pruning strategies, the GraphQL query language allows much easier cache management
  
GraphQL is not a golden hammer however, and there still valid cases to have more restful API designs. And also GraphQL is still relatively new and under heavy development. So inform yourself well. 

#### More information

There are many interesting source of information on GraphQL to be found on the internet, like using it together with [Relay]() and [ReactJS]() to create nicely decoupled dynamic web front-ends and back-ends that communicate very efficiently.

But to get you started, here a couple of interesting resources to check:

- Nice [Introduction to GraphQL](https://learngraphql.com/basics/introduction) by Kadira
- View [Zero to GraphQL in 30 minutes](https://www.youtube.com/embed/UBGzsb2UkeY) on YouTube
- And check the list of [Awesome GraphQL resources](https://github.com/chentsulin/awesome-graphql) for some great community projects

### Vert.x and GraphQL: happy together!

The features described above already make Vert.x ideally suited as enabling technology and backbone in decoupled microservices environments. Together with the query capabilities offered by GraphQL a set of new, interesting ways to communicate between verticles, becomes available.

In larger Vert.x deployments the management of message bus endpoint addresses and message payloads can become quite involved. There are already some modules to ease this burden and simplify the architecture, of which the [Vert.x Service Discovery](https://github.com/vert-x3/vertx-service-discovery) module is one.

Service discovery allows you, among others, to publish [service proxies](http://vertx.io/docs/vertx-service-proxy/java/) which offer (polyglot) RPC-style communication between service proxy clients and remote service implementations.

With this project you get an additional `graphql-service` discovery type, that lets you publish GraphQL schema's and consume them remotely over the event bus, thereby allowing a more data-oriented communication style. If implemented well it will provide a tremendously powerful and versatile, asynchronous, distributed data layer to your architecture.

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

## Acknowledgments

This library was made possible due to many great efforts in the open-source community, but especially to the works of the Vert.x team (Tim Fox, Clement Escoffier, Paulo Lopes, Julien Viet et al), the GraphQL team at Facebook (Lee Byron et al), and Andreas Marek who initiated the graphql-java on Github and from whom the StarWars test data was adapted.
