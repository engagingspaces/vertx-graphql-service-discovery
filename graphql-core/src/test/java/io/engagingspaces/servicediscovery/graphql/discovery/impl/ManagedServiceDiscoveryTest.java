package io.engagingspaces.servicediscovery.graphql.discovery.impl;

import io.vertx.core.Vertx;
import io.vertx.core.impl.Action;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(VertxUnitRunner.class)
public class ManagedServiceDiscoveryTest {

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @Test
    public void should_Invoke_Close_Action_When_Closing_Managed_Discovery(TestContext context) {
        Async async = context.async();
        AtomicInteger check = new AtomicInteger(1);

        final Action<Void> closeAction = () -> {
            assertEquals(0, check.decrementAndGet());
            async.complete();
            return null;
        };
        ServiceDiscovery discovery = ManagedServiceDiscovery.of(
                ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setName("discovery2")), closeAction);
        discovery.close();
    }

    @Test
    public void should_Not_Wrap_An_Already_Managed_Discovery() {
        Action<Void> closeAction = () -> null;
        ServiceDiscovery discovery = ManagedServiceDiscovery.of(
                ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setName("discovery2")), closeAction);
        ServiceDiscovery discovery2 = ManagedServiceDiscovery.of(discovery, closeAction);
        assertEquals(discovery, discovery2);
    }
}
