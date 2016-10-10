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

package io.engagingspaces.servicediscovery.graphql.consumer;

import io.engagingspaces.graphql.discovery.impl.AbstractRegistrar;
import io.engagingspaces.graphql.discovery.impl.AbstractRegistration;
import io.engagingspaces.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.graphql.events.SchemaUsageHandler;
import io.engagingspaces.graphql.events.impl.SchemaMessageConsumers;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

/**
 * Manages {@link ServiceDiscovery} creation, and registration of discovery events.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class DiscoveryRegistrar extends AbstractRegistrar<DiscoveryRegistration> {

    private final SchemaMessageConsumers eventManager;

    protected DiscoveryRegistrar(Vertx vertx) {
        super(vertx);
        this.eventManager = new SchemaMessageConsumers(vertx);
    }

    /**
     * Creates a new discovery registrar instance for managing service discoveries and their event consumers.
     *
     * @param vertx the vert.x instance
     * @return the discovery registrar
     */
    public static DiscoveryRegistrar create(Vertx vertx) {
        return new DiscoveryRegistrar(vertx);
    }

    /**
     * Registers the provided event handlers to the `announce` and `usage` events of the service discovery
     * specified in the service discovery options.
     *
     * @param options         the service discovery options
     * @param announceHandler the handler for `announce` events
     * @param usageHandler    the handler for `usage` events
     * @return the discovery registration
     */
    protected DiscoveryRegistration startListening(
            ServiceDiscoveryOptions options, SchemaAnnounceHandler announceHandler, SchemaUsageHandler usageHandler) {

        ServiceDiscovery discovery = getOrCreateDiscovery(options, () -> {
            stopListening(options);
            return null;
        });
        eventManager.registerConsumer(options.getAnnounceAddress(), announceHandler);
        eventManager.registerConsumer(options.getUsageAddress(), usageHandler);
        return register(options.getName(), DiscoveryRegistration.create(discovery, options));
    }

    /**
     * Stops listening to service discovery events if they are no longer in use, then closes the service discovery.
     *
     * @param options the service discovery options
     */
    protected void stopListening(ServiceDiscoveryOptions options) {
        eventManager.unregisterConsumer(options.getAnnounceAddress());
        eventManager.unregisterConsumer(options.getUsageAddress());
        closeServiceDiscovery(options.getName());
    }

    /**
     * Closes the registrar and release all its resources.
     */
    @Override
    protected void close() {
        registrations().stream()
                .map(AbstractRegistration::getDiscoveryOptions)
                .forEach(this::stopListening);
        eventManager.close();
        super.close();
    }
}
