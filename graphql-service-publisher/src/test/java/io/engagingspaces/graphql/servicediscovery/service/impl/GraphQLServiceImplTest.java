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

package io.engagingspaces.graphql.servicediscovery.service.impl;

import io.engagingspaces.graphql.query.Queryable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.ServiceReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for graphql service type implementation class.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@RunWith(VertxUnitRunner.class)
public class GraphQLServiceImplTest {

    private io.engagingspaces.graphql.servicediscovery.service.impl.GraphQLServiceImpl graphQLService;

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private Record record;
    private JsonObject config;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setName("test-discovery"));
        config = new JsonObject().put("deliveryOptions",
                new JsonObject().put("timeout", 1000).put("codecName", "theCodecName"));
        record = new Record().setLocation(new JsonObject().put(Record.ENDPOINT, "theEndpoint"));
        graphQLService = new io.engagingspaces.graphql.servicediscovery.service.impl.GraphQLServiceImpl();
    }

    @Test
    public void should_Create_Service_Reference_With_Configuration() {
        ServiceReference reference = graphQLService.get(vertx, discovery, record, config);
        assertNotNull(reference);
        assertNull(reference.cached());
        Queryable serviceProxy = reference.get();
        assertNotNull(serviceProxy);
    }
}
