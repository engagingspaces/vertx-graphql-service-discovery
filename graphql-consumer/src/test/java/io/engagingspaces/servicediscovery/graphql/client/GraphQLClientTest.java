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

import io.engagingspaces.servicediscovery.graphql.data.DroidsSchema;
import io.engagingspaces.servicediscovery.graphql.publisher.SchemaRegistration;
import io.engagingspaces.servicediscovery.graphql.query.QueryResult;
import io.engagingspaces.servicediscovery.graphql.query.Queryable;
import io.engagingspaces.servicediscovery.graphql.service.GraphQLService;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Tests for the {@link GraphQLClient}.
 *
 * @author Arnold Schrijver
 */
@RunWith(VertxUnitRunner.class)
public class GraphQLClientTest {

    private static final String DROIDS_QUERY =
            "        query CheckTypeOfR2 {\n" +
            "            hero {\n" +
            "                __typename\n" +
            "                name\n" +
            "            }\n" +
            "        }";

    private static final String DROIDS_VARIABLES_QUERY =
            "        query GetDroidNameR2(\\$id: String!) {\n" +
            "            droid(id: \\$id) {\n" +
            "                name\n" +
            "            }\n" +
            "        }";

    private Vertx vertx;
    private Record record;
    private ServiceDiscovery discovery;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());

        ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx,
                new ServiceDiscoveryOptions().setName("my-schema-discovery"));
        Handler<SchemaRegistration> schemaHandler = schema -> {
            record = schema.getRecord();
            discovery = schema.getDiscovery();
            assertEquals(serviceDiscovery, discovery);
        };
        GraphQLService.publish(vertx, serviceDiscovery, DroidsSchema.get(), context.asyncAssertSuccess(schemaHandler));
    }

    @After
    public void tearDown() {
        discovery.close();
        vertx.close();
    }

    @Test
    public void should_Execute_Query_Through_Standard_Service_Discovery(TestContext context) {
        Async async = context.async();
        Queryable queryable = discovery.getReference(record).get();
        queryable.query(
                DROIDS_QUERY,
                rh -> {
                    context.assertTrue(rh.succeeded());
                    context.assertNotNull(rh.result());
                    QueryResult queryResult = rh.result();
                    context.assertTrue(queryResult.isSucceeded());
                    context.assertNotNull(queryResult.getData());
                    context.assertNotNull(queryResult.getErrors());
                    context.assertTrue(queryResult.getErrors().isEmpty());
                    JsonObject data = queryResult.getData();
                    context.assertNotNull(data.getJsonObject("hero"));
                    context.assertEquals("Droid", data.getJsonObject("hero").getString("__typename"));
                    context.assertEquals("R2-D2", data.getJsonObject("hero").getString("name"));
                    async.complete();
                });
    }

    @Test
    public void should_Execute_Query_Directly_From_Valid_Record(TestContext context) {
        GraphQLClient.executeQuery(discovery, record, DROIDS_QUERY, context.asyncAssertSuccess(queryResult -> {
            assertNotNull(queryResult);
            assertTrue(queryResult.isSucceeded());
            assertNotNull(queryResult.getData().getJsonObject("hero"));
        }));
    }

    @Test
    public void should_Execute_Query_With_Variables_Directly_From_Valid_Record(TestContext context) {
        JsonObject variables = new JsonObject().put("id", "2001");
        GraphQLClient.executeQuery(discovery, record, DROIDS_VARIABLES_QUERY, variables,
                context.asyncAssertSuccess(queryResult -> {
            assertNotNull(queryResult);
            assertTrue(queryResult.isSucceeded());
            assertNotNull(queryResult.getData().getJsonObject("droid"));
        }));
    }

    @Test
    public void should_Execute_Invalid_Query_And_Return_Query_Result_Errors(TestContext context) {
        GraphQLClient.executeQuery(discovery, record, DROIDS_QUERY.substring(10),
                context.asyncAssertSuccess(queryResult -> {
            assertNotNull(queryResult);
            assertFalse(queryResult.isSucceeded());
            assertEquals(queryResult.getData(), new JsonObject());
            assertFalse(queryResult.getErrors().isEmpty());
            assertEquals("InvalidSyntax", queryResult.getErrors().get(0).getErrorType());
            assertEquals(1, queryResult.getErrors().get(0).getLocations().get(0).getLine());
        }));
    }

    @Test
    public void should_Fail_Execute_Query_With_Required_Variable_Missing(TestContext context) {
        JsonObject variables = new JsonObject().put("some", "other variable");
        GraphQLClient.executeQuery(discovery, record, DROIDS_VARIABLES_QUERY, variables,
                context.asyncAssertFailure(ex -> {
                    assertNotNull(ex.getMessage());
                    assertTrue(ex.getMessage().startsWith("Null value for NonNull type GraphQLNonNull"));
                }));
    }

    @Test
    public void should_Find_Service_Proxy_From_Valid_Filter(TestContext context) {
        Async async = context.async();
        GraphQLClient.getSchemaProxy(discovery, new JsonObject().put("name", "QueryType"), rh -> {
            if (rh.failed()) {
                context.fail(rh.cause());
            }
            context.assertNotNull(rh.result());
            Queryable queryable = rh.result();
            queryable.query(
                    DROIDS_QUERY,
                    result -> {
                        context.assertTrue(result.succeeded());
                        context.assertNotNull(result.result());
                    });
            async.complete();
        });
    }

    @Test
    public void should_Find_Service_Proxy_From_Valid_Record(TestContext context) {
        Async async = context.async();
        GraphQLClient.getSchemaProxy(discovery, record, rh -> {
            if (rh.failed()) {
                context.fail(rh.cause());
            }
            context.assertNotNull(rh.result());
            Queryable queryable = rh.result();
            queryable.query(
                    DROIDS_QUERY,
                    result -> {
                        context.assertTrue(result.succeeded());
                        context.assertNotNull(result.result());
                    });
            async.complete();
        });
    }

    @Test
    public void should_Fail_Service_Proxy_Filtering_On_Unknown_Record(TestContext context) {
        GraphQLClient.getSchemaProxy(discovery, new JsonObject().put("name", "FooBar"),
                context.asyncAssertFailure());
    }

    @Test
    public void should_Fail_Service_Proxy_With_Record_Of_Wrong_Type(TestContext context) {
        GraphQLClient.getSchemaProxy(discovery, new Record().setName("QueryType"),
                context.asyncAssertFailure(ex ->
                    assertEquals("Record 'QueryType' is of wrong type 'null'. Expected: graphql-service",
                            ex.getMessage())
                ));
    }

    @Test
    public void should_Fail_Service_Proxy_With_Record_Status_Not_UP(TestContext context) {
        GraphQLClient.getSchemaProxy(discovery, new Record()
                .setName("QueryType").setType(Queryable.SERVICE_TYPE),
                        context.asyncAssertFailure(ex ->
                            assertEquals("Record status indicates service 'QueryType' is: UNKNOWN. Expected: UP",
                                    ex.getMessage())
                        ));
    }

    @Test
    public void should_Fail_Service_Proxy_With_Record_Not_Registered(TestContext context) {
        GraphQLClient.getSchemaProxy(discovery, new Record()
                .setName("QueryType").setType(Queryable.SERVICE_TYPE).setStatus(Status.UP),
                        context.asyncAssertFailure(ex ->
                            assertEquals("Record 'QueryType' has no service discovery registration", ex.getMessage())
                        ));
    }
}
