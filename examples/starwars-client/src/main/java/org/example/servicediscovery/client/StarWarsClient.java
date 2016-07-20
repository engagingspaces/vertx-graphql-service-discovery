package org.example.servicediscovery.client;

import io.engagingspaces.servicediscovery.graphql.client.GraphQLClient;
import io.engagingspaces.servicediscovery.graphql.consumer.DiscoveryRegistrar;
import io.engagingspaces.servicediscovery.graphql.consumer.SchemaConsumer;
import io.engagingspaces.servicediscovery.graphql.events.SchemaReferenceData;
import io.engagingspaces.servicediscovery.graphql.query.QueryResult;
import io.engagingspaces.servicediscovery.graphql.query.Queryable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.ServiceReference;
import org.junit.Assert;

import static org.example.servicediscovery.client.StarWarsClient.AuthLevel.DROIDS;
import static org.example.servicediscovery.client.StarWarsClient.AuthLevel.HUMANS;

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

    public SecurityRealm authorizeHuman(String question, String answer) {
        if (question.equals("Give me the first piece of pi") && answer.contains("3.14")) {
            return SecurityRealm.Droids; // To-do: improve security ;)
        } else {
            return SecurityRealm.StarWars;
        }
    }

    @Override
    public void start() {
        registrar = DiscoveryRegistrar.create(vertx);
        SchemaConsumer.startDiscovery(new ServiceDiscoveryOptions().setName(securityRealm(DROIDS)), this);
        SchemaConsumer.startDiscovery(new ServiceDiscoveryOptions().setName(securityRealm(HUMANS)), this);
    }

    @Override
    public void schemaDiscoveryEvent(Record record) {
        if (record.match(new JsonObject().put("name", "StarWarsQuery").put("status", "UP"))) {

            String graphQLQuery = "bla bla";
            JsonObject expected = new JsonObject();

            CompositeFuture.all(
                    queryDirectly(securityRealm(HUMANS), "StarWarsQuery", graphQLQuery),
                    queryFromGraphQLService(graphQLQuery, record),
                    queryFromServiceProxy(graphQLQuery, record)

            ).setHandler(rh -> {
                if (rh.succeeded()) {
                    Assert.assertArrayEquals(
                            new JsonObject[] {expected, expected, expected}, rh.result().list().toArray());
                    LOG.info("GraphQL queries were executed successfully and yielded same results...");
                } else {
                    LOG.error("Failed to execute GraphQL queries", rh.cause());
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

    Future<QueryResult> queryDirectly(String discoveryName, String schemaName, String query) {
        Future<QueryResult> queryFuture = Future.future();

        executeQuery(discoveryName, schemaName, query, null, rh -> {
            if (rh.succeeded()) {
                queryFuture.complete(rh.result());
            } else {
                queryFuture.fail(rh.cause());
            }
        });
        return queryFuture;
    }

    Future<QueryResult> queryFromServiceProxy(String query, Record record) {
        ServiceReference reference = discoveryFor(HUMANS).getReference(record);
        Queryable queryable = reference.cached() == null ? reference.get() : reference.cached();
        Future<QueryResult> queryFuture = Future.future();

        queryable.query(query, rh -> {
            if (rh.succeeded()) {
                queryFuture.complete(rh.result());
            } else{
                queryFuture.fail(rh.cause());
            }
        });
        return queryFuture;
    }

    Future<QueryResult> queryFromGraphQLService(String query, Record record) {
        Future<QueryResult> queryFuture = Future.future();
        GraphQLClient.executeQuery(discoveryFor(HUMANS), record, query, rh -> {
            if (rh.succeeded()) {
                queryFuture.complete(rh.result());
            } else {
                queryFuture.fail(rh.cause());
            }
        });
        return queryFuture;
    }

    ServiceDiscovery discoveryFor(AuthLevel authLevel) {
        return getDiscovery(securityRealm(authLevel)).get();
    }


    String securityRealm(AuthLevel authLevel) {
        // Yeah, that's right, don't given them Droids the full Monty!
        return AuthLevel.DROIDS.equals(authLevel) ? SecurityRealm.Droids.name() : SecurityRealm.StarWars.name();
    }

    @Override
    public DiscoveryRegistrar discoveryRegistrar() {
        return registrar;
    }
}
