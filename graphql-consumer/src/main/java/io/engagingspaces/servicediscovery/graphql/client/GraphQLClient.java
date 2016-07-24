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

package io.engagingspaces.servicediscovery.graphql.client;

import io.engagingspaces.servicediscovery.graphql.query.QueryResult;
import io.engagingspaces.servicediscovery.graphql.query.Queryable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Status;

import java.util.Objects;

import static io.engagingspaces.servicediscovery.graphql.query.Queryable.SERVICE_TYPE;

/**
 * Client for consuming GraphQL services.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@VertxGen
public interface GraphQLClient {

    /**
     * Lookup a GraphQL service record using the provided filter and if found, retrieve the service proxy of type
     * {@link Queryable} used for querying.
     *
     * @param discovery     the service discovery instance
     * @param filter        the filter to select the schema
     * @param resultHandler the result handler
     */
    static void getSchemaProxy(ServiceDiscovery discovery, JsonObject filter,
                               Handler<AsyncResult<Queryable>> resultHandler) {
        Objects.requireNonNull(discovery, "Service discovery cannot be null");

        discovery.getRecord(filter, rh -> {
            if (rh.failed()) {
                resultHandler.handle(Future.failedFuture(rh.cause()));
            } else {
                if (rh.result() == null) {
                    resultHandler.handle(Future.failedFuture("Failed to find schema proxy using filter " + filter));
                } else {
                    Record record = rh.result();
                    if (!SERVICE_TYPE.equals(record.getType())) {
                        resultHandler.handle(Future.failedFuture("Record '" + record.getName() +
                                "' is of wrong type '" + record.getType() + "'. Expected: " + SERVICE_TYPE));
                    } else {
                        getSchemaProxy(discovery, rh.result(), resultHandler);
                    }
                }
            }
        });
    }

    /**
     * Get the GraphQL service proxy that is associated with the provided service record.
     *
     * @param discovery     the service discovery instance
     * @param record        the service record of a published GraphQL service
     * @param resultHandler the result handler
     */
    static void getSchemaProxy(ServiceDiscovery discovery, Record record,
                               Handler<AsyncResult<Queryable>> resultHandler) {
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        Objects.requireNonNull(record, "Record cannot be null");

        if (!SERVICE_TYPE.equals(record.getType())) {
            resultHandler.handle(Future.failedFuture("Record '" + record.getName() +
                    "' is of wrong type '" + record.getType() + "'. Expected: " + SERVICE_TYPE));
        } else if (!Status.UP.equals(record.getStatus())) {
            resultHandler.handle(Future.failedFuture("Record status indicates service '" + record.getName() +
                    "' is: " + record.getStatus() + ". Expected: " + Status.UP));
        } else if (record.getRegistration() == null) {
            resultHandler.handle(Future.failedFuture("Record '" + record.getName() +
                    "' has no service discovery registration"));
        } else {
            ServiceReference reference = discovery.getReference(record);
            Queryable queryable = reference.cached() == null ? reference.get() : reference.cached();
            reference.release();
            resultHandler.handle(Future.succeededFuture(queryable));
        }
    }

    /**
     * Executes the GraphQL query on the GraphQL service that is associated with the provided service record.
     * <p>
     * On success a {@link QueryResult} is returned. While this indicates the query executor has finished processing,
     * the query itself might still have failed, so be sure to check {@link QueryResult#isSucceeded()} and
     * {@link QueryResult#getErrors()} properties on the query result afterwards.
     * afterwards.
     *
     * @param discovery     the service discovery instance
     * @param record        the service record of a published GraphQL service
     * @param query         the GraphQL query
     * @param resultHandler the result handler
     */
    static void executeQuery(ServiceDiscovery discovery, Record record, String query,
                             Handler<AsyncResult<QueryResult>> resultHandler) {
        executeQuery(discovery, record, query, null, resultHandler);
    }

    /**
     * Executes the parametrized GraphQL query on the GraphQL service associated with the provided service record.
     * <p>
     * On success a {@link QueryResult} is returned. Note that at
     * this point the GraphQL query may still have failed, so be sure to check the
     * {@link QueryResult#getErrors()} property afterwards.
     * <p>
     * The top-level keys in the `variables` parameter represent the variable names that are used in the query, and
     * the json values are passed as {@link Object} to the query executor.
     *
     * @param discovery     the service discovery instance
     * @param record        the service record of a published GraphQL service
     * @param query         the GraphQL query
     * @param variables     the variables to pass to the query executor
     * @param resultHandler the result handler
     */
    static void executeQuery(ServiceDiscovery discovery, Record record, String query,
                             JsonObject variables, Handler<AsyncResult<QueryResult>> resultHandler) {
        Objects.requireNonNull(discovery, "Service discovery cannot be null");
        Objects.requireNonNull(record, "Record cannot be null");
        Objects.requireNonNull(query, "GraphQL query cannot be null");

        getSchemaProxy(discovery, record, rh -> {
           if (rh.succeeded()) {
               rh.result().queryWithVariables(query, variables, resultHandler);
           } else {
               resultHandler.handle(Future.failedFuture(rh.cause()));
           }
        });
    }
}
