/*
 * Copyright (c) 2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.engagingspaces.graphql.servicediscovery.consumer;

import io.engagingspaces.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.graphql.events.SchemaReferenceData;
import io.engagingspaces.graphql.events.SchemaUsageHandler;
import io.engagingspaces.graphql.query.QueryResult;
import io.engagingspaces.graphql.servicediscovery.client.GraphQLClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Interface that facilitates easy subscription to service discovery events related to graphql services.
 * <p>
 * Implementors only need to initialize the {@link SchemaConsumer#discoveryRegistrar()} with a new instance, and then
 * attach to a service discovery instance to listen for events.
 * <p>
 * Service discovery instances are automatically created and closed when needed.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public interface SchemaConsumer extends SchemaAnnounceHandler, SchemaUsageHandler {

    /**
     * Starts listening to discovery events of graphql services that occur in the default service discovery instance.
     * <p>
     * Any `announce` and `usage` events emitted by the service repository that are related to a graphql service
     * are forwarded to the {@link SchemaConsumer#schemaDiscoveryEvent(Record)}
     * and {@link SchemaConsumer#schemaReferenceEvent(SchemaReferenceData)} event handlers, to be handled by clients.
     * <p>
     * If the service discovery doesn't exist, a new instance is automatically created and managed by the
     * {@link DiscoveryRegistrar}.
     * <p>
     * Returns an {@link DiscoveryRegistration} that contains the registered message consumers.
     *
     * @param consumer the instance that will receive the discovery events
     * @return the discovery registration
     */
    static DiscoveryRegistration startDiscovery(SchemaConsumer consumer) {
        return startDiscovery(new ServiceDiscoveryOptions(), consumer);
    }

    /**
     * Starts listening to discovery events of graphql services that occur in the service discovery specified by the
     * {@code options} parameter.
     * <p>
     * Any `announce` and `usage` events emitted by the service repository that are related to a graphql service
     * are forwarded to the {@link SchemaConsumer#schemaDiscoveryEvent(Record)}
     * and {@link SchemaConsumer#schemaReferenceEvent(SchemaReferenceData)} event handlers, to be handled by clients.
     * <p>
     * If the service discovery doesn't exist, a new instance is automatically created and managed by the
     * {@link DiscoveryRegistrar}.
     * <p>
     * Returns an {@link DiscoveryRegistration} that contains the
     * registered message consumers.
     *
     * @param options  the service discovery options
     * @param consumer the consumer of the schema discovery events
     * @return the discovery registration
     */
    static DiscoveryRegistration startDiscovery(ServiceDiscoveryOptions options, SchemaConsumer consumer) {
        Objects.requireNonNull(options, "Service discovery options cannot be null");
        Objects.requireNonNull(consumer, "Schema consumer cannot be null");
        Objects.requireNonNull(consumer.discoveryRegistrar(), "Schema consumer registrar cannot be null");
        return consumer.discoveryRegistrar().startListening(options, consumer, consumer);
    }

    /**
     * Stops listening to discovery events of the service discovery associated with the {@link DiscoveryRegistration}.
     *
     * @param registration  the discovery registration
     * @param consumer      the consumer of the schema discovery events
     */
    static void stopDiscovery(DiscoveryRegistration registration, SchemaConsumer consumer) {
        Objects.requireNonNull(consumer, "Schema consumer cannot be null");
        Objects.requireNonNull(consumer.discoveryRegistrar(), "Schema consumer registrar cannot be null");
        Objects.requireNonNull(registration, "Discovery registration cannot be null");
        Objects.requireNonNull(registration.getDiscoveryOptions(), "Service discovery options cannot be null");

        consumer.discoveryRegistrar().stopListening(registration.getDiscoveryOptions());
    }

    /**
     * Stops listening to discovery events of the service discovery specified in the the
     * {@link ServiceDiscoveryOptions}.
     *
     * @param options  the service discovery options
     * @param consumer      the consumer of the schema discovery events
     */
    static void stopDiscovery(ServiceDiscoveryOptions options, SchemaConsumer consumer) {
        Objects.requireNonNull(consumer, "Schema consumer cannot be null");
        Objects.requireNonNull(consumer.discoveryRegistrar(), "Schema consumer registrar cannot be null");
        Objects.requireNonNull(options, "Service discovery options cannot be null");

        consumer.discoveryRegistrar().stopListening(options);
    }

    /**
     * Unregisters all event consumers associated with the managed service discoveries, then closes the service
     * discoveries.
     *
     * @param consumer the schema event consumer to close
     */
    static void close(SchemaConsumer consumer) {
        Objects.requireNonNull(consumer, "Schema consumer cannot be null");
        Objects.requireNonNull(consumer.discoveryRegistrar(), "Schema consumer registrar cannot be null");
        consumer.discoveryRegistrar().close();
    }

    /**
     * Executes the GraphQL query against the specified schema definition (aka the graphql service name)
     * that is published to the service discovery with the specified name.
     * <p>
     * On success a {@link QueryResult} is returned. The GraphQL query itself may still have failed, so
     * check {@link QueryResult#isSucceeded()} afterwards. If not successful parse errors can be retrieved
     * from {@link QueryResult#getErrors()}.
     * <p>
     * The top-level keys in the `variables` json represent the variable names that are used in the query string, and
     * are passed with their corresponding values to the query executor.
     *
     * @param discoveryName the name of the service discovery
     * @param schemaName    the name of the schema definition to query
     * @param query         the GraphQL query
     * @param resultHandler the result handler
     */
    default void executeQuery(String discoveryName, String schemaName, String query,
                              Handler<AsyncResult<QueryResult>> resultHandler) {
        executeQuery(discoveryName, schemaName, query, null, resultHandler);
    }

    /**
     * Executes the parametrized GraphQL query and its variables against the specified schema definition
     * (aka the graphql service name) that is published to the service discovery with the specified name.
     * <p>
     * On success a {@link QueryResult} is returned. Note that at this point the GraphQL query may still have failed,
     * so be sure to check the {@link QueryResult#getErrors()} property afterwards.
     * <p>
     * The top-level keys in the `variables` json represent the variable names that are used in the query string, and
     * are passed with their corresponding values to the query executor.
     *
     * @param discoveryName the name of the service discovery
     * @param schemaName    the name of the schema definition to query
     * @param query         the GraphQL query
     * @param variables     the variables to pass to the query executor
     * @param resultHandler the result handler
     */
    default void executeQuery(String discoveryName, String schemaName, String query,
                              JsonObject variables, Handler<AsyncResult<QueryResult>> resultHandler) {
        Objects.requireNonNull(schemaName, "Schema definition name cannot be null");
        Objects.requireNonNull(query, "GraphQL query cannot be null");
        Objects.requireNonNull(resultHandler, "Query result handler cannot be null");

        if (!managedDiscoveries().contains(discoveryName)) {
            resultHandler.handle(Future.failedFuture("Service discovery with name '" + discoveryName +
                    "' is not managed by this schema consumer"));
            return;
        }
        ServiceDiscovery discovery = discoveryRegistrar().getDiscovery(discoveryName);
        discovery.getRecord(record -> schemaName.equals(record.getName()), rh -> {
            if (rh.succeeded() && rh.result() != null) {
                GraphQLClient.executeQuery(discovery, rh.result(), query, variables, resultHandler);
            } else {
                resultHandler.handle(Future.failedFuture(
                        "Failed to find published schema '" + schemaName + "' in repository: " + discoveryName));
            }
        });
    }

    /**
     * Gets the service discovery instance with the specified name, if the discoverer is managing it.
     *
     * @param discoveryName the name of the service discovery, or {@code null} to get the default service discovery
     * @return optional that contains the service discovery reference if found, otherwise empty
     */
    default Optional<ServiceDiscovery> getDiscovery(String discoveryName) {
        return Optional.ofNullable(discoveryRegistrar().getDiscovery(discoveryName));
    }

    /**
     * Gets the service discovery names of the discovery instances this consumer is listening to.
     *
     * @return the list of service discovery names
     */
    default List<String> managedDiscoveries() {
        return discoveryRegistrar().serviceDiscoveryNames();
    }

    /**
     * Gets the registrar that is used to manage consumer internal state.
     * <p>
     * Clients only need to create and return a valid instance when implementing a {@link SchemaConsumer}. The
     * registrar is controlled by the consumer interface. You only need to keep a reference to it for as long as it
     * is used.
     *
     * @return the discovery registrar
     */
    DiscoveryRegistrar discoveryRegistrar();
}
