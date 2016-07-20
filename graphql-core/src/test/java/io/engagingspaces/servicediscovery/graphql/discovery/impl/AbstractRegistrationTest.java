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

package io.engagingspaces.servicediscovery.graphql.discovery.impl;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for the abstract registration base class.
 *
 * @author Arnold Schrijver
 */
public class AbstractRegistrationTest {

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private ServiceDiscoveryOptions options;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("theAnnounceAddress").setUsageAddress("theUsageAddress");
        discovery = ServiceDiscovery.create(vertx, options);
    }

    @After
    public void tearDown() {
        discovery.close();
        vertx.close();
    }

    @Test
    public void should_Implement_Proper_Reference_Equality() {
        TestRegistration registration1 = new TestRegistration(discovery, options);
        assertEquals(registration1, registration1);
        assertNotEquals(registration1, "test");

        TestRegistration registration2 = new TestRegistration(discovery, options);
        assertEquals(registration1, registration2);
        assertEquals(registration1.getDiscovery(), registration2.getDiscovery());
        assertEquals(registration1.getDiscoveryOptions().getName(), registration2.getDiscoveryOptions().getName());
        assertNotEquals(registration1.getDiscoveryOptions(), registration2.getDiscoveryOptions()); // returns a copy

        TestRegistration registration3 = new TestRegistration(discovery, new ServiceDiscoveryOptions(options));
        assertEquals(registration1, registration3);

        TestRegistration registration4 = new TestRegistration(discovery,
                new ServiceDiscoveryOptions(options).setName("theOtherDiscovery"));
        assertNotEquals(registration1, registration4);

        TestRegistration registration5 = new TestRegistration(ServiceDiscovery.create(vertx), options);
        assertNotEquals(registration1, registration5); // would be illegal as well
    }

    private class TestRegistration extends AbstractRegistration {
        protected TestRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
            super(discovery, options);
        }
    }
}
