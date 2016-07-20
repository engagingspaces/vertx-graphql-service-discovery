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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for the abstract registrar base class.
 *
 * @author Arnold Schrijver
 */
@RunWith(VertxUnitRunner.class)
public class AbstractRegistrarTest {

    private Vertx vertx;
    private ServiceDiscoveryOptions options;
    private TestRegistration registration;
    private TestClass abstractRegistrar;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        abstractRegistrar = new TestClass(vertx);
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("theAnnounceAddress").setUsageAddress("theUsageAddress");
    }

    @Test
    public void should_Manage_Service_Discovery_Creation_And_Invoke_Close_Handler(TestContext context) {
        Async async = context.async(2);
        vertx.runOnContext(ctx ->
        {
            ServiceDiscovery serviceDiscoveryDefault = abstractRegistrar
                    .getOrCreateDiscovery(new ServiceDiscoveryOptions(), null);
            context.assertNotNull(serviceDiscoveryDefault);

            ServiceDiscovery serviceDiscovery1 = abstractRegistrar.getOrCreateDiscovery(options, () -> {
                abstractRegistrar.closeServiceDiscovery(options.getName());
                async.countDown();
                return null;
            });
            context.assertTrue(serviceDiscovery1 instanceof ManagedServiceDiscovery);
            context.assertEquals(1, abstractRegistrar.serviceDiscoveryNames().size());

            serviceDiscoveryDefault.close();
            context.assertEquals(1, abstractRegistrar.serviceDiscoveryNames().size());
            ServiceDiscovery serviceDiscovery2 = abstractRegistrar.getOrCreateDiscovery(options, null);
            context.assertEquals(serviceDiscovery1, serviceDiscovery2);

            serviceDiscovery1.close();
            context.assertEquals(1, async.count());
            context.assertEquals(0, abstractRegistrar.serviceDiscoveryNames().size());
            async.countDown();

        });
        async.awaitSuccess();
    }

    @Test
    public void should_Manage_Registrations() {
        abstractRegistrar.getOrCreateDiscovery(options, () -> {
            abstractRegistrar.closeServiceDiscovery(options.getName());
            return null;
        });
        TestRegistration registration1 = new TestRegistration(ServiceDiscovery.create(vertx, options), options);
        assertNotNull(abstractRegistrar.registrations());
        abstractRegistrar.register(options.getName(), registration1);
        assertEquals(1, abstractRegistrar.registrations().size());

        TestRegistration registration2 = new TestRegistration(ServiceDiscovery.create(vertx, options), options);
        abstractRegistrar.register(options.getName(), registration2);
        assertEquals(2, abstractRegistrar.registrations().size());

        abstractRegistrar.unregister(registration2);
        assertEquals(1, abstractRegistrar.registrations().size());
        assertEquals(1, abstractRegistrar.serviceDiscoveryNames().size());
        abstractRegistrar.unregister(registration1);
        assertEquals(0, abstractRegistrar.registrations().size());
        assertEquals(0, abstractRegistrar.serviceDiscoveryNames().size());

        abstractRegistrar.close();
    }

    @Test
    public void should_Cleanup_On_Close() {
        abstractRegistrar.getOrCreateDiscovery(options, () -> {
            abstractRegistrar.closeServiceDiscovery(options.getName());
            return null;
        });
        TestRegistration registration1 = new TestRegistration(ServiceDiscovery.create(vertx, options), options);
        abstractRegistrar.register(options.getName(), registration1);
        assertEquals(1, abstractRegistrar.registrations().size());
        assertEquals(1, abstractRegistrar.serviceDiscoveryNames().size());

        abstractRegistrar.close();
        assertEquals(0, abstractRegistrar.registrations().size());
        assertEquals(0, abstractRegistrar.serviceDiscoveryNames().size());
    }

    private class TestRegistration extends AbstractRegistration {

        protected TestRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
            super(discovery, options);
        }
    }

    private class TestClass extends AbstractRegistrar<TestRegistration> {

        protected TestClass(Vertx vertx) {
            super(vertx);
        }
    }
}
