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

package io.engagingspaces.graphql.servicediscovery.consumer;

import io.engagingspaces.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.graphql.events.SchemaReferenceData;
import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests for discovery registrar.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class DiscoveryRegistrarTest {

    private DiscoveryRegistrar discoveryRegistrar;

    private Vertx vertx;
    private ServiceDiscoveryOptions options;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @Test
    public void should_Create_Discovery_And_Start_Listening_For_Events(TestContext context) {
        Async async = context.async(2);
        vertx.runOnContext(ctx ->
        {
            discoveryRegistrar = DiscoveryRegistrar.create(vertx);
            final AtomicInteger checkAnnounce = new AtomicInteger(1);
            final AtomicInteger checkUsage = new AtomicInteger(1);

            options = new ServiceDiscoveryOptions().setName("the-discovery")
                    .setAnnounceAddress("announceAddress").setUsageAddress("usageAddress");
            SchemaAnnounceHandler announce = ann -> {
                context.assertTrue(checkAnnounce.get() == 0 && "theSchemaRecord".equals(ann.getName()));
                async.countDown();
            };

            DiscoveryRegistration registration = discoveryRegistrar.startListening(options, announce, usage -> {
                context.assertTrue(checkUsage.get() == 0 && "theSchemaRecord".equals(usage.getRecord().getName()));
                async.countDown();
            });

            assertNotNull(registration);
            assertNotNull(registration.getDiscovery());
            assertNotNull(registration.getDiscoveryOptions());
            ServiceDiscoveryOptions registeredOptions = registration.getDiscoveryOptions();
            assertEquals(options.getName(), registeredOptions.getName());
            assertEquals(options.getAnnounceAddress(), registeredOptions.getAnnounceAddress());
            assertEquals(options.getUsageAddress(), registeredOptions.getUsageAddress());
            JsonObject wrongTypeRecord = new Record().setName("someRecord").toJson();
            JsonObject matchingRecord = new Record().setName("theSchemaRecord")
                    .setType(Queryable.SERVICE_TYPE).toJson();

            vertx.eventBus().send("announceAddress", wrongTypeRecord);
            checkAnnounce.decrementAndGet();
            vertx.eventBus().send("announceAddress", matchingRecord);

            vertx.eventBus().send("usageAddress", createReferenceInfo(wrongTypeRecord).toJson());
            checkUsage.decrementAndGet();
            vertx.eventBus().send("usageAddress", createReferenceInfo(matchingRecord).toJson());
        });
        async.awaitSuccess();
    }

    @Test
    public void should_Cleanup_Unused_Consumers_On_Closing_Managed_Discovery() {
        discoveryRegistrar = DiscoveryRegistrar.create(vertx);
        ServiceDiscovery discovery1 = discoveryRegistrar.startListening(
                new ServiceDiscoveryOptions().setName("discovery1"), record -> {}, refData -> {}).getDiscovery();
        ServiceDiscovery discovery2 = discoveryRegistrar.startListening(
                new ServiceDiscoveryOptions().setName("discovery1"), record -> {}, refData -> {}).getDiscovery();
        ServiceDiscovery discovery3 = discoveryRegistrar.startListening(
                new ServiceDiscoveryOptions().setName("discovery2").setAnnounceAddress("otherAnnounce"),
                        record -> {}, refData -> {}).getDiscovery();

        assertEquals(2, discoveryRegistrar.serviceDiscoveryNames().size());
        assertNotNull(discoveryRegistrar.getDiscovery("discovery1"));
        assertEquals(discoveryRegistrar.getDiscovery("discovery1"), discovery2);
        assertNotEquals(discoveryRegistrar.getDiscovery("discovery2"), discoveryRegistrar.getDiscovery("discovery3"));

        // TODO Complete test

        discovery1.close();
    }

    private SchemaReferenceData createReferenceInfo(JsonObject record) {
        return new SchemaReferenceData(
                new JsonObject().put("id", "theId").put("type", "bind").put("record", record)
        );
    }
}
