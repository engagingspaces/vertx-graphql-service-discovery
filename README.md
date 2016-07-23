# Vert.x GraphQL Service discovery

[![Build Status](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery.svg?branch=master)](https://travis-ci.org/engagingspaces/vertx-graphql-service-discovery/)&nbsp;&nbsp;
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4bbc6f66bf524c5dbed8cf1a0466d0e2)](https://www.codacy.com/app/arnold_schrijver/vertx-graphql-service-discovery?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=engagingspaces/vertx-graphql-service-discovery&amp;utm_campaign=Badge_Grade)&nbsp;&nbsp;
[![Bintray](https://img.shields.io/bintray/v/engagingspaces/maven/vertx-graphql-core.svg?maxAge=2592000)](https://bintray.com/engagingspaces/maven/vertx-graphql-core)&nbsp;&nbsp;
[![Apache licensed](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

This library allows you to publish GraphQL schema's as independent services in your Vert.x based microservices environment and execute queries from remote service clients over the (local or clustered) Vert.x event bus.
The library deals with the transfer of GraphQL query strings to the appropriate service, and returning of Json-formatted query results, or a list of parse errors in case of parse errors.

**Note**: While code samples are in Java, service discovery clients can be implemented in any JVM language supported by [Vert.x](#vertx---building-reactive-polyglot-applications-at-scale).

## Table of contents

- [Technology](#technology)
  - [Vert.x](#vertx---building-reactive-polyglot-applications-at-scale)
  - [GraphQL](#graphql---application-layer-query-language-specification)
  - [Vert.x and GraphQL: happy together!](#vertx-and-graphql-happy-together)
- [Getting started](#getting-started)
  - [Using with Gradle](#using-with-gradle)
  - [Using with Maven](#using-with-maven)
  - [Building from source](#building-from-source)
- [Publishing a GraphQL schema](#publishing-a-graphql-schema)
    - [Using a `SchemaPublisher` implementation](#using-a-schema-publisher-implementation)
    - [Using the `GraphQLService` directly](#using-the-graphqlservice-directly)
- [Consuming and querying a GraphQL service](#consuming-and-querying-a-graphql-service)
    - [Using a `SchemaConsumer` implemention](#using-a-schema-consumer-implementation)
    - [Using the `GraphQLClient` directly](#using-the-graphqlclient-directly)
- [Compatibility](#compatibility)
- [Known issues](#known-issues)
- [Contributing](#contributing)
- [Acknowledgments](#acknowledgments)
- [License](#license)

## Technology

Vert.x GraphQL Service discovery is implemented in Java 8 and based on the interesting and innovative technologies [Vert.x](http://vertx.io/) from [Eclipse](http://www.eclipse.org/) and [GraphQL](http://graphql.org/) from [Facebook](https://www.facebook.com/). The code is an extension to the recently released (as of version `3.3.0`) [Vert.x Service discovery](http://vertx.io/docs/vertx-service-discovery/java/) module (one of a range of different microservices modules that were introduced with this release) 

### Vert.x - Building reactive polyglot applications at scale

Vert.x was originally created by Tim Fox in 2011 while working for VMWare, and is now part of the [Eclipse Foundation](http://www.eclipse.org/org/foundation/) (see [Vert.x on wikipedia](https://en.wikipedia.org/wiki/Vert.x)). Vert.x is a toolkit that allows development of event-driven, non-blocking application code with ease and create highly scalable, concurrent internet applications.

With Vert.x you run your code in so-called Verticles that are guaranteed to run on the same thread (except when they are deployed as workers in a thread pool). This alleviates the developer from creating complex and error-prone concurrent Java code, and makes it easy to fully utilize all your processor cores and communicate between distributed verticles in a cloud-based cluster (e.g. by using the [Hazelcast Cluster Manager](http://vertx.io/docs/vertx-hazelcast/java/) module or the [Apache Ignite](http://vertx.io/docs/vertx-ignite/java/) cluster manager module that are part of the toolkit)

Also Vert.x is polyglot which enables you to write verticles in any JVM-based programming language. There is out-of-the-box support for Java, JavaScript, Groovy, Ruby, Ceylon and Scala). After creating your code using your favourite language it can seamlessly interoperate with other Vert.x verticles written in different languages.

Finally Vert.x's modular and and non-opinionated toolkit design makes it very versatile and widely applicable. Where possible Vert.x lets you choose your own technologies and development practices. The toolkit comes with [many standard modules](http://vertx.io/docs/#explore) out of the box that provide additional features that you can add as needed. 
But adding additional modules is entirely optional. You can readily start with a single dependency on the light-weight [Vert.x Core](http://vertx.io/docs/vertx-core/java/) module, and run it as stand-alone service or fully embedded in your code, hidden from clients. Spinning up a full-blown Netty-based HTTP server then requires just 3 lines of code (or a single line if you value conciseness above readibility. Scala users, take note! :wink: :wink:).

#### More information

- The [Vert.x website](http://vertx.io/docs) has lots of documentation to get you started
- Almost every feature is demonstrated in [vertx-examples](https://github.com/vert-x3/vertx-examples) on Github
- And also check this collection of [awesome Vert.x resources](https://github.com/vert-x3/vertx-awesome)

### GraphQL - Query language specification for application data layers

The [GraphQL specification](http://graphql.org/) and its [reference implementation in Javascript](https://github.com/graphql/graphql-js) were released to the open-source community by Facebook in 2015 after having used it internally in production for several years as a data query language and runtime to improve clarity and accessibility to the data exposed by the many services of the Facebook website, and allow decoupled development of client- and service-side codebases.

GraphQL provides a new and interesting way of exposing the data in your application layer to clients, that can yield significant benefits over more 'traditional' REST and HATEOAS styles of communication, especially as projects get bigger and REST API designs more complex, getting ever more endpoints, URL parameters and new caching requirements as a consequence.

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
  
The term 'GraphQL query' might lead you to believe that GraphQL is only about querying. The term is a bit misleading, because there are also so-called *mutation queries* where you can modify data. You could compare mutations and queries to *commands* and *queries* in a CQRS design. The GraphqL queries are similar to the de-normalized data projections that live on the query-side, except that with GraphQL you don't have to define the projections on the server-side. The front-end developer is in charge here.

GraphQL is not a golden hammer however, and there are still valid cases where more restful API designs are in place. Also GraphQL is still relatively new and under heavy development. So do your homework well. 

#### More information

There are many interesting sources of information on GraphQL to be found on the internet, and a very active community. Biggest application at the moment is with other Facebook project like [Relay]() and [ReactJS]() to create nicely decoupled dynamic web front-ends and back-ends that communicate very efficiently with the backend.

But to get you started, here a couple of interesting resources to check:

- Nice and gentle [Introduction to GraphQL](https://learngraphql.com/basics/introduction) by Kadira
- View [Zero to GraphQL in 30 minutes](https://www.youtube.com/embed/UBGzsb2UkeY) on YouTube
- And check the list of [Awesome GraphQL resources](https://github.com/chentsulin/awesome-graphql) for some great community projects

### Vert.x and GraphQL: happy together!

The features described above already make Vert.x ideally suited as enabling technology and backbone in decoupled microservices environments. Together with the query capabilities offered by GraphQL a set of new, interesting ways to communicate between verticles becomes available.

Currently this project is using the [graphq-java](https://github.com/graphql-java/graphql-java) library implementation developed for Java 6. A Vert.x-based reimplementation might outperform (see [benchmarks](https://www.techempower.com/benchmarks/)) other current GraphQL server implementations (maybe not the [Go-based ones](https://github.com/chentsulin/awesome-graphql#lib-go), but that would be an interesting battle) and it would be the first fully asynchronous GraphQL server.

Vert.x itself is an ideal server implementation platform for GraphQL. Most server implementations and almost all examples you'll find on the internet are using [node](https://nodejs.org/en/) and [express](https://expressjs.com/). 

GraphQL also brings something extra to Vert.x:

In larger Vert.x deployments the management of message bus endpoint addresses, consumers and message payloads can become quite involved. There are already some modules to ease this burden and simplify the architecture, of which the [Vert.x Service Discovery](https://github.com/vert-x3/vertx-service-discovery) module is one.

Service discovery allows you, among others, to publish [service proxies](http://vertx.io/docs/vertx-service-proxy/java/) to provide RPC-style communication between service proxy clients (polyglot) and their remote service implementations (also polyglot).

With GraphQL and this project you get an additional `graphql-service` discovery type, that lets you publish GraphQL schema's and consume them remotely over the event bus. This allows for a more data-oriented communication style, and a clean abstraction of your data layer. GraphQL is ideally suited for microservices architectures, where you can have a GraphQL schema's expose the bounded context of your microservices. When implemented well it will provide a tremendously powerful and versatile data layer to your architecture that is both asynchronous and fully distributed.

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

## Publishing a GraphQL schema

As mentioned this project is an extension to the [vertx-service-discovery](https://github.com/vert-x3/vertx-service-discovery) module and provides an additional service type `graphql-service` for publishing GraphQL schema definitions as services.

To get started a schema needs to be published. This can be accomplished in multiple different ways, which each provide you with different levels of convenience versus control. But first you'll need to provide a `SchemaDefinition` that exposes an instance of [`GraphQLSchema`](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/GraphQLSchema.java):

```java
public class DroidsSchema implements SchemaDefinition {

    public static DroidsSchema get() {
        return new DroidsSchema();
    }

    @Override
    public GraphQLSchema schema() {
        return droidsSchema;
    }
    
    static final GraphQLObjectType queryType = newObject()
            .name("QueryType") // Query name == graphql service name. Suffix of proxy address
            .field(newFieldDefinition()
                    .name("droid")
                    .type(droidType)
                    .argument(newArgument()
                            .name("id")
                            .description("id of the droid")
                            .type(new GraphQLNonNull(GraphQLString))
                            .build())
                    .dataFetcher(DroidsData.getDroidDataFetcher())
                    .build())
            .build();

    static final GraphQLSchema droidsSchema = GraphQLSchema.newSchema()
            .query(queryType)
            .build();
}
```

### Using a `SchemaPublisher` implementation

Most convenient and easy to use is publication of GraphQL services using a [`SchemaPublisher`](https://github.com/engagingspaces/vertx-graphql-service-discovery/blob/master/graphql-publisher/src/main/java/io/engagingspaces/servicediscovery/graphql/publisher/SchemaPublisher.java).

A schema publisher is implemented as an interface so you can attach it to any class without limiting its extensibility (e.g. your verticle can still derive from `AbstractVerticle`). The only requirement is that you provide a valid instance of a `SchemaRegistrar` from the overridden `SchemaPublisher.schemaRegistrar()` method.

The registrar will manage the state of the publisher, consisting of a list of `SchemaRegistration` items for each published GraphQL schema, the service discoveries (one or more) to which the services are published, a `MessageConsumer` for the service implementation as well as message consumers for schema-related (`publish` and `unpublish`) service discovery events.

But providing the instance is all that's needed to get you started:

```java
public class DroidsServer extends AbstractVerticle implements SchemaPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(DroidsServer.class);
    private SchemaRegistrar registrar;  // need to keep hold of registrar reference

    @Override
    public void start(Future<Void> startFuture) {
        registrar = SchemaRegistrar.create(vertx);
        SchemaPublisher.publishAll(this, new ServiceDiscoveryOptions()
                .setName("my-graphql-publisher"), rh -> {
            if (rh.succeeded()) {
                LOG.info("Published Droids schema to StarWars world...");
                startFuture.complete();
            } else {
                startFuture.fail(rh.cause());
            }
        }, DroidsSchema.get());
    }

    @Override
    public void schemaPublished(SchemaRegistration registration) {
        LOG.info("Schema " + registration.getSchemaName() + " is now " + 
                registration.getRecord().getStatus());
    }

    @Override
    public void schemaUnpublished(SchemaRegistration registration) {
        LOG.info("Schema " + registration.getSchemaName() + " was " + 
                registration.getRecord().getStatus());
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        SchemaPublisher.close(this, rh -> {
            if (rh.succeeded()) {
                stopFuture.complete();
            } else {
                stopFuture.fail(rh.cause());
            }
        });
    }

    @Override
    public SchemaRegistrar schemaRegistrar() {
        return registrar;  // provide a valid instance here..
    }
}
```

### Using the `GraphQLService` directly

Alternatively you can publish a GraphQL schema directly by invoking a static method on `GraphQLService` and waiting for the result handler to return the schema registration:

```java
GraphQLService.publish(vertx, serviceDiscovery, schemaDefinition, metadata, rh -> {
    if (rh.succeeded()) {
        // Return the schema registration that is the result
        resultHandler.handle(Future.succeededFuture(rh.result()));
    } else {
        resultHandler.handle(Future.failedFuture(rh.cause()));
    }
});
```

When publishing this way without using a schema publisher be aware that you must manually manage the resources (e.g. unregister message consumer, creating/closing service discoveries) yourself as well as create your own handlers for `publish` and `unpublish` events. Refer to the [Vert.x Service discovery](http://vertx.io/docs/vertx-service-discovery/java/) documentation for more info.

## Consuming and querying a GraphQL service

Just as with publishing there are multiple ways to consume a GraphQL service that was published. Most convenient once again is using a `SchemaConsumer`, but you can also query directly using one of the `GraphQLClient` static methods or even by using standard [vertx-service-discovery](https://github.com/vert-x3/vertx-service-discovery) to get to a `Queryable` service proxy manually. 

### Using a `SchemaConsumer` implemention

Easiest is to implement the `SchemaConsumer` interface on the class where you want to retrieve query results. The only requirement is that you provide a valid instance of a `DiscoveryRegistrar` that manages creation / closing of (one or more) service discoveries and registration / unregistration of service discovery event handlers.

When using the consumer the standard `announce` and `usage` discovery events from the managed service discoveries are caught and - when they are related to a `graphql-service` - translates them to the more convenient `schemaDiscoveryEvent` and `schemaReference` event respectively.

Let's see how this looks like in code:

```java
public class StarWarsClient extends AbstractVerticle implements SchemaConsumer {

    public enum AuthLevel {
        DROIDS,
        HUMANS
    }

    public enum SecurityRealm {
        Droids,
        StarWars
    }

    private static final Logger LOG = LoggerFactory.getLogger(StarWarsClient.class);
    private DiscoveryRegistrar registrar;

    // To-do: improve security to fool TensorFlow ;-)
    public SecurityRealm authorizeHuman(String question, String answer) {
        if (question.equals("Give me the first piece of pi") && answer.contains("3.14")) {
            return SecurityRealm.Droids;
        } else {
            return SecurityRealm.StarWars;
        }
    }

    @Override
    public void start() {
        registrar = DiscoveryRegistrar.create(vertx);
        // Subscribe to multiple service discoveries (here different auth levels have different services).
        SchemaConsumer.startDiscovery(new ServiceDiscoveryOptions().setName(securityRealm(DROIDS)), this);
        SchemaConsumer.startDiscovery(new ServiceDiscoveryOptions().setName(securityRealm(HUMANS)), this);
    }

    @Override
    public void schemaDiscoveryEvent(Record record) {
        if (record.match(new JsonObject().put("name", "StarWarsQuery").put("status", "UP"))) {
            String schemaName = record.getName()  // same as root query name in GraphQL schema
            String graphQLQuery = "foo bar";      // your query here..
            JsonObject expected = new JsonObject();

            executeQuery(discoveryName, schemaName, query, null, rh -> {
                if (rh.succeeded()) {
                    QueryResult result = rh.result();
                    if (result.isSucceeded()) {
                        JsonObject queryData = result.getData();
                        // Do something interesting with your data..
                    } else {
                        List<QueryError> errors = result.getErrors();
                        LOG.error("Failed to execute GraphQL query with " + errors.size() + " parse errors);
                    }
                } else {
                    LOG.error("Failed to execute GraphQL query", rh.cause());
                }
            });
        }
    }

    @Override
    public void schemaReferenceEvent(SchemaReferenceData referenceInfo) {
        Record record = referenceInfo.getRecord();
        LOG.info("Service " + record.getName() + " was " + referenceInfo.getStatus());
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        SchemaConsumer.close(this);
    }

    @Override
    public DiscoveryRegistrar discoveryRegistrar() {
        return registrar;  // provide a valid instance here..
    }
}

```
Also take a look at the [example code](https://github.com/engagingspaces/vertx-graphql-service-discovery/tree/master/examples) in the project.

### Using the `GraphQLClient` directly

When not using a consumer you can also invoke a static method on `GraphQLClient` to execute a query or just retrieve the `Queryable` service proxy interface. For all calls to the graphql client you need to provide your own service discovery instance, as well as a published `graphql-service` record. Also you have to manage all resources and discovery event subscriptions yourself.

```java
GraphQLClient.executeQuery(serviceDiscoveryFor(HUMANS), record, query, rh -> {
    if (rh.succeeded()) {
        queryFuture.complete(rh.result());
    } else {
        queryFuture.fail(rh.cause());
    }
});
```

## Compatibility

* Oracle Java `8`
* Gradle `2.14`
* Vert.x `3.3.0`
* GraphQL Java `2.0.0`

## Known issues

- There are two tests that need to be fixed that are now `@Ignore`d
  - One is related to `SchemaPublisher.publishAll()` that has synchronization issues in the test (implementation probably okay, but you best use `publish()` until test code is fixed)
- A build issue, not related to code, but codacy coverage automation does not work due to some exception thrown (see issue #1)

## Contributing

All your feedback and help to improve this project is very welcome. Please create issues for your bugs and enhancement request, and better yet, contribute directly by creating a PR.

When reporting an issue, please add a detailed instruction, and if possible a code snippet or test that can be used as a reproducer of your issue.

When creating a pull request, please adhere to the Vert.x coding style, and create tests with your code so it keeps providing a good test coverage level. PR's without tests are not accepted unless they only have minor changes.

## Acknowledgments

This library was made possible due to many great efforts in the open-source community, but especially to the works of the Vert.x team (Tim Fox, Clement Escoffier, Paulo Lopes, Julien Viet et al), the GraphQL team at Facebook (Lee Byron et al), and Andreas Marek who initiated the graphql-java on Github and from whom the StarWars test data was adapted.

## License

This project [vertx-graphql-service-discovery](https://github.com/engagingspaces/vertx-graphql-service-discovery) is licensed under the [Apache Commons v2.0](https://github.com/engagingspaces/vertx-graphql-service-discovery/LICENSE) license.

Copyright &copy; 2016 Arnold Schrijver and other [contributors](https://github.com/engagingspaces/vertx-graphql-service-discovery/graphs/contributors)
