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
import io.engagingspaces.servicediscovery.graphql.query.Queryable;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Tests for schema publisher interface default implementation.
 *
 * @author Arnold Schrijver
 */
@RunWith(VertxUnitRunner.class)
public class SchemaPublisherTest {

    private TestClass schemaPublisher;

    private Vertx vertx;
    private ServiceDiscoveryOptions options;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("theAnnounceAddress").setUsageAddress("theUsageAddress");
        schemaPublisher = new TestClass(vertx);
    }

    @After
    public void tearDown(TestContext context) {
        SchemaPublisher.close(schemaPublisher, context.asyncAssertSuccess(rh2 -> {
            vertx.close();
        }));
    }

    @Test
    public void should_Publish_Schema_Definition(TestContext context) {
        Async async = context.async();
        vertx.runOnContext( ctx ->
        {
            schemaPublisher = new TestClass(vertx);
            SchemaPublisher.publish(schemaPublisher, options, DroidsSchema.get(), rh -> {
                context.assertTrue(rh.succeeded());
                context.assertNotNull(rh.result());
                context.assertNotNull(schemaPublisher.getDiscovery("theDiscovery").get());
                context.assertFalse(schemaPublisher.getDiscovery("theUnknownDiscovery").isPresent());
                SchemaRegistration registration = rh.result();
                context.assertTrue(registration.getPublisherId().isPresent());
                context.assertEquals("thePublisherId", registration.getPublisherId().get());
                context.assertNotNull(registration.getRecord());

                Record record = registration.getRecord();
                context.assertEquals(Queryable.SERVICE_TYPE, record.getType());
                context.assertEquals("QueryType", record.getName());
                context.assertEquals(registration.getSchemaName(), record.getName());
                context.assertNotNull(record.getRegistration());
                context.assertEquals(Queryable.ADDRESS_PREFIX + ".QueryType", record.getLocation().getString(Record.ENDPOINT));
                context.assertEquals(Status.UP, record.getStatus());

                ServiceDiscoveryOptions options = registration.getDiscoveryOptions();
                context.assertEquals("theDiscovery", options.getName());
                context.assertEquals("theAnnounceAddress", options.getAnnounceAddress());
                context.assertEquals("theUsageAddress", options.getUsageAddress());

                assertNotNull(registration.getServiceConsumer());
                assertEquals(Queryable.ADDRESS_PREFIX + ".QueryType", registration.getServiceConsumer().address());
                assertTrue(registration.getServiceConsumer().isRegistered());

                schemaPublisher.getDiscovery("theDiscovery").get().unpublish("QueryType", rh2 -> {
                    assertFalse(rh2.succeeded());
                    schemaPublisher.getDiscovery("theDiscovery").get().unpublish(record.getRegistration(), rh3 -> {
                        assertTrue(rh3.succeeded());
                        async.complete();
                    });
                });
            });
        });
        async.awaitSuccess();
    }

    @Test
    public void should_Return_Failure_When_Schema_Definition_Not_Provided(TestContext context) {
        Async async = context.async(4);
        SchemaPublisher.publish(schemaPublisher, new ServiceDiscoveryOptions(), null, rh -> {
            assertFalse(rh.succeeded());
            async.countDown();
        });
        SchemaPublisher.publish(schemaPublisher, new ServiceDiscoveryOptions(), null, null, rh -> {
            assertFalse(rh.succeeded());
            async.countDown();
        });
        SchemaPublisher.publishAll(schemaPublisher, new ServiceDiscoveryOptions(), rh -> {
            assertFalse(rh.succeeded());
            async.countDown();
        });
        SchemaDefinition[] definitions = null;
        SchemaPublisher.publishAll(schemaPublisher, new ServiceDiscoveryOptions(), rh -> {
            assertFalse(rh.succeeded());
            async.countDown();
        }, definitions);
        async.awaitSuccess();
    }

    private class TestClass implements SchemaPublisher {

        private SchemaRegistrar registrar;

        public TestClass(Vertx vertx) {
            this.registrar = SchemaRegistrar.create(vertx, "thePublisherId");
        }

        @Override
        public SchemaRegistrar schemaRegistrar() {
            return registrar;
        }

        @Override
        public void schemaPublished(SchemaRegistration registration) {
            assertNotNull(registration);
        }

        @Override
        public void schemaUnpublished(SchemaRegistration registration) {
            assertNotNull(registration);
        }
    }
}
