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

import io.engagingspaces.servicediscovery.graphql.data.DroidsSchema;
import io.engagingspaces.servicediscovery.graphql.data.StarWarsSchema;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for schema registrar class.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class SchemaRegistrarTest {

    private SchemaRegistrar schemaRegistrar;

    private Vertx vertx;
    private ServiceDiscoveryOptions options;
    private TestClass schemaPublisher;


    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp() {
        vertx = rule.vertx();
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("announceAddress").setUsageAddress("usageAddress");
        schemaRegistrar = SchemaRegistrar.create(vertx);
        schemaPublisher = new TestClass(schemaRegistrar);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close();
        schemaRegistrar.close(null, context.asyncAssertSuccess());
    }

    @Test
    public void should_Manage_Schema_Registration_And_Close_Properly(TestContext context) {
        Async async = context.async();
        context.assertNotNull(schemaRegistrar.getPublisherId());
        schemaRegistrar = SchemaRegistrar.create(vertx, "thePublisherId");
        context.assertEquals("thePublisherId", schemaRegistrar.getPublisherId());

        ServiceDiscovery discovery = schemaRegistrar.getOrCreateDiscovery(options);
        context.assertNotNull(discovery);
        context.assertNotNull(schemaRegistrar.registrations());
        context.assertEquals(0, schemaRegistrar.registrations().size());

        // Fans of nested handlers, take note! ;)
        // Publish 1
        schemaPublisher.publish(options, DroidsSchema.get(), rh -> {
            context.assertTrue(rh.succeeded());
            context.assertEquals(1, schemaPublisher.registeredSchemas().size());

            // Publish 2
            schemaPublisher.publish(options, StarWarsSchema.get(), rh2 -> {
                context.assertTrue(rh2.succeeded());
                context.assertEquals(2, schemaPublisher.registeredSchemas().size());

                // Re-publish 1. No change, already published
                schemaPublisher.publish(options, DroidsSchema.get(), rh3 -> {
                    context.assertFalse(rh3.succeeded());
                    context.assertEquals(2, schemaPublisher.registeredSchemas().size());

                    // Publish 1 to different repository. Creates new registrations 1'
                    schemaPublisher.publish(new ServiceDiscoveryOptions(options).setName("theOtherRegistry"),
                            DroidsSchema.get(), rh4 -> {
                        context.assertTrue(rh4.succeeded());
                        context.assertEquals(3, schemaPublisher.registeredSchemas().size());

                        // Unpublish 1. Discovery still in use
                        schemaPublisher.unpublish(rh.result(), rh5 -> {
                            context.assertTrue(rh5.succeeded());
                            context.assertEquals(2, schemaPublisher.registeredSchemas().size());
                            context.assertEquals(2, schemaPublisher.managedDiscoveries().size());

                            // Unpublish 2. Discovery now closed
                            schemaPublisher.unpublish(rh2.result(), rh6 -> {
                                assertTrue(rh6.succeeded());
                                assertEquals(1, schemaPublisher.registeredSchemas().size());
                                assertEquals(1, schemaPublisher.managedDiscoveries().size());

                                // Publish 1 again
                                schemaPublisher.publish(options, DroidsSchema.get(), rh7 -> {
                                    context.assertTrue(rh7.succeeded());
                                    assertEquals(2, schemaPublisher.managedDiscoveries().size());

                                    schemaPublisher.registrar.close((registration, handler) ->
                                            handler.handle(Future.succeededFuture()), rh8 ->
                                            {
                                                assertEquals(0, schemaPublisher.managedDiscoveries().size());
                                                async.complete();
                                            });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    @Test
    @Ignore("Need to rewrite test so it waits for the results of CompositeFuture in publishAll calls")
    public void should_Manage_Schema_Registration_And_Close_Properly2(TestContext context) {
        Async async = context.async(6);
        context.assertNotNull(schemaRegistrar.getPublisherId());
        schemaRegistrar = SchemaRegistrar.create(vertx, "thePublisherId");
        context.assertEquals("thePublisherId", schemaRegistrar.getPublisherId());

        ServiceDiscovery discovery = schemaRegistrar.getOrCreateDiscovery(options);
        context.assertNotNull(discovery);
        context.assertNotNull(schemaRegistrar.registrations());
        context.assertEquals(0, schemaRegistrar.registrations().size());

        // Publish 1
        schemaPublisher.publish(options, DroidsSchema.get(), rh -> {
            context.assertTrue(rh.succeeded());
            context.assertEquals(1, schemaPublisher.registeredSchemas().size());
            async.countDown();

            // Publish 2
            schemaPublisher.publish(options, StarWarsSchema.get(), rh2 -> {
                context.assertTrue(rh2.succeeded());
                context.assertEquals(2, schemaPublisher.registeredSchemas().size());
                async.countDown();

                // Re-publish 1 and 2. No change, already published
                schemaPublisher.publish(options, DroidsSchema.get(), rh3 -> {
                    context.assertTrue(rh3.succeeded());
                    context.assertEquals(2, schemaPublisher.registeredSchemas().size());
                    async.countDown();

                    // Publish 1 and 2 to different repository. Creates 2 new registrations 1' and 2'
                    schemaPublisher.publishAll(
                            new ServiceDiscoveryOptions(options).setName("theOtherRegistry"), rh4 -> {
                        context.assertTrue(rh4.succeeded());
                        context.assertEquals(4, schemaPublisher.registeredSchemas().size());
                        async.countDown();

                        // Unpublish 1. Discovery still in use
                        schemaPublisher.unpublish(schemaPublisher.registeredSchemas().get(0), rh5 -> {
                            context.assertTrue(rh5.succeeded());
                            context.assertEquals(3, schemaPublisher.registeredSchemas().size());
                            context.assertEquals(2, schemaPublisher.managedDiscoveries().size());
                            async.countDown();

                            // Unpublish 2. Discovery now closed
                            schemaPublisher.unpublish(schemaPublisher.registeredSchemas().get(0), rh6 -> {
                                context.assertTrue(rh6.succeeded());
                                context.assertEquals(2, schemaPublisher.registeredSchemas().size());
                                context.assertEquals(1, schemaPublisher.managedDiscoveries().size());
                                async.countDown();
                            });
                        });
                    }, StarWarsSchema.get(), DroidsSchema.get());
                });
            });
        });
    }

    private class TestClass implements SchemaPublisher {

        private SchemaRegistrar registrar;

        public TestClass(SchemaRegistrar registrar) {
            this.registrar = registrar;
        }

        @Override
        public SchemaRegistrar schemaRegistrar() {
            return registrar;
        }

        @Override
        public void schemaPublished(SchemaRegistration registration) {

        }

        @Override
        public void schemaUnpublished(SchemaRegistration registration) {

        }
    }
}
