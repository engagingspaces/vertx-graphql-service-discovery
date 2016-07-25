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

package io.engagingspaces.servicediscovery.graphql.publisher;

import io.engagingspaces.servicediscovery.graphql.discovery.impl.AbstractRegistrar;
import io.engagingspaces.servicediscovery.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.servicediscovery.graphql.events.SchemaPublishedHandler;
import io.engagingspaces.servicediscovery.graphql.events.SchemaUnpublishedHandler;
import io.engagingspaces.servicediscovery.graphql.events.impl.SchemaMessageConsumers;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Manages {@link ServiceDiscovery} creation, schema registration and schema events.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class SchemaRegistrar extends AbstractRegistrar<SchemaRegistration> {

    private final String publisherId;
    private final SchemaMessageConsumers consumerManager;

    protected SchemaRegistrar(Vertx vertx, String publisherId) {
        super(vertx);
        this.publisherId = publisherId == null ? UUID.randomUUID().toString() :  publisherId;
        this.consumerManager = new SchemaMessageConsumers(vertx);
    }

    /**
     * Creates a new schema registrar instance for managing service discoveries, tracking published schema's and
     * trigger schema publication events for the {@link SchemaPublisher} it is associated with.
     *
     * @param vertx the vert.x instance
     * @return the schema registrar
     */
    public static SchemaRegistrar create(Vertx vertx) {
        return new SchemaRegistrar(vertx, null);
    }

    /**
     * Creates a new schema registrar instance for managing service discoveries, tracking published schema's and
     * trigger schema publication events for the {@link SchemaPublisher} it is associated with.
     * <p>
     * The {@code publisherId} is used in schema registrations to determine target publisher for event handling.
     *
     * @param vertx       the vert.x instance
     * @param publisherId the name of the publisher
     * @return the schema registrar
     */
    public static SchemaRegistrar create(Vertx vertx, String publisherId) {
        return new SchemaRegistrar(vertx, publisherId);
    }

    /**
     * @return the name of the publisher associated with this registrar.
     */
    protected String getPublisherId() {
        return publisherId;
    }

    /**
     * @param options the service discovery options
     * @return the existing or created service discovery instance
     */
    protected ServiceDiscovery getOrCreateDiscovery(ServiceDiscoveryOptions options) {
        return super.getOrCreateDiscovery(options, () -> null);
    }

    /**
     * @return the graphql schema's that are published by the associated publisher.
     */
    @Override
    protected List<SchemaRegistration> registrations() {
        return super.registrations();
    }

    /**
     * Registers a schema definition created by the
     * {@link io.engagingspaces.servicediscovery.graphql.service.GraphQLService}.
     * <p>
     * The provided registration is cloned, completed with publisher-related information, registered and then returned.
     *
     * @param partialRegistration the partially completed schema registration
     * @param options             the service discovery options to add
     * @param publishedHandler    the event handler to invoke on schema published events
     * @param unpublishedHandler  the event handler to invoke on schema unpublished events
     * @return the completed schema registration
     */
    protected SchemaRegistration register(
            SchemaRegistration partialRegistration, ServiceDiscoveryOptions options,
            SchemaPublishedHandler<SchemaRegistration> publishedHandler,
            SchemaUnpublishedHandler<SchemaRegistration> unpublishedHandler) {

        // First start listening to schema events.
        registerSchemaEventConsumers(options, publishedHandler, unpublishedHandler);

        // Then register service consumer created from schema definition, if it was not registered yet.
        MessageConsumer<JsonObject> serviceConsumer = registerSchemaServiceConsumer(
                partialRegistration.getRecord(), partialRegistration.getSchemaDefinition());

        // Complete the schema registration
        SchemaRegistration fullRegistration = SchemaRegistration.create(partialRegistration.getDiscovery(), options,
                partialRegistration.getRecord(), partialRegistration.getSchemaDefinition(), serviceConsumer);

        return super.register(options.getName(), fullRegistration);
    }

    /**
     * Unregisters the published schema indicated by the provided registration.
     *
     * @param registration the schema registration
     */
    @Override
    protected void unregister(SchemaRegistration registration) {
        consumerManager.unregisterConsumer(registration.getRecord().getLocation().getString(Record.ENDPOINT));
        super.unregister(registration);
    }

    /**
     * Finds the schema registration that is managed by this registrar and is published to the specified discovery.
     *
     * @param discoveryName the service discovery name to search
     * @param schemaName    the name of the published schema (graphql service name)
     * @return optional that holds the schema registration, or empty if not found
     */
    protected Optional<SchemaRegistration> findRegistration(String discoveryName, String schemaName) {
        return registrations().stream()
                .filter(registration -> discoveryName.equals(registration.getDiscoveryOptions().getName()))
                .filter(registration -> registration.getSchemaName().equals(schemaName))
                .findFirst();
    }

    /**
     * Closes the registrar and releases all its resources.
     *
     * @param closeAction  the action to perform for closing registered schema's
     * @param closeHandler the close handler
     */
    protected void close(BiConsumer<SchemaRegistration, Handler<AsyncResult<Void>>> closeAction,
               Handler<AsyncResult<Void>> closeHandler) {

        if (!registrations().isEmpty()) {
            // Execute the close action against each of the published schema's (e.g. un-publishing)
            List<Future> futures = new ArrayList<>();
            registrations().forEach(registration -> closeAction.accept(registration,
                    rh -> futures.add(rh.succeeded() ? Future.succeededFuture() : Future.failedFuture(rh.cause()))));

            handleCloseCompletion(closeHandler, futures);
        } else {
            doClose(closeHandler);
        }
    }

    private void registerSchemaEventConsumers(
            ServiceDiscoveryOptions options, SchemaPublishedHandler<SchemaRegistration> publishedHandler,
            SchemaUnpublishedHandler<SchemaRegistration> unpublishedHandler) {

        SchemaAnnounceHandler announceHandler = record ->
                findRegistration(options.getName(), record.getName()).ifPresent(reg -> {
                    if (Status.UP.equals(record.getStatus())) {
                        publishedHandler.schemaPublished(reg);
                    } else {
                        unpublishedHandler.schemaUnpublished(reg);
                    }
                });
        consumerManager.registerConsumer(options.getAnnounceAddress(), announceHandler);
    }

    private MessageConsumer<JsonObject> registerSchemaServiceConsumer(Record record, SchemaDefinition definition) {
        String address = record.getLocation().getString(Record.ENDPOINT);
        return consumerManager.registerServiceConsumer(address, definition);
    }

    private void handleCloseCompletion(Handler<AsyncResult<Void>> closeHandler, List<Future> futures) {
        CompositeFuture.all(futures).setHandler(rh -> {
            CompositeFuture composite = rh.result();
            for (int index = 0; index < composite.size(); index++) {
                if (composite.succeeded(index) && composite.result(index) != null) {
                    composite.<SchemaRegistration>result(index).unregisterServiceProxy();
                }
            }
            if (rh.succeeded()) {
                doClose(closeHandler);
            } else
                closeHandler.handle(Future.failedFuture(rh.cause()));
        });
    }

    private void doClose(Handler<AsyncResult<Void>> closeHandler) {
        super.close();
        consumerManager.close();
        closeHandler.handle(Future.succeededFuture());
    }
}
