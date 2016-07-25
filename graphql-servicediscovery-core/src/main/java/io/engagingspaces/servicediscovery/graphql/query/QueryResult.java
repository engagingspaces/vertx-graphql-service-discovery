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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable data object that holds the execution result of a GraphQL query.
 * <p>
 * If the query was successful the response is available as {@link JsonObject}. Otherwise a list of
 * {@link QueryError} provides more detail on the failure.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@DataObject
public class QueryResult {

    private final JsonObject data;
    private final boolean succeeded;
    private final List<QueryError> errors;

    private volatile int hashCode;

    public QueryResult(JsonObject data, boolean succeeded, List<QueryError> errors) {
        this.data = data;
        this.succeeded = succeeded;
        this.errors = errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    /**
     * Creates a new {@link QueryResult} from
     * its json representation.
     *
     * @param json the json object
     */
    public QueryResult(JsonObject json) {
        this.data = json.getJsonObject("data", new JsonObject());
        this.succeeded = json.getBoolean("succeeded", false);
        List<QueryError> queryErrors = json.getJsonArray("errors", new JsonArray()).stream()
                .map(error -> new QueryError((JsonObject) error)).collect(Collectors.toList());
        this.errors = queryErrors == null ? Collections.emptyList() : Collections.unmodifiableList(queryErrors);
    }

    /**
     * Creates a new {@link QueryResult} by copying
     * the values from another {@link QueryResult}.
     *
     * @param other the query result to copy
     */
    public QueryResult(QueryResult other) {
        this.data = other.data;
        this.succeeded = other.succeeded;
        this.errors = other.errors;
    }

    /**
     * @return the JSON representation of the current
     * {@link QueryResult}.
     */
    public JsonObject toJson() {
        return new JsonObject()
                .put("data", data)
                .put("succeeded", succeeded)
                .put("errors", new JsonArray(errors.stream().map(QueryError::toJson).collect(Collectors.toList())));
    }

    /**
     * Gets the {@link JsonObject} response of a successful GraphQL query. If the query wasn't
     * successful an empty json object is returned.
     *
     * @return the query response
     */
    public JsonObject getData() {
        return data;
    }

    /**
     * @return {@code true} when the query was successful, {@code false} otherwise
     */
    public boolean isSucceeded() {
        return succeeded;
    }

    /**
     * Gets the errors that occurred on query execution, if the GraphQL query was not successful.
     *
     * @return the query errors that occurred, or an empty list
     */
    public List<QueryError> getErrors() {
        return errors;
    }

    /**
     * Determine object equality of this query result with another object.
     * <p>
     * Use this method judiciously, as it uses deep comparison, which can become costly when
     * comparing to a successful query result holding a large json payload.
     *
     * @param other the object to compare
     * @return {@code true} when equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof QueryResult)) {
            return false;
        }
        QueryResult test = (QueryResult) other;
        return succeeded == test.succeeded && fieldEquals(errors, test.errors) && fieldEquals(data, test.data);
    }

    private static boolean fieldEquals(Object value1, Object value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    /**
     * The hash code of the query result.
     * <p>
     * Result is lazy-loaded on the first call, then cached.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + (data == null ? 0 : data.hashCode());
            result = 31 * result + (succeeded ? 1 : 0);
            result = 31 * result + (errors == null ? 0 : errors.hashCode());
            hashCode = result;
        }
        return result;
    }

    /**
     * Data object that represents an error that occurred upon execution of a GraphQL query.
     */
    @DataObject
    public static class QueryError {

        private final String errorType;
        private final String message;
        private final List<ErrorLocation> locations;

        public QueryError(String errorType, String message, List<ErrorLocation> locations) {
            this.errorType = errorType;
            this.message = message;
            this.locations = locations == null ? Collections.emptyList() : Collections.unmodifiableList(locations);
        }

        /**
         * Creates a new {@link QueryError} from
         * its json representation.
         *
         * @param json the json object
         */
        public QueryError(JsonObject json) {
            this.errorType = json.getString("errorType");
            this.message = json.getString("message");
            List<ErrorLocation> errorLocations = json.getJsonArray("locations", new JsonArray()).stream()
                    .map(location -> new ErrorLocation((JsonObject) location)).collect(Collectors.toList());
            this.locations = errorLocations == null ?
                    Collections.emptyList() : Collections.unmodifiableList(errorLocations);
        }

        /**
         * Creates a new {@link QueryError} by copying
         * the values from another {@link QueryError}.
         *
         * @param other the query error to copy
         */
        public QueryError(QueryError other) {
            this.errorType = other.errorType;
            this.message = other.message;
            this.locations = other.locations;
        }

        /**
         * @return the JSON representation of the current
         * {@link QueryError}.
         */
        public JsonObject toJson() {
            return new JsonObject()
                    .put("errorType", errorType)
                    .put("message", message)
                    .put("locations", new JsonArray(locations.stream()
                            .map(ErrorLocation::toJson).collect(Collectors.toList())));
        }

        /**
         * Gets the type of the graphql execution error.
         * <p>
         * The possible types are determined by the `ValidationErrorType` enum defined in the GraphQL implementation
         * (refer to: https://github.com/graphql-java/graphql-java).
         *
         * @return the error type
         */
        public String getErrorType() {
            return errorType;
        }

        /**
         * @return the error message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the location(s) where the error occurred (if applicable)
         */
        public List<ErrorLocation> getLocations() {
            return locations;
        }

        /**
         * @param other the object to compare to this query error
         * @return {@code true} when equal, {@code false} otherwise
         */
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof QueryError)) {
                return false;
            }
            QueryError test = (QueryError) other;
            return fieldEquals(errorType, test.errorType) && fieldEquals(message, test.message) &&
                    fieldEquals(locations, test.locations);
        }

        private boolean fieldEquals(Object value1, Object value2) {
            return value1 == null ? value2 == null : value1.equals(value2);
        }

        /**
         * @return the hash code of the query error
         */
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + (errorType == null ? 0 : errorType.hashCode());
            result = 31 * result + (message == null ? 0 : message.hashCode());
            result = 31 * result + (locations == null ? 0: locations.hashCode());
            return result;
        }
    }

    /**
     * Data object that holds the location of a graphql query error.
     */
    @DataObject
    public static class ErrorLocation {

        private final int line;
        private final int column;

        public ErrorLocation(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public ErrorLocation(JsonObject json) {
            this.line = json.getInteger("line", 0);
            this.column = json.getInteger("column", 0);
        }

        public ErrorLocation(ErrorLocation other) {
            this.line = other.line;
            this.column = other.column;
        }

        public JsonObject toJson() {
            return new JsonObject().put("line", line).put("column", column);
        }

        /**
         * @return the line where the error occurred
         */
        public int getLine() {
            return line;
        }

        /**
         * @return the column where the error occurred
         */
        public int getColumn() {
            return column;
        }

        /**
         * @param other the object to compare to this error location
         * @return {@code true} when equal, {@code false} otherwise
         */
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof ErrorLocation)) {
                return false;
            }
            ErrorLocation test = (ErrorLocation) other;
            return this.line == test.line && this.column == test.column;
        }

        /**
         * @return the hash code of the error location
         */
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + line;
            result = 31 * result + column;
            return result;
        }
    }
}
