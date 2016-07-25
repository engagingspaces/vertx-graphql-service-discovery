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

import io.engagingspaces.servicediscovery.graphql.data.DroidsSchema;
import io.engagingspaces.servicediscovery.graphql.events.SchemaReferenceData;
import io.engagingspaces.servicediscovery.graphql.query.QueryResult;
import io.engagingspaces.servicediscovery.graphql.service.GraphQLService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for the {@link SchemaConsumer}.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class SchemaConsumerTest {

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
    private TestClass testClass;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        testClass = new TestClass();
        vertx.deployVerticle(testClass, new DeploymentOptions(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        SchemaConsumer.close(testClass);
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void should_Create_Service_Discovery_And_Return_Registration() {
        DiscoveryRegistration defaultRegistration = SchemaConsumer.startDiscovery(testClass);
        DiscoveryRegistration registration = SchemaConsumer
                .startDiscovery(new ServiceDiscoveryOptions().setName("the-discovery"), testClass);
        assertNotNull(defaultRegistration);
        assertNotNull(registration);
        assertNotNull(registration.getDiscovery());
        assertNotNull(registration.getDiscoveryOptions());
        assertEquals(2, testClass.managedDiscoveries().size());
    }

    @Test
    public void should_Stop_Repository_Listening_And_Remove_Registration() {
        DiscoveryRegistration registration = SchemaConsumer
                .startDiscovery(new ServiceDiscoveryOptions().setName("the-discovery"), testClass);
        DiscoveryRegistration sameRegistration = SchemaConsumer
                .startDiscovery(new ServiceDiscoveryOptions().setName("the-discovery"), testClass);
        assertNotNull(registration);
        assertEquals(registration, sameRegistration);
        assertEquals(1, testClass.managedDiscoveries().size());
        SchemaConsumer.stopDiscovery(registration, testClass);
        assertEquals(0, testClass.managedDiscoveries().size());

        DiscoveryRegistration registration2 = SchemaConsumer
                .startDiscovery(new ServiceDiscoveryOptions().setName("the-discovery"), testClass);
        assertNotNull(registration);
        assertEquals(1, testClass.managedDiscoveries().size());
        SchemaConsumer.stopDiscovery(registration2.getDiscoveryOptions(), testClass);
        assertEquals(0, testClass.managedDiscoveries().size());
    }


    @Test
    public void should_Execute_Query_On_Consumer_Instance(TestContext context) {
        Async async = context.async();
        vertx.runOnContext(ctx -> {
            SchemaConsumer.startDiscovery(new ServiceDiscoveryOptions().setName("the-discovery"), testClass);
            testClass.executeQuery("the-discovery", "QueryType", DROIDS_QUERY, rh ->
            {
                context.assertFalse(rh.succeeded()); // Not yet published
                GraphQLService.publish(vertx, testClass.getDiscovery("the-discovery").get(), DroidsSchema.get(), rh2 ->

                        testClass.executeQuery("the-discovery", "QueryType", DROIDS_QUERY, rh3 ->
                        {
                            context.assertNotNull(rh3.result());
                            QueryResult queryResult = rh3.result();
                            context.assertTrue(queryResult.isSucceeded());
                            context.assertNotNull(queryResult.getData());
                            context.assertNotNull(queryResult.getErrors());
                            context.assertTrue(queryResult.getErrors().isEmpty());
                            JsonObject data = queryResult.getData();
                            context.assertNotNull(data.getJsonObject("hero"));
                            context.assertEquals("Droid", data.getJsonObject("hero").getString("__typename"));
                            context.assertEquals("R2-D2", data.getJsonObject("hero").getString("name"));

                            SchemaConsumer.close(testClass);
                            assertEquals(0, testClass.managedDiscoveries().size());
                            async.complete();
                        })
                );
            });
        });
        async.awaitSuccess();
    }

    public class TestClass extends AbstractVerticle implements SchemaConsumer {

        public DiscoveryRegistrar registrar;

        @Override
        public void start() {
            registrar = DiscoveryRegistrar.create(vertx);
        }

        @Override
        public void schemaDiscoveryEvent(Record record) {

        }

        @Override
        public void schemaReferenceEvent(SchemaReferenceData eventData) {

        }

        @Override
        public DiscoveryRegistrar discoveryRegistrar() {
            return registrar;
        }
    }
}
