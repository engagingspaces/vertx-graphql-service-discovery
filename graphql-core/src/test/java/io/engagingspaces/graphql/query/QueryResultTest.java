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

package io.engagingspaces.graphql.query;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import io.engagingspaces.graphql.schema.SchemaDefinition;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.example.graphql.testdata.utils.MapBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import static org.junit.Assert.*;

/**
 * Tests for the query results data object.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class QueryResultTest {

    private static final ExecutionResult QUERY_RESULT_SUCCESS = new ExecutionResultImpl(MapBuilder.immutableMapOf(
            MapBuilder.<String, Object>entry("query-data", true)), null);


    private static final QueryResult EXPECTED_SUCCESS = new QueryResult(new JsonObject(
            "{\n" +
            "  \"data\": {\n" +
            "    \"query-data\": true\n" +
            "  },\n" +
            "  \"succeeded\": true," +
            "  \"errors\": [\n" +
            "  ]\n" +
            "}"));

    private static final ExecutionResult QUERY_RESULT_FAILURE = new ExecutionResultImpl(Arrays.asList(
                    new ValidationError(ValidationErrorType.UnknownType, new SourceLocation(1, 1), "Error1"),
                    new ValidationError(ValidationErrorType.WrongType,
                            Arrays.asList(new SourceLocation(2, 2), new SourceLocation(3, 3)), "Error2")));


    private static final QueryResult EXPECTED_FAILURE = new QueryResult(new JsonObject(
            "{\n" +
            "  \"data\": {},\n" +
            "  \"succeeded\": false," +
            "  \"errors\": [\n" +
            "    {\n" +
            "      \"errorType\": \"ValidationError\",\n" +
            "      \"message\": \"Validation error of type UnknownType: Error1\",\n" +
            "      \"locations\": [\n" +
            "        {\n" +
            "          \"line\": 1,\n" +
            "          \"column\": 1\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"errorType\": \"ValidationError\",\n" +
            "      \"message\": \"Validation error of type WrongType: Error2\",\n" +
            "      \"locations\": [\n" +
            "        {\n" +
            "          \"line\": 2,\n" +
            "          \"column\": 2\n" +
            "        },\n" +
            "        {\n" +
            "          \"line\": 3,\n" +
            "          \"column\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}"));

    @Test
    public void should_Create_Query_Result_From_Succeeded_Execution_Result() {
        // given
        final ExecutionResult input = QUERY_RESULT_SUCCESS;
        // when
        QueryResult result = new QueryResult(new QueryResult(
                new QueryResult(SchemaDefinition.convertToQueryResult(input)).toJson()));
        // then
        assertNotNull(result.getData());
        assertEquals(true, result.getData().getBoolean("query-data"));
        assertTrue(result.isSucceeded());
        assertNotNull(result.getErrors());
        assertEquals(0, result.getErrors().size());
        assertEquals(EXPECTED_SUCCESS, result);
        assertEquals(EXPECTED_SUCCESS.hashCode(), result.hashCode());
    }

    @Test
    public void should_Create_Query_Result_From_Failed_Execution_Result() {
        // given
        final ExecutionResult input = QUERY_RESULT_FAILURE;
        // when
        QueryResult result = new QueryResult(new QueryResult(SchemaDefinition.convertToQueryResult(input).toJson()));
        // then
        assertEquals(result.getData(), new JsonObject());
        assertEquals(result, result);
        assertNotEquals(result, "test");
        assertFalse(result.isSucceeded());
        assertNotNull(result.getErrors());
        assertEquals(2, result.getErrors().size());
        assertEquals(result.getErrors().get(0), result.getErrors().get(0));
        assertNotEquals(result.getErrors().get(0), "test");
        assertEquals("ValidationError", result.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type UnknownType: Error1", result.getErrors().get(0).getMessage());
        assertNotNull(result.getErrors().get(0).getLocations());
        assertNotNull(result.getErrors().get(1).getLocations());
        assertEquals(2, result.getErrors().get(1).getLocations().size());
        assertEquals(1, result.getErrors().get(0).getLocations().get(0).getLine());
        assertEquals(3, result.getErrors().get(1).getLocations().get(1).getColumn());
        assertEquals(EXPECTED_FAILURE, result);
        assertEquals(EXPECTED_FAILURE.hashCode(), result.hashCode());
    }

    @Test
    public void should_Create_Error_And_Error_Location_Separately() {
        QueryResult.QueryError error = new QueryResult.QueryError("type", "msg",
                Collections.singletonList(new QueryResult.ErrorLocation(new QueryResult.ErrorLocation(3, 7))));
        assertEquals(7, error.getLocations().get(0).getColumn());
        QueryResult.QueryError error2 = new QueryResult.QueryError(error);
        assertEquals(error.hashCode(), error2.hashCode());
        assertEquals(error.getLocations().get(0).hashCode(), error2.getLocations().get(0).hashCode());
        assertEquals(error2.getLocations().get(0), error2.getLocations().get(0));
        assertNotEquals(error2.getLocations().get(0), "test");
    }
}
