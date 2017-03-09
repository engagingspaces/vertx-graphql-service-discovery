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

package io.engagingspaces.graphql.schema;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import io.engagingspaces.graphql.query.QueryResult;
import io.engagingspaces.graphql.query.QueryResult.ErrorLocation;
import io.engagingspaces.graphql.query.QueryResult.QueryError;
import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class SchemaDefinition implements Queryable {

    private final GraphQLSchema schema;
    private final String schemaName;
    private final SchemaMetadata schemaMetadata;
    private final String serviceAddress;

    protected SchemaDefinition(GraphQLSchema schema, SchemaMetadata metadata) {
        this.schema = schema;
        this.schemaMetadata = metadata == null ? SchemaMetadata.create() : metadata;
        this.schemaName = schemaMetadata.getSchemaName() == null || schemaMetadata.getSchemaName().isEmpty() ?
                schema.getQueryType().getName() : schemaMetadata.getSchemaName();
        this.serviceAddress = schemaMetadata.getServiceAddress() == null ||
                schemaMetadata.getServiceAddress().isEmpty() ?
                        Queryable.ADDRESS_PREFIX + "." + schemaName() : schemaMetadata.getServiceAddress();

        schemaMetadata.put(SchemaMetadata.METADATA_QUERIES, schema.getQueryType().getFieldDefinitions().stream()
                .map(GraphQLFieldDefinition::getName).collect(Collectors.toList()));
        schemaMetadata.put(SchemaMetadata.METADATA_MUTATIONS,
                !schema.isSupportingMutations() ? Collections.emptyList() :
                        schema.getMutationType().getFieldDefinitions().stream()
                                .map(GraphQLFieldDefinition::getName).collect(Collectors.toList()));
    }

    public static SchemaDefinition createInstance(GraphQLSchema schema, SchemaMetadata metadata) {
        return new SchemaDefinition(schema, metadata);
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
    @Override
    public void query(String graphqlQuery, Handler<AsyncResult<QueryResult>> resultHandler) {
        queryWithVariables(graphqlQuery, null, resultHandler);
    }

    /**
     * Executes the GraphQL query on the GraphQL schema proxy using the provided variables.
     *
     * @param graphqlQuery  the graphql query
     * @param resultHandler the result handler with the graphql query result on success, or a failure
     */
    @Override
    public void queryWithVariables(String graphqlQuery, JsonObject variables,
                                    Handler<AsyncResult<QueryResult>> resultHandler) {
        try {
            QueryResult result = queryBlocking(graphqlQuery, variables);
            resultHandler.handle(Future.succeededFuture(result));
        } catch (RuntimeException ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    /**
     * Gets the GraphQL schema that is associated with this service proxy.
     * <p>
     * A valid schema instance must be available on {@link Queryable} service proxy implementations that will
     * be published. Accessing this method from a service proxy results in an {@link UnsupportedOperationException}.
     *
     * @return the graphql schema to be published and queried
     * @throws UnsupportedOperationException if invoked from a service proxy
     */
    public GraphQLSchema schema() {
        return schema;
    }

    /**
     * Gets the name of the schema definition.
     *
     * @return the schema name
     */
    public String schemaName() {
        return schemaName;
    }

    /**
     * Gets the address where the schema will be published.
     *
     * @return the service address
     */
    public String serviceAddress() {
        return serviceAddress;
    }

    /**
     * Gets the metadata associated with the schema.
     *
     * @return the schema metadata
     */
    public SchemaMetadata metadata() {
        return schemaMetadata;
    }

    /**
     * Executes a blocking call to the GraphQL query processor and executes the query.
     *
     * @param graphqlQuery the graphql query
     * @param variables    the variables to pass to the query
     * @return the graphql query result
     */
    public QueryResult queryBlocking(String graphqlQuery, JsonObject variables) {
        Objects.requireNonNull(graphqlQuery, "GraphQL query cannot be null");
        GraphQL graphQL = new GraphQL(schema());
        ExecutionResult result;
        if (variables == null) {
            result = graphQL.execute(graphqlQuery);
        } else {
            result = graphQL.execute(graphqlQuery, (Object) null, variables.getMap());
        }
        return convertToQueryResult(result);
    }

    /**
     * Creates a new {@link QueryResult} data object from the
     * provided GraphQL {@link ExecutionResult}.
     *
     * @param executionResult the execution result of the GraphQL query
     * @return the query result data object
     */
    @SuppressWarnings("unchecked")
    public static QueryResult convertToQueryResult(ExecutionResult executionResult) {
        Objects.requireNonNull(executionResult, "Query execution result cannot be null");
        boolean succeeded = executionResult.getErrors() == null || executionResult.getErrors().isEmpty();

        return new QueryResult(
                succeeded ? new JsonObject((Map<String, Object>) executionResult.getData()) : new JsonObject(),
                succeeded, executionResult.getErrors().stream()
                        .map(SchemaDefinition::convertToQueryError).collect(Collectors.toList()));
    }

    /**
     * Creates a new {@link QueryError} data object
     * based on the provided {@link GraphQLError}.
     *
     * @param graphQLError the graphql error to convert
     * @return the converted query error data object
     */
    public static QueryError convertToQueryError(GraphQLError graphQLError) {
        return new QueryError(graphQLError.getErrorType().name(), graphQLError.getMessage(),
                graphQLError.getLocations().stream().map(location ->
                        new ErrorLocation(location.getLine(), location.getColumn())).collect(Collectors.toList()));
    }
}
