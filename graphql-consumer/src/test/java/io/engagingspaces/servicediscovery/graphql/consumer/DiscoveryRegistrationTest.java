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

import io.engagingspaces.servicediscovery.graphql.discovery.impl.AbstractRegistration;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for the discovery registration class.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
public class DiscoveryRegistrationTest {

    private Vertx vertx;
    private ServiceDiscovery discovery;
    private ServiceDiscovery discovery2;
    private ServiceDiscoveryOptions options;
    private ServiceDiscoveryOptions options2;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        options = new ServiceDiscoveryOptions().setName("theDiscovery")
                .setAnnounceAddress("foo").setUsageAddress("bar");
        options2 = new ServiceDiscoveryOptions().setName("theDiscovery2")
                .setAnnounceAddress("foo").setUsageAddress("bar");
        discovery = ServiceDiscovery.create(vertx, options);
        discovery2 = ServiceDiscovery.create(vertx, options);
    }

    @Test
    public void should_Include_Record_Name_And_Type_In_Equality_Check() {
        DiscoveryRegistration result1 = DiscoveryRegistration.create(discovery, options);
        assertNotEquals(result1, new testRegistration(discovery, options));
        assertEquals(result1, DiscoveryRegistration.create(discovery, options));
        DiscoveryRegistration result2 = DiscoveryRegistration.create(discovery2, options);
        assertNotEquals(result1, result2);
        DiscoveryRegistration result3 = DiscoveryRegistration.create(discovery2, options2);
        assertNotEquals(result2, result3);

        // Ignore addresses
        DiscoveryRegistration result4 = DiscoveryRegistration.create(discovery2,
                options.setAnnounceAddress("baz").setUsageAddress("brr"));
        assertEquals(result2, result4);
        assertEquals(result2.hashCode(), result4.hashCode());
    }

    private class testRegistration extends AbstractRegistration {

        protected testRegistration(ServiceDiscovery discovery, ServiceDiscoveryOptions options) {
            super(discovery, options);
        }
    }
}
