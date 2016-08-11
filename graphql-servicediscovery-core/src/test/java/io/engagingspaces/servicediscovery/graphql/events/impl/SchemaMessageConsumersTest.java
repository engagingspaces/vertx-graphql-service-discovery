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

package io.engagingspaces.servicediscovery.graphql.events.impl;

import org.example.servicediscovery.server.droids.DroidsSchema;
import io.engagingspaces.servicediscovery.graphql.events.SchemaAnnounceHandler;
import io.engagingspaces.servicediscovery.graphql.events.SchemaUsageHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the schema message consumers helper class.
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class SchemaMessageConsumersTest {

    private SchemaMessageConsumers messageConsumers;

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        messageConsumers = new SchemaMessageConsumers(vertx);
    }

    @After
    public void tearDown() {
        messageConsumers.close();
        vertx.close();
    }

    @Test
    public void should_Manage_Consumers_Without_Registering_Duplicates() {
        messageConsumers.registerConsumer("announce", (SchemaAnnounceHandler) rh -> {});
        assertEquals(1, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("announce").isRegistered());

        messageConsumers.registerConsumer("usage", (SchemaUsageHandler) rh -> {});
        assertEquals(2, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("usage").isRegistered());

        messageConsumers.registerConsumer("usage", (SchemaUsageHandler) rh -> {});
        assertEquals(2, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("usage").isRegistered());

        messageConsumers.unregisterConsumer("usage");
        assertEquals(2, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("usage").isRegistered());

        messageConsumers.unregisterConsumer("usage");
        assertEquals(1, messageConsumers.getConsumers().size());

        messageConsumers.unregisterConsumer("usage");
        assertEquals(1, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("announce").isRegistered());

        messageConsumers.unregisterConsumer("announce");
        assertEquals(0, messageConsumers.getConsumers().size());
    }

    @Test
    public void should_Manage_Service_Consumers() {
        messageConsumers.registerServiceConsumer("DroidQueries", DroidsSchema.get());
        assertEquals(1, messageConsumers.getConsumers().size());
        assertTrue(messageConsumers.getConsumers().get("DroidQueries").isRegistered());

        messageConsumers.registerServiceConsumer("DroidQueries", DroidsSchema.get());
        assertEquals(1, messageConsumers.getConsumers().size());

        messageConsumers.unregisterConsumer("DroidQueries");
        assertEquals(1, messageConsumers.getConsumers().size());

        messageConsumers.registerConsumer("announce", (SchemaAnnounceHandler) rh -> {});
        assertEquals(2, messageConsumers.getConsumers().size());

        messageConsumers.unregisterConsumer("DroidQueries");
        assertEquals(1, messageConsumers.getConsumers().size());

        messageConsumers.close();
        assertEquals(0, messageConsumers.getConsumers().size());
    }

    @Test
    public void should_Cleanup_When_Closing() {
        messageConsumers.registerConsumer("announce", (SchemaAnnounceHandler) rh -> {});
        messageConsumers.registerConsumer("announce", (SchemaAnnounceHandler) rh -> {});
        messageConsumers.registerConsumer("announce1", (SchemaAnnounceHandler) rh -> {});
        messageConsumers.registerConsumer("announce1", (SchemaAnnounceHandler) rh -> {});
        messageConsumers.registerConsumer("usage", (SchemaUsageHandler) rh -> {});
        messageConsumers.registerConsumer("usage1", (SchemaUsageHandler) rh -> {});
        messageConsumers.registerConsumer("announce", (SchemaUsageHandler) rh -> {});
        assertEquals(4, messageConsumers.getConsumers().size());
        messageConsumers.close();
        assertEquals(0, messageConsumers.getConsumers().size());
    }
}
