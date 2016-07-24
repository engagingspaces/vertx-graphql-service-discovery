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

package io.engagingspaces.servicediscovery.graphql.query;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Service proxy interface that provides access to the schema definitions that are exposed by a GraphQL publisher.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@ProxyGen
public interface Queryable {

    /**
     * Name of the discovery service type for GraphQL schema's.
     */
    String SERVICE_TYPE = "graphql-service";

    /**
     * The prefix that is combined with the root query name of the associated GraphQL schema
     * to form the {@link io.vertx.servicediscovery.Record#ENDPOINT} address used in service discovery.
     */
    String ADDRESS_PREFIX = "service.graphql";

    /**
     * Creates a service proxy to the {@link Queryable} implementation
     * at the specified address.
     * <p>
     * The {@link DeliveryOptions} to use on the returned message consumer must be passed as
     * plain json, because it does not provide a toJson() method (see:vhttps://github.com/eclipse/vert.x/issues/1502).
     *
     * @param vertx           the vert.x instance
     * @param address         the address of the service proxy
     * @param deliveryOptions the delivery options to use on the message consumer
     * @return the graphql service proxy
     */
    static Queryable createProxy(Vertx vertx, String address, JsonObject deliveryOptions) {
        return ProxyHelper.createProxy(Queryable.class, vertx, address, new DeliveryOptions(deliveryOptions));
    }

    /**
     * Executes the GraphQL query on the GraphQL schema proxy.
     * <p>
     * On success a {@link QueryResult} is returned. While this indicates the query executor has finished processing,
     * the query itself might still have failed, so be sure to check {@link QueryResult#isSucceeded()} and
     * {@link QueryResult#getErrors()} properties on the query result afterwards.
     * afterwards.
     *
     * @param graphqlQuery  the graphql query
     * @param resultHandler the result handler with the query result on success, or a failure
     */
    void query(String graphqlQuery, Handler<AsyncResult<QueryResult>> resultHandler);

    /**
     * Executes the GraphQL query on the GraphQL schema proxy using the provided variables.
     *
     * @param graphqlQuery  the graphql query
     * @param variables     the query variables
     * @param resultHandler the result handler with the graphql query result on success, or a failure
     */
    void queryWithVariables(String graphqlQuery, JsonObject variables,
                            Handler<AsyncResult<QueryResult>> resultHandler);

    /**
     * Invoked when the queryable service proxy closes. Does nothing by default, but can be overridden in sub-classes.
     */
    @ProxyClose
    default void close() {
        // NO OP
    }
}
