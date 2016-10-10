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

package io.engagingspaces.graphql.servicediscovery.service.impl;

import io.engagingspaces.graphql.servicediscovery.service.GraphQLService;
import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.AbstractServiceReference;

import java.util.Objects;

/**
 * Implementation of the {@link GraphQLService} interface.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class GraphQLServiceImpl implements GraphQLService {

    /**
     * @return the service discovery type name
     */
    @Override
    public String name() {
        return Queryable.SERVICE_TYPE;
    }

    /**
     * Gets a service reference to a GraphQL service.
     * <p>
     * A {@link io.vertx.core.eventbus.DeliveryOptions} used for creating the
     * {@link Queryable} service proxy can be passed as
     * {@link JsonObject} value of key `deliveryOptions` in the configuration.
     *
     * @param vertx         the vert.x instance
     * @param discovery     the service discovery instance
     * @param record        the service record
     * @param configuration the configuration to pass to the GraphQL service
     * @return the graphql service reference
     */
    @Override
    public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
        Objects.requireNonNull(vertx, "Vertx cannot be null");
        Objects.requireNonNull(record, "Service record cannot be null");
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        return new GraphQLServiceReference(vertx, discovery, record, configuration);
    }

    /**
     * Implementation of {@link ServiceReference} for event bus graphql services.
     */
    private class GraphQLServiceReference extends AbstractServiceReference<Queryable> {

        private final JsonObject deliveryOptions;

        // TODO Create a data object for graphql service configuration
        GraphQLServiceReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
            super(vertx, discovery, record);
            this.deliveryOptions = configuration.getJsonObject("deliveryOptions", new JsonObject());
        }

        /**
         * Returns the service proxy that is associated with the published graphql schema that is referenced.
         * This method is called once, then the return is cached.
         *
         * @return the graphql service proxy for querying
         */
        @Override
        protected synchronized Queryable retrieve() {
            return Queryable.createProxy(vertx, record().getLocation().getString(Record.ENDPOINT), deliveryOptions);
        }
    }
}
