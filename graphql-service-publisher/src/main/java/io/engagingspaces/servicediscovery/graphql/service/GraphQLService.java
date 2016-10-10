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

package io.engagingspaces.servicediscovery.graphql.service;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import io.engagingspaces.graphql.query.Queryable;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaPublisher;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistration;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaDefinition;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.spi.ServiceType;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.engagingspaces.graphql.query.Queryable.SERVICE_TYPE;

/**
 * {@link ServiceType} for GraphQL services.
 * Consumers receive a suitable service proxy to handle GraphQL queries and mutations.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public interface GraphQLService extends ServiceType {

    /**
     * Publish a GraphQL schema for querying.
     * <p>
     * On success a {@link SchemaRegistration} is returned. It contains the message consumer of the
     * {@link Queryable} service proxy that supplies the published {@link SchemaDefinition}, the published service
     * discovery record, and the {@link ServiceDiscovery} it was published to.
     * <p>
     * You need to keep the returned {@link Record} as long as the schema is published.
     *
     * @param vertx         the vert.x instance
     * @param discovery     the service discovery instance
     * @param definition    the service proxy instance exposing the graphql schema
     * @param resultHandler the result handler that returns the
     * {@link SchemaRegistration}
     */
    static void publish(Vertx vertx, ServiceDiscovery discovery, SchemaDefinition definition,
                        Handler<AsyncResult<SchemaRegistration>> resultHandler) {
        publish(vertx, discovery, definition, new JsonObject(), resultHandler);
    }

    /**
     * Publish a GraphQL schema for querying.
     * <p>
     * On success a {@link SchemaRegistration} is returned. It contains the message consumer of the
     * {@link Queryable} service proxy that supplies the published {@link SchemaDefinition}, the published service
     * discovery record, and the {@link ServiceDiscovery} it was published to.
     * <p>
     * Note that unless invoked from a {@link SchemaPublisher} a
     * client needs to keep hold of the returned {@link Record} as long as it is published.
     *
     * @param vertx         the vert.x instance
     * @param discovery     the service discovery instance
     * @param definition    the service proxy instance exposing the graphql schema
     * @param metadata      the metadata to pass to the service
     * @param resultHandler the result handler that returns the registration
     */
    static void publish(Vertx vertx, ServiceDiscovery discovery, SchemaDefinition definition,
                        JsonObject metadata, Handler<AsyncResult<SchemaRegistration>> resultHandler) {

        Objects.requireNonNull(vertx, "Vertx cannot be null");
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        Objects.requireNonNull(definition, "GraphQL queryable cannot be null");
        Objects.requireNonNull(definition.schema(), "GraphQL schema cannot be null");
        Objects.requireNonNull(resultHandler, "Publication result handler cannot be null");

        GraphQLSchema schema = definition.schema();
        String rootQueryName = schema.getQueryType().getName();
        String address = SchemaDefinition.ADDRESS_PREFIX + "." + rootQueryName;
        JsonObject meta = metadata == null ? new JsonObject() : metadata.copy();

        // TODO Caching proxy ok?

        final MessageConsumer<JsonObject> serviceConsumer;
        if (meta.getString("publisherId") == null) {
            serviceConsumer = ProxyHelper.registerService(Queryable.class, vertx, definition, address);
        } else {
            // Publisher handles service instantiation, manages consumer.
            serviceConsumer = null;
        }

        // For later use
        meta.put(SchemaRegistration.METADATA_QUERIES, schema.getQueryType().getFieldDefinitions().stream()
                .map(GraphQLFieldDefinition::getName).collect(Collectors.toList()));
        meta.put(SchemaRegistration.METADATA_MUTATIONS, !schema.isSupportingMutations() ? Collections.emptyList() :
                schema.getMutationType().getFieldDefinitions().stream()
                        .map(GraphQLFieldDefinition::getName).collect(Collectors.toList()));

        Record record = new Record()
                .setType(SERVICE_TYPE)
                .setName(rootQueryName)
                .setMetadata(meta)
                .setLocation(new JsonObject().put(Record.ENDPOINT, address));

        discovery.publish(record, rh -> {
            if (rh.succeeded()) {
                resultHandler.handle(Future.succeededFuture(
                        SchemaRegistration.create(discovery, null, rh.result(), definition, serviceConsumer)));
            } else {
                resultHandler.handle(Future.failedFuture(rh.cause()));
            }
        });
    }

    /**
     * Unpublish a GraphQL schema that was previously published.
     *
     * @param registration the information of the schema to unpublish
     * @param resultHandler      the result handler
     */
    static void unpublish(SchemaRegistration registration,
                          Handler<AsyncResult<Void>> resultHandler) {
        Objects.requireNonNull(registration, "Schema registration cannot be null");
        Objects.requireNonNull(resultHandler, "Un-publication result handler cannot be null");

        registration.getDiscovery().unpublish(registration.getRecord().getRegistration(), rh -> {
            if (rh.succeeded()) {
                registration.unregisterServiceProxy();
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(rh.cause()));
            }
        });
    }
}
